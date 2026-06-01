package com.resapp.app.restaurant;

import com.resapp.app.account.AccountPrincipal;
import com.resapp.app.account.AccountService;
import com.resapp.app.address.Address;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {
    private final AccountService accountService;

    public RestaurantController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * ONLY ADMINS AND CUSTOMERS SHOULD BE ABLE TO ACCESS
     */
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    @GetMapping
    public List<Restaurant> findAllRestaurants() {
        return accountService.getAllRestaurants();
    }

    /**
     * ONLY ADMINS AND CUSTOMERS SHOULD BE ABLE TO ACCESS
     */
    // @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    @GetMapping("/{restaurantId}")
    public Optional<Restaurant> findRestaurant(@PathVariable UUID restaurantId, @AuthenticationPrincipal AccountPrincipal principal) {
        return accountService.getRestaurantById(restaurantId, principal.getAccount());
    }

    @GetMapping("/{restaurantId}/address")
    public Optional<Address> findRestaurantAddress(@PathVariable UUID restaurantId, @AuthenticationPrincipal AccountPrincipal principal) {
        return accountService.getRestaurantAddress(restaurantId, principal.getAccount());
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerRestaurant(@Valid @RequestBody RestaurantRegistrationRequest request) {
        accountService.registerNewRestaurant(request);
    }
}
