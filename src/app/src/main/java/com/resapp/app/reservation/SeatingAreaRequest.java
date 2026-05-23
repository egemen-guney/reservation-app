package com.resapp.app.reservation;

import jakarta.validation.constraints.Min;

public record SeatingAreaRequest(
        AreaName areaName,
        @Min(value = 1, message = "Capacity must be at least 1.")
        int capacity
) { }