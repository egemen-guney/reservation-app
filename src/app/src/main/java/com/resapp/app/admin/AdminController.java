package com.resapp.app.admin;

import com.resapp.app.account.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admins")
public class AdminController {
    private final AccountService accountService;

    public AdminController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/restaurant/{restaurantId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRestaurant(@PathVariable UUID restaurantId) {
        accountService.deleteRestaurant(restaurantId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/customer/{customerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustomer(@PathVariable UUID customerId) {
        accountService.deleteCustomer(customerId);
    }

//    @PostMapping("/register")
//    @ResponseStatus(HttpStatus.CREATED)
//    public void registerAdmin(@Valid @RequestBody AdminRegistrationRequest request) {
//        accountService.registerNewAdmin(request);
//    }
}
