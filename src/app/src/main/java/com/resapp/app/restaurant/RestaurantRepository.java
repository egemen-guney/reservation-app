package com.resapp.app.restaurant;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class RestaurantRepository {
    private final JdbcClient jdbcClient;

    public RestaurantRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<Restaurant> findByRestaurantId(UUID restaurantId) {
        return jdbcClient.sql("SELECT * FROM restaurant WHERE restaurant_id = :id")
                .param("id", restaurantId)
                .query(Restaurant.class)
                .optional();
    }

    public Optional<Restaurant> findByAccountId(UUID accountId) {
        return jdbcClient.sql("SELECT * FROM restaurant WHERE account_id = :id")
                .param("id", accountId)
                .query(Restaurant.class)
                .optional();
    }

    public List<Restaurant> findAll() {
        return jdbcClient.sql("SELECT * FROM restaurant")
                .query(Restaurant.class)
                .list();
    }

    public void create(Restaurant restaurant) {
        var updated = jdbcClient.sql("INSERT INTO restaurant (restaurant_id, account_id, name, address_id, bus_phone, menu_id, stars, opening_hours, closing_hours, is_open) " +
                "VALUES (:restaurantId, :accountId, :name, :addressId, :busPhone, :menuId, :stars, :oHours, :cHours, :isOpen)")
                .param("restaurantId", restaurant.getRestaurantId())
                .param("accountId", restaurant.getAccountId())
                .param("name", restaurant.getName())
                .param("addressId", restaurant.getAddressId())
                .param("busPhone", restaurant.getBusPhone())
                .param("menuId", restaurant.getMenuId())
                .param("stars", restaurant.getStars())
                .param("oHours", restaurant.getOpeningHours())
                .param("cHours", restaurant.getClosingHours())
                .param("isOpen", restaurant.isOpen())
                .update();

        Assert.state(updated == 1, "Failed to create restaurant profile for account with ID: " + restaurant.getAccountId());
    }

    public void update(Restaurant restaurant) {
        var updated = jdbcClient.sql("UPDATE restaurant SET name = :name, address_id = :addressId, bus_phone = :busPhone, menu_id = :menuId, stars = :stars, opening_hours = :oHours, closing_hours = :cHours, is_open = :isOpen " +
                "WHERE restaurant_id = :id")
                .param("id", restaurant.getRestaurantId())
                .param("name", restaurant.getName())
                .param("addressId", restaurant.getAddressId())
                .param("busPhone", restaurant.getBusPhone())
                .param("menuId", restaurant.getMenuId())
                .param("stars", restaurant.getStars())
                .param("oHours", restaurant.getOpeningHours())
                .param("cHours", restaurant.getClosingHours())
                .param("isOpen", restaurant.isOpen())
                .update();

        Assert.state(updated == 1, "Failed to update restaurant with ID: " + restaurant.getRestaurantId());
    }

    public void delete(UUID restaurantId) {
        var updated = jdbcClient.sql("DELETE FROM restaurant WHERE restaurant_id = :id")
                .param("id", restaurantId)
                .update();

        Assert.state(updated == 1, "Failed to delete restaurant profile with ID: " + restaurantId);
    }
}
