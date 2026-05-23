package com.resapp.app.reservation;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SeatingAreaRepository {
    private final JdbcClient jdbcClient;

    public SeatingAreaRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<SeatingArea> findById(UUID areaId) {
        return jdbcClient.sql("SELECT * FROM seating_area WHERE area_id = :id")
                .param("id", areaId)
                .query(SeatingArea.class)
                .optional();
    }

    public List<SeatingArea> findByRestaurantId(UUID restaurantId) {
        return jdbcClient.sql("SELECT * FROM seating_area WHERE restaurant_id = :id")
                .param("id", restaurantId)
                .query(SeatingArea.class)
                .list();
    }

    public void create(SeatingArea area) {
        var updated = jdbcClient.sql("INSERT INTO seating_area (area_id, restaurant_id, area_name, capacity) " +
                "VALUES (:areaId, :restaurantId, :name, :capacity)")
                .param("areaId", area.getAreaId())
                .param("restaurantId", area.getRestaurantId())
                .param("name", area.getAreaName().name())
                .param("capacity", area.getCapacity())
                .update();

        Assert.state(updated == 1, "Failed to create " + area.getAreaName().name() + " seating for restaurant with ID: " + area.getRestaurantId());
    }

    public void update(SeatingArea area) {
        var updated = jdbcClient.sql("UPDATE seating_area SET area_name = :name, capacity = :capacity WHERE area_id = :id")
                .param("id", area.getAreaId())
                .param("name", area.getAreaName().name())
                .param("capacity", area.getCapacity())
                .update();

        Assert.state(updated == 1, "Failed to update " + area.getAreaName().name() + " seating for restaurant with ID: " + area.getRestaurantId());
    }

    public void delete(UUID areaId) {
        var updated = jdbcClient.sql("DELETE FROM seating_area WHERE area_id = :id")
                .param("id", areaId)
                .update();

        Assert.state(updated == 1, "Failed to delete seating area with ID: " + areaId);
    }
}
