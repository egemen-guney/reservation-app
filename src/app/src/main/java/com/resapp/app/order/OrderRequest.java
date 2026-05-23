package com.resapp.app.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record OrderRequest(
        @NotNull(message = "Reservation ID is required.")
        UUID resId,

        @NotBlank(message = "Credit card number (or token) is required.")
        String ccNum,

        @NotEmpty(message = "Order must contain at least one item.")
        @Valid
        List<OrderItemRequest> items
) { }