package com.resapp.app.order;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class OrderItemRepository {
    private final JdbcClient jdbcClient;

    public OrderItemRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<OrderItem> findByItemId(UUID itemId, UUID orderId) {
        return jdbcClient.sql("SELECT * FROM order_item WHERE menu_item_id = :itemId AND order_id = :orderId")
                .param("orderId", orderId)
                .param("itemId", itemId)
                .query(OrderItem.class)
                .optional();
    }

    public List<OrderItem> findByOrderId(UUID orderId) {
        return jdbcClient.sql("SELECT * FROM order_item WHERE order_id = :id")
                .param("id", orderId)
                .query(OrderItem.class)
                .list();
    }

    public void create(OrderItem orderItem) {
        var updated = jdbcClient.sql("INSERT INTO order_item (order_id, menu_item_id, quantity) " +
                "VALUES (:orderId, :itemId, :quantity)")
                .param("orderId", orderItem.getOrderId())
                .param("itemId", orderItem.getMenuItemId())
                .param("quantity", orderItem.getQuantity())
                .update();

        Assert.state(updated == 1, "Failed to add item with ID: " + orderItem.getMenuItemId() + " to order with ID: " + orderItem.getOrderId());
    }

    public void update(OrderItem orderItem) {
        var updated = jdbcClient.sql("UPDATE order_item SET quantity = :quantity WHERE order_id = :orderId AND menu_item_id = :itemId")
                .param("orderId", orderItem.getOrderId())
                .param("itemId", orderItem.getMenuItemId())
                .param("quantity", orderItem.getQuantity())
                .update();

        Assert.state(updated == 1, "Failed to update item with ID: " + orderItem.getMenuItemId() + " in order with ID: " + orderItem.getOrderId());
    }

    public void delete(UUID itemId, UUID orderId) {
        var updated = jdbcClient.sql("DELETE FROM order_item WHERE order_id = :orderId AND menu_item_id = :itemId")
                .param("orderId", orderId)
                .param("itemId", itemId)
                .update();

        Assert.state(updated == 1, "Failed to delete item with ID: " + itemId + " from order with ID: " + orderId);
    }
}
