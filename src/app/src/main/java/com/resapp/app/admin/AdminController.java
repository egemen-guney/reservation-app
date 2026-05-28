package com.resapp.app.admin;

import com.resapp.app.account.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admins")
public class AdminController {
    private final AccountService accountService;

    public AdminController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerAdmin(@Valid @RequestBody AdminRegistrationRequest request) {
        accountService.registerNewAdmin(request);
    }
}
