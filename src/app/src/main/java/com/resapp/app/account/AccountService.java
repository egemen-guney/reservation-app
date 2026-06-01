package com.resapp.app.account;

import com.resapp.app.address.Address;
import com.resapp.app.address.AddressRepository;
import com.resapp.app.admin.Admin;
import com.resapp.app.admin.AdminRegistrationRequest;
import com.resapp.app.admin.AdminRepository;
import com.resapp.app.customer.Customer;
import com.resapp.app.customer.CustomerRepository;
import com.resapp.app.customer.CustomerRegistrationRequest;
import com.resapp.app.menu.Menu;
import com.resapp.app.menu.MenuRepository;
import com.resapp.app.restaurant.Restaurant;
import com.resapp.app.restaurant.RestaurantRegistrationRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.resapp.app.restaurant.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final AdminRepository adminRepository;
    private final RestaurantRepository restaurantRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;
    private final MenuRepository menuRepository;
    private final JWTService jwtService;

    public AccountService(AccountRepository accountRepository, CustomerRepository customerRepository,
                          AdminRepository adminRepository, RestaurantRepository restaurantRepository,
                          AddressRepository addressRepository, PasswordEncoder passwordEncoder,
                          MenuRepository menuRepository, JWTService service) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.adminRepository = adminRepository;
        this.restaurantRepository = restaurantRepository;
        this.addressRepository = addressRepository;
        this.passwordEncoder = passwordEncoder;
        this.menuRepository = menuRepository;
        this.jwtService = service;
    }

    @Transactional
    public void registerNewCustomer(CustomerRegistrationRequest request) {
        if (accountRepository.findByEmail(request.email()).isPresent()) throw new IllegalStateException("An account with this email already exists.");
        if (accountRepository.findByPhone(request.phone()).isPresent()) throw new IllegalStateException("An account with this phone number already exists.");

        String passwordHash = passwordEncoder.encode(request.password());

        UUID newAccountId = UUID.randomUUID();
        Account newAccount = Account.builder()
                .accountId(newAccountId)
                .email(request.email())
                .phone(request.phone())
                .passwordHash(passwordHash)
                .role(AccountRole.CUSTOMER)
                .build();

        UUID newCustomerId = UUID.randomUUID();
        Customer newCustomer = Customer.builder()
                .customerId(newCustomerId)
                .accountId(newAccountId)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .build();

        accountRepository.create(newAccount);
        customerRepository.create(newCustomer);
    }

    @Transactional
    public void registerNewRestaurant(RestaurantRegistrationRequest request) {
        if (accountRepository.findByEmail(request.email()).isPresent()) throw new IllegalStateException("An account with this email already exists.");
        if (accountRepository.findByPhone(request.phone()).isPresent()) throw new IllegalStateException("An account with this phone already exists.");

        String passwordHash = passwordEncoder.encode(request.password());

        UUID newAccountId = UUID.randomUUID();
        Account newAccount = Account.builder()
                .accountId(newAccountId)
                .email(request.email())
                .phone(request.phone())
                .passwordHash(passwordHash)
                .role(AccountRole.RESTAURANT)
                .build();

        UUID newRestaurantId = UUID.randomUUID();
        UUID newAddressId = UUID.randomUUID();
        UUID newMenuId = UUID.randomUUID();
        Restaurant newRestaurant = Restaurant.builder()
                .restaurantId(newRestaurantId)
                .accountId(newAccountId)
                .name(request.name())
                .addressId(newAddressId)
                .busPhone(request.busPhone())
                .menuId(newMenuId)
                .openingHours(request.openingHours())
                .closingHours(request.closingHours())
                .isOpen(request.isOpen())
                .build();

        Address newAddress = Address.builder()
                .addressId(newAddressId)
                .street(request.street())
                .city(request.city())
                .state(request.state())
                .zipCode(request.zipCode())
                .country(request.country())
                .build();

        Menu newMenu = Menu.builder()
                .menuId(newMenuId)
                .build();

        accountRepository.create(newAccount);
        addressRepository.create((newAddress));
        menuRepository.create(newMenu);
        restaurantRepository.create(newRestaurant);
    }

