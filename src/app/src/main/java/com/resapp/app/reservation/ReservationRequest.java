package com.resapp.app.reservation;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.OffsetTime;
import java.util.UUID;

public record ReservationRequest(
        @NotNull(message = "Area ID is required.")
        UUID areaId,

        @NotNull(message = "Reservation date is required.")
        @FutureOrPresent(message = "Cannot book in the past.")
        LocalDate date,

        @NotNull(message = "Start time is required.")
        OffsetTime start,

        @NotNull(message = "End time is required.")
        OffsetTime end,

        @Min(value = 1, message = "Must book for at least 1 person.")
        int size,
        String note
) { }