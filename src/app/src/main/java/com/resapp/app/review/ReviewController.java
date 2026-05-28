package com.resapp.app.review;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * ONLY ADMINS AND CUSTOMERS SHOULD BE ABLE TO ACCESS
     */
    @GetMapping("/customers/{customerId}")
    public List<Review> getCustomerReviews(@PathVariable UUID customerId) {
        return reviewService.getReviewsByCustomer(customerId);
    }

    /**
     * RESTAURANTS CANNOT VIEW OTHER RESTAURANTS' REVIEWS
     */
    @GetMapping("/restaurants/{restaurantId}")
    public List<Review> getRestaurantReviwes(@PathVariable UUID restaurantId) {
        return reviewService.getReviewsByRestaurant(restaurantId);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/{customerId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void review(@PathVariable UUID customerId, @Valid @RequestBody ReviewRequest request) {
        reviewService.review(customerId, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{reviewId}")
    @ResponseStatus(HttpStatus.OK)
    public void removeReview(@PathVariable UUID reviewId) {
        reviewService.removeReview(reviewId);
    }
}
