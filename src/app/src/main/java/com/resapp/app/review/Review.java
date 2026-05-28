package com.resapp.app.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    private UUID reviewId;
    private UUID restaurantId;
    private UUID customerId;
    private int stars;
    private String comment;
    private OffsetDateTime createdAt;
}
