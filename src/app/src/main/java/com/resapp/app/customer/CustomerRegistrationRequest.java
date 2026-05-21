package com.resapp.app.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerRegistrationRequest(
        @NotBlank(message = "Email is required.")
        @Email(message = "Must be a valid email (abc@xyz.com).")
        String email,

        @NotBlank(message = "Phone number is required.")
        String phone,

        @NotBlank(message = "Password is required.")
        @Size(min = 8, message = "Password must be at least 8 characters long.")
        String password,

        @NotBlank(message = "First name is required.")
        String firstName,

        @NotBlank(message = "Last name is required.")
        String lastName
) { }
