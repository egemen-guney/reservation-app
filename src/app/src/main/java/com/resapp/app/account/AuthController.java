package com.resapp.app.account;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AccountService accountService;

    public AuthController(AccountService accountService) {
        this.accountService = accountService;
    }

    // public String login(@Valid @RequestBody LoginRequest request) {
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return accountService.login(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public void logout(HttpServletRequest request) throws ServletException {
        // This explicitly clears any background session Spring Security might be holding
        request.logout();
    }
}
