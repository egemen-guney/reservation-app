package com.resapp.app.review;

import com.resapp.app.account.AccountPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @GetMapping("/customers/{customerId}")
    public List<Review> getCustomerReviews(@PathVariable UUID customerId, @AuthenticationPrincipal AccountPrincipal principal) {
        return reviewService.getReviewsByCustomer(customerId, principal.getAccount());
    }

    /**
     * RESTAURANTS CANNOT VIEW OTHER RESTAURANTS' REVIEWS
     */
    @GetMapping("/restaurants/{restaurantId}")
    public List<Review> getRestaurantReviews(@PathVariable UUID restaurantId, @AuthenticationPrincipal AccountPrincipal principal) {
        return reviewService.getReviewsByRestaurant(restaurantId, principal.getAccount());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT')") // Admins and restaurant owners can view specific reviews
    @GetMapping("/customers/{customerId}/restaurants/{restaurantId}")
    public ResponseEntity<Review> checkExistingReview(@PathVariable UUID customerId, @PathVariable UUID restaurantId,
                                                      @AuthenticationPrincipal AccountPrincipal principal) {
        return reviewService.getCustomerReviewForRestaurant(customerId, restaurantId, principal.getAccount())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    /**
     * CUSTOMERS CANNOT REVIEW ON BEHALF OF OTHER CUSTOMERS
     */
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/{customerId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void review(@PathVariable UUID customerId, @Valid @RequestBody ReviewRequest request,
                       @AuthenticationPrincipal AccountPrincipal principal) {
        reviewService.review(customerId, request, principal.getAccount().getAccountId());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @DeleteMapping("/{reviewId}")
    @ResponseStatus(HttpStatus.OK)
    public void removeReview(@PathVariable UUID reviewId, @AuthenticationPrincipal AccountPrincipal principal) {
        reviewService.removeReview(reviewId, principal.getAccount());
    }
}
