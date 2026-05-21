package com.resapp.app.restaurant;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

public record RestaurantRegistrationRequest(
        String email, String phone,
        String password,
        String name,
        // UUID addressId,
        String street, String city, String state, String zipCode, String country,
        String busPhone,
        BigDecimal stars,
        LocalTime openingHours, LocalTime closingHours, boolean isOpen
) { }
