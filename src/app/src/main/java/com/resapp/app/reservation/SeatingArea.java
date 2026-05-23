package com.resapp.app.reservation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatingArea {
    private UUID areaId;
    private UUID restaurantId;
    private AreaName areaName;
    private int capacity;
}
