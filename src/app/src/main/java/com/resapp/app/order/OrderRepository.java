package com.resapp.app.order;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class OrderRepository {
    private final JdbcClient jdbcClient;

    public OrderRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<Order> findById(UUID orderId) {
        return jdbcClient.sql("SELECT * FROM orders WHERE order_id = :id")
                .param("id", orderId)
                .query(Order.class)
                .optional();
    }

    public Optional<Order> findByResId(UUID resId) {
        return jdbcClient.sql("SELECT * FROM orders WHERE res_id = :id")
                .param("id", resId)
                .query(Order.class)
                .optional();
    }
/*
    public Optional<Order> findByCustomerId(UUID customerId) {
        return jdbcClient.sql("SELECT * FROM orders WHERE customer_id = :id")
                .param("id", customerId)
                .query(Order.class)
                .optional();
    }*/

    public List<Order> findAllByCustomerId(UUID customerId) {
        return jdbcClient.sql("SELECT * FROM orders WHERE customer_id = :id")
                .param("id", customerId)
                .query(Order.class)
                .list();
    }

    public void create(Order order) {
        var updated = jdbcClient.sql("INSERT INTO orders (order_id, res_id, customer_id, total_price, cc_num, status) " +
                "VALUES (:orderId, :resId, :customerId, :totalPrice, :ccNum, :status)")
                .param("orderId", order.getOrderId())
                .param("resId", order.getResId())
                .param("customerId", order.getCustomerId())
                .param("totalPrice", order.getTotalPrice())
                .param("ccNum", order.getCcNum())
                .param("status", order.getStatus().name())
                .update();

        Assert.state(updated == 1, "Failed to create order for reservation with ID: " + order.getResId());
    }

    public void update(Order order) {
        var updated = jdbcClient.sql("UPDATE orders SET total_price = :totalPrice, cc_num = :ccNum, status = :status " +
                        "WHERE order_id = :id")
                .param("id", order.getOrderId())
                .param("totalPrice", order.getTotalPrice())
                .param("ccNum", order.getCcNum())
                .param("status", order.getStatus().name())
                .update();

        Assert.state(updated == 1, "Failed to update order with ID: " + order.getResId());
    }

    public void delete(UUID orderId) {
        var updated = jdbcClient.sql("DELETE FROM orders WHERE order_id = :id")
                .param("id", orderId)
                .update();

        Assert.state(updated == 1, "Failed to delete order with ID: " + orderId);
    }
}
