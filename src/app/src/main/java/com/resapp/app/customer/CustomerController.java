package com.resapp.app.customer;

import com.resapp.app.account.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private final AccountService accountService;

    public CustomerController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public String demo() { // debug function to test auth
        return "passed";
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerCustomer(@Valid @RequestBody CustomerRegistrationRequest request) {
        accountService.registerNewCustomer(request);
    }
}
