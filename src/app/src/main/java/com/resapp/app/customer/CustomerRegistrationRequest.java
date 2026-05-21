package com.resapp.app.customer;

public record CustomerRegistrationRequest(
        String email, String phone,
        String password,
        String firstName, String lastName
) { }
