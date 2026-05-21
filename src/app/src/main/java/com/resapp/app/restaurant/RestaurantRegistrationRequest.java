package com.resapp.app.restaurant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalTime;

public record RestaurantRegistrationRequest(
        @NotBlank(message = "Email is required.")
        @Email(message = "Must be a valid email (abc@xyz.com).")
        String email,

        @NotBlank(message = "Phone number is required.")
        String phone,

        @NotBlank(message = "Password is required.")
        @Size(min = 8, message = "Password must be at least 8 characters long.")
        String password,

        @NotBlank(message = "Restaurant name is required.")
        String name,

        @NotBlank(message = "Street address is required.")
        String street,

        @NotBlank(message = "City is required.")
        String city,
        String state,

        @NotBlank(message = "Zip code is required.")
        String zipCode,

        @NotBlank(message = "Country is required.")
        String country,

        @NotBlank(message = "Restaurant phone is required.")
        String busPhone,
        BigDecimal stars,

        @NotNull(message = "Opening times are required.")
        LocalTime openingHours,

        @NotNull(message = "Closing times are required.")
        LocalTime closingHours,
        boolean isOpen
) { }
