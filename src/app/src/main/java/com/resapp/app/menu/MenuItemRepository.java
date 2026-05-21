package com.resapp.app.menu;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class MenuItemRepository {
    private final JdbcClient jdbcClient;

    public MenuItemRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    // finds all menu items that belong to a given menu
    public List<MenuItem> findByMenuId(UUID menuId) {
        return jdbcClient.sql("SELECT * FROM menu_item WHERE menu_id = :id")
                .param("id", menuId)
                .query(MenuItem.class)
                .list();
    }

    public Optional<MenuItem> findByItemId(UUID itemId) {
        return jdbcClient.sql("SELECT * FROM menu_item WHERE menu_item_id = :id")
                .param("id", itemId)
                .query(MenuItem.class)
                .optional();
    }

    public void create(MenuItem menuItem) {
        var updated = jdbcClient.sql("INSERT INTO menu_item (menu_item_id, menu_id, name, description, category, is_available, price) " +
                "VALUES (:menuItemId, :menuId, :name, :description, :category, :isAvailable, :price)")
                .param("menuItemId", menuItem.getMenuItemId())
                .param("menuId", menuItem.getMenuId())
                .param("name", menuItem.getName())
                .param("description", menuItem.getDescription())
                .param("category", menuItem.getCategory())
                .param("isAvailable", menuItem.isAvailable())
                .param("price", menuItem.getPrice())
                .update();

        Assert.state(updated == 1, "Failed to create menu item " + menuItem.getName());
    }

    public void update(MenuItem menuItem) {
        var updated = jdbcClient.sql("UPDATE menu_item SET name = :name, description = :description, category = :category, is_available = :isAvailable, price = :price, satisfaction = :satisfaction " +
                        "WHERE menu_item_id = :id")
                .param("id", menuItem.getMenuItemId())
                .param("name", menuItem.getName())
                .param("description", menuItem.getDescription())
                .param("category", menuItem.getCategory())
                .param("isAvailable", menuItem.isAvailable())
                .param("price", menuItem.getPrice())
                .param("satisfaction", menuItem.getSatisfaction())
                .update();

        Assert.state(updated == 1, "Failed to update menu item with ID: " + menuItem.getMenuItemId());
    }

    public void delete(UUID menuItemId) {
        var updated = jdbcClient.sql("DELETE FROM menu_item WHERE menu_item_id = :id")
                .param("id", menuItemId)
                .update();

        Assert.state(updated == 1, "Failed to delete menu item with ID: " + menuItemId);
    }
}
