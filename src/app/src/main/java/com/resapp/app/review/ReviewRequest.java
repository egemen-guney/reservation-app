package com.resapp.app.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReviewRequest(
        @NotNull(message = "Restaurant ID is required")
        UUID restaurantId,

        @Min(value = 1, message = "Stars must be at least 1")
        @Max(value = 5, message = "Stars cannot be more than 5")
        int stars,

        String comment
) { }