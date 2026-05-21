package com.resapp.app.menu;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.Optional;
import java.util.UUID;

@Repository
public class MenuRepository {
    private final JdbcClient jdbcClient;

    public MenuRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<Menu> findById(UUID menuId) {
        return jdbcClient.sql("SELECT * FROM menu WHERE menu_id = :id")
                .param("id", menuId)
                .query(Menu.class)
                .optional();
    }

    public void create(Menu menu) {
        var updated = jdbcClient.sql("INSERT INTO menu (menu_id) VALUES (:id)")
                .param("id", menu.getMenuId())
                .update();

        Assert.state(updated == 1, "Failed to create menu with ID: " + menu.getMenuId());
    }

    public void delete(UUID menuId) {
        var updated = jdbcClient.sql("DELETE FROM menu WHERE menu_id = :id")
                .param("id", menuId)
                .update();

        Assert.state(updated == 1, "Failed to delete menu with ID: " + menuId);
    }
}
