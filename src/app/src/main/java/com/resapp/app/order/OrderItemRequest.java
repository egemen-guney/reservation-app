package com.resapp.app.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OrderItemRequest(
        @NotNull(message = "Menu item ID is required.")
        UUID menuItemId,

        @Min(value = 1, message = "Quantity can be at least one.")
        int quantity
) { }
