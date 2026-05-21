package com.resapp.app.restaurant;

import com.resapp.app.account.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {
    private final AccountService accountService;

    public RestaurantController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerRestaurant(@Valid @RequestBody RestaurantRegistrationRequest request) {
        accountService.registerNewRestaurant(request);
    }
}
