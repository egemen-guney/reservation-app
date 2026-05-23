package com.resapp.app.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private UUID orderId;
    private UUID resId;
    private UUID customerId;
    private BigDecimal totalPrice;
    private String ccNum;
    private OrderStatus status;
    private OffsetDateTime createdAt;
}
