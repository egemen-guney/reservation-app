package com.resapp.app.address;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    private UUID addressId;
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}
