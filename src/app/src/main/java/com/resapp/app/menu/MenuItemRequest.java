package com.resapp.app.menu;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record MenuItemRequest(
        @NotBlank(message = "Item name is required.")
        String name,
        String description,

        @NotBlank(message = "Category is required.")
        String category,

        @NotNull(message = "Price is required.")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero.")
        BigDecimal price,
        boolean isAvailable
) { }
