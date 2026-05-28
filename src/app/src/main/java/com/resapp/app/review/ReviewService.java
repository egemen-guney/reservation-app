package com.resapp.app.review;

import com.resapp.app.customer.Customer;
import com.resapp.app.customer.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final CustomerRepository customerRepository;

    public ReviewService(ReviewRepository reviewRepository, CustomerRepository customerRepository) {
        this.reviewRepository = reviewRepository;
        this.customerRepository = customerRepository;
    }

    public List<Review> getReviewsByCustomer(UUID customerId) {
        return reviewRepository.findByCustomerId(customerId);
    }

    public List<Review> getReviewsByRestaurant(UUID restaurantId) {
        return reviewRepository.findByRestaurantId(restaurantId);
    }

    @Transactional
    public void review(UUID customerId, ReviewRequest request) {
        Customer customer = customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new IllegalStateException("No customer profile found for this account."));

        Review newReview = Review.builder()
                .reviewId(UUID.randomUUID())
                .restaurantId(request.restaurantId())
                .customerId(customerId)
                .stars(request.stars())
                .comment(request.comment())
                .build();

        reviewRepository.create(newReview);
    }

    @Transactional
    public void removeReview(UUID reviewId) {
        reviewRepository.delete(reviewId);
    }
}
