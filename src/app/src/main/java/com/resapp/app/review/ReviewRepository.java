package com.resapp.app.review;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ReviewRepository {
    private final JdbcClient jdbcClient;

    public ReviewRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<Review> findById(UUID reviewId) {
        return jdbcClient.sql("SELECT * FROM review WHERE review_id = :id")
                .param("id", reviewId)
                .query(Review.class)
                .optional();
    }

    public List<Review> findByCustomerId(UUID customerId) {
        return jdbcClient.sql("SELECT * FROM review WHERE customer_id = :id")
                .param("id", customerId)
                .query(Review.class)
                .list();
    }

    public List<Review> findByRestaurantId(UUID restaurantId) {
        return jdbcClient.sql("SELECT * FROM review WHERE restaurant_id = :id")
                .param("id", restaurantId)
                .query(Review.class)
                .list();
    }

    public void create(Review review) {
        var updated = jdbcClient.sql("INSERT INTO review VALUES (:reviewId, :restaurantId, :customerId, :stars, :comment, :createdAt)")
                .param("reviewId", review.getReviewId())
                .param("restaurantId", review.getRestaurantId())
                .param("customerId", review.getCustomerId())
                .param("stars", review.getStars())
                .param("comment", review.getComment())
                .param("createdAt", review.getCreatedAt())
                .update();

        Assert.state(updated == 1, "Failed to add review from customer with ID: " + review.getCustomerId() + " to restaurant with ID: " + review.getRestaurantId());
    }

    public void delete(UUID reviewId) {
        var updated = jdbcClient.sql("DELETE FROM review WHERE review_id = :id")
                .param("id", reviewId)
                .update();

        Assert.state(updated == 1, "Failed to delete review with ID: " + reviewId);
    }

    public Optional<Review> findByCustomerIdAndRestaurantId(UUID customerId, UUID restaurantId) {
        return jdbcClient.sql("SELECT * FROM review WHERE customer_id = :customerId AND restaurant_id = :restaurantId")
                .param("customerId", customerId)
                .param("restaurantId", restaurantId)
                .query(Review.class)
                .optional();
    }
}
