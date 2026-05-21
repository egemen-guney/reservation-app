package com.resapp.app.menu;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem {
    private UUID menuItemId;
    private UUID menuId;
    private String name;
    private String description;
    private String category;
    private boolean isAvailable;
    private BigDecimal price;
    private BigDecimal satisfaction;
}
