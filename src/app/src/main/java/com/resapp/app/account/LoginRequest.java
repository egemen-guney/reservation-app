package com.resapp.app.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Email or phone number is required.")
        String emailOrPhone,

        @NotBlank(message = "Password is required.")
        String password
) {
}
