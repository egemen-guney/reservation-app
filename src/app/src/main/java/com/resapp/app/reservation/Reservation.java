package com.resapp.app.reservation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {
    private UUID resId;
    private UUID restaurantId;
    private UUID customerId;
    private UUID areaId;
    private LocalDate resDate;
    private OffsetTime startTime;
    private OffsetTime endTime;
    private int numPeople;
    private String note;
    private ReservationStatus status;
}
