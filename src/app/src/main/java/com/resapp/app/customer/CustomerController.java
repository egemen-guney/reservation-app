package com.resapp.app.customer;

import com.resapp.app.account.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private final AccountService accountService;

    public CustomerController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<Customer> getCustomers() { // debug function to test auth
        return accountService.getAllCustomers();
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerCustomer(@Valid @RequestBody CustomerRegistrationRequest request) {
        accountService.registerNewCustomer(request);
    }
}
