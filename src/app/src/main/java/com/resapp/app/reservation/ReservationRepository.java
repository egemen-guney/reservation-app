package com.resapp.app.reservation;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.time.OffsetTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ReservationRepository {
    private final JdbcClient jdbcClient;

    public ReservationRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<Reservation> findByResId(UUID resId) {
        return jdbcClient.sql("SELECT * FROM reservation WHERE res_id = :id")
                .param("id", resId)
                .query(Reservation.class)
                .optional();
    }

    public List<Reservation> findByRestaurantId(UUID restaurantId) {
        return jdbcClient.sql("SELECT * FROM reservation WHERE restaurant_id = :id")
                .param("id", restaurantId)
                .query(Reservation.class)
                .list();
    }

    public List<Reservation> findAllByCustomerId(UUID customerId) {
        return jdbcClient.sql("SELECT * FROM reservation WHERE customer_id = :id")
                .param("id", customerId)
                .query(Reservation.class)
                .list();
    }

    public Optional<Reservation> findByCustomerId(UUID customerId) {
        return jdbcClient.sql("SELECT * FROM reservation WHERE customer_id = :id")
                .param("id", customerId)
                .query(Reservation.class)
                .optional();
    }

    // used for UPDATE
    public int getOverlappingGuestCount(UUID areaId, LocalDate resDate, OffsetTime startTime, OffsetTime endTime, UUID excludeResId) {
        String sql = "SELECT COALESCE(SUM(num_people), 0) FROM reservation " +
                "WHERE area_id = :areaId " +
                "AND res_date = :resDate " +
                "AND status IN ('PENDING', 'CONFIRMED') " +
                "AND start_time < :endTime " +
                "AND end_time > :startTime ";

        if (excludeResId != null) {
            sql += "AND res_id != :excludeResId";
        }

        var statement = jdbcClient.sql(sql)
                .param("areaId", areaId)
                .param("resDate", resDate)
                .param("startTime", startTime)
                .param("endTime", endTime);

        if (excludeResId != null) {
            statement.param("excludeResId", excludeResId);
        }

        return statement.query(Integer.class).single();
    }

    // used for CREATE
    public int getOverlappingGuestCount(UUID areaId, LocalDate resDate, OffsetTime startTime, OffsetTime endTime) {
        return getOverlappingGuestCount(areaId, resDate, startTime, endTime, null);
    }

    public void create(Reservation res) {
        var updated = jdbcClient.sql("INSERT INTO reservation (res_id, restaurant_id, customer_id, area_id, res_date, start_time, " +
                "end_time, num_people, note, status) VALUES (:resId, :restaurantId, :customerId, :areaId, :date, :start, :end, :size, :note, :status)")
                .param("resId", res.getResId())
                .param("restaurantId", res.getRestaurantId())
                .param("customerId", res.getCustomerId())
                .param("areaId", res.getAreaId())
                .param("date", res.getResDate())
                .param("start", res.getStartTime())
                .param("end", res.getEndTime())
                .param("size", res.getNumPeople())
                .param("note", res.getNote())
                .param("status", res.getStatus().name())
                .update();

        Assert.state(updated == 1, "Failed to create reservation for customer with ID: " + res.getCustomerId() + " to restaurant with ID: " + res.getRestaurantId());
    }

    public void update(Reservation res) {
        var updated = jdbcClient.sql("UPDATE reservation SET area_id = :areaId, res_date = :date, start_time = :start, end_time = :end, num_people = :size, note = :note , status = :status " +
                        "WHERE res_id = :id")
                .param("id", res.getResId())
                .param("areaId", res.getAreaId())
                .param("date", res.getResDate())
                .param("start", res.getStartTime())
                .param("end", res.getEndTime())
                .param("size", res.getNumPeople())
                .param("note", res.getNote())
                .param("status", res.getStatus().name())
                .update();

        Assert.state(updated == 1, "Failed to update reservation with ID: " + res.getResId());
    }

    public void delete(UUID resId) {
        var updated = jdbcClient.sql("DELETE FROM reservation WHERE res_id = :id")
                .param("id", resId)
                .update();

        Assert.state(updated == 1, "Failed to delete reservation with ID: " + resId);
    }
}