//    @Transactional
//    public void registerNewAdmin(AdminRegistrationRequest request) {
//        if (accountRepository.findByEmail(request.email()).isPresent()) throw new IllegalStateException("An account with this email already exists.");
//        if (accountRepository.findByPhone(request.phone()).isPresent()) throw new IllegalStateException("An account with this phone number already exists.");
//
//        String passwordHash = passwordEncoder.encode(request.password());
//
//        UUID newAccountId = UUID.randomUUID();
//        Account newAccount = Account.builder()
//                .accountId(newAccountId)
//                .email(request.email())
//                .phone(request.phone())
//                .passwordHash(passwordHash)
//                .role(AccountRole.ADMIN)
//                .build();
//
//        UUID newAdminId = UUID.randomUUID();
//        Admin newAdmin = Admin.builder()
//                .adminId(newAdminId)
//                .accountId(newAccountId)
//                .firstName(request.firstName())
//                .lastName(request.lastName())
//                .build();
//
//        accountRepository.create(newAccount);
//        adminRepository.create(newAdmin);
//    }

    // public String login(LoginRequest request) {
    @Transactional
    public LoginResponse login(LoginRequest request) {
        Account account = accountRepository.findByEmailOrPhone(request.emailOrPhone())
                .orElseThrow(() -> new IllegalArgumentException("Could not find an account with these credentials."));

        boolean passwordMatches = passwordEncoder.matches(request.password(), account.getPasswordHash());

        if (!passwordMatches) throw new IllegalArgumentException("Invalid password.");

        // return jwtService.generateToken(request.emailOrPhone());
        String token = jwtService.generateToken(request.emailOrPhone());

        // Find the specific profile ID based on their role!
        UUID profileId = null;
        if (account.getRole() == AccountRole.CUSTOMER) {
            profileId = customerRepository.findByAccountId(account.getAccountId()).get().getCustomerId();
        } else if (account.getRole() == AccountRole.RESTAURANT) {
            profileId = restaurantRepository.findByAccountId(account.getAccountId()).get().getRestaurantId();
        } else if (account.getRole() == AccountRole.ADMIN) {
            profileId = adminRepository.findByAccountId(account.getAccountId()).get().getAdminId();
        }

        return new LoginResponse(token, account.getRole().name(), profileId);
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }

    public Optional<Restaurant> getRestaurantById(UUID restaurantId, Account account) {
        if (account.getRole() == AccountRole.RESTAURANT) {
            Restaurant myRestaurant = restaurantRepository.findByAccountId(account.getAccountId())
                    .orElseThrow(() -> new IllegalStateException("Restaurant not found."));

            if (!myRestaurant.getRestaurantId().equals(restaurantId)) {
                throw new AccessDeniedException("You are only authorized to view your own restaurant.");
            }
        }
        return restaurantRepository.findByRestaurantId(restaurantId);
    }

    public Optional<Address> getRestaurantAddress(UUID restaurantId, Account account) {
        if (account.getRole() == AccountRole.RESTAURANT) {
            Restaurant myRestaurant = restaurantRepository.findByAccountId(account.getAccountId())
                    .orElseThrow(() -> new IllegalStateException("Restaurant not found."));

            if (!myRestaurant.getRestaurantId().equals(restaurantId)) {
                throw new AccessDeniedException("You are only authorized to view your own restaurant's address.");
            }
        }
        return restaurantRepository.findAddressByRestaurantId(restaurantId);
    }

    @Transactional
    public void deleteRestaurant(UUID restaurantId) {
        // we are already checking this in the delete method of repositories.
        Restaurant restaurant = restaurantRepository.findByRestaurantId(restaurantId)
                .orElseThrow(() -> new IllegalStateException("Restaurant not found."));

        restaurantRepository.delete(restaurantId);
    }

    @Transactional
    public void deleteCustomer(UUID customerId) {
        // we are already checking this in the delete method of repositories.
        Customer customer = customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new IllegalStateException("Customer not found."));

        customerRepository.delete(customerId);
    }
}
