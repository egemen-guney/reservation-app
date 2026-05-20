package com.resapp.app.restaurant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Restaurant {
    private UUID restaurantId;
    private UUID accountId;
    private String name;
    private UUID addressId;
    private String busPhone;
    private UUID menuId;
    private BigDecimal stars;
    private LocalTime openingHours;
    private LocalTime closingHours;
    private boolean isOpen;
}
