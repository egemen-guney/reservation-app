package com.resapp.app.account;

import com.resapp.app.address.Address;
import com.resapp.app.address.AddressRepository;
import com.resapp.app.customer.Customer;
import com.resapp.app.customer.CustomerRepository;
import com.resapp.app.customer.CustomerRegistrationRequest;
import com.resapp.app.menu.Menu;
import com.resapp.app.menu.MenuRepository;
import com.resapp.app.restaurant.Restaurant;
import com.resapp.app.restaurant.RestaurantRegistrationRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.resapp.app.restaurant.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final RestaurantRepository restaurantRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;
    private final MenuRepository menuRepository;
    private JWTService jwtService;

    public AccountService(AccountRepository accountRepository, CustomerRepository customerRepository,
                          RestaurantRepository restaurantRepository, AddressRepository addressRepository,
                          PasswordEncoder passwordEncoder, MenuRepository menuRepository, JWTService service) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
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
        // debug
        // System.out.println(passwordHash);

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
        // debug
        // System.out.println(passwordHash);

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
                .stars(request.stars())
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

    @Transactional
    public String login(LoginRequest request) {
        Account account = accountRepository.findByEmailOrPhone(request.emailOrPhone())
                .orElseThrow(() -> new IllegalArgumentException("Could not find an account with these credentials."));

        boolean passwordMatches = passwordEncoder.matches(request.password(), account.getPasswordHash());

        if (!passwordMatches) throw new IllegalArgumentException("Invalid password.");

        return jwtService.generateToken(request.emailOrPhone());
        /*return new LoginResponse(
                account.getAccountId(),
                account.getRole(),
                "Successfully logged in!");*/
    }
}
