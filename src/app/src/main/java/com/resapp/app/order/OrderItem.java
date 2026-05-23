package com.resapp.app.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    private UUID orderId;
    private UUID menuItemId;
    private int quantity;
}
