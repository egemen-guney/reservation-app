package com.resapp.app.review;

import com.resapp.app.account.Account;
import com.resapp.app.account.AccountRole;
import com.resapp.app.customer.Customer;
import com.resapp.app.customer.CustomerRepository;
import com.resapp.app.reservation.Reservation;
import com.resapp.app.reservation.ReservationRepository;
import com.resapp.app.restaurant.Restaurant;
import com.resapp.app.restaurant.RestaurantRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final CustomerRepository customerRepository;
    private final RestaurantRepository restaurantRepository;
    private final ReservationRepository reservationRepository;

    public ReviewService(ReviewRepository reviewRepository, CustomerRepository customerRepository, RestaurantRepository restaurantRepository, ReservationRepository reservationRepository) {
        this.reviewRepository = reviewRepository;
        this.customerRepository = customerRepository;
        this.restaurantRepository = restaurantRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<Review> getReviewsByCustomer(UUID customerId, Account account) {
        if (account.getRole() == AccountRole.CUSTOMER) {
            Customer myCustomer = customerRepository.findByAccountId(account.getAccountId())
                    .orElseThrow(() -> new IllegalStateException("Customer profile not found."));

            if (!myCustomer.getCustomerId().equals(customerId)) {
                throw new AccessDeniedException("You are only authorized to view reviews of your own.");
            }
        }
        return reviewRepository.findByCustomerId(customerId);
    }

    public List<Review> getReviewsByRestaurant(UUID restaurantId, Account account) {
        if (account.getRole() == AccountRole.RESTAURANT) {
            Restaurant myRestaurant = restaurantRepository.findByAccountId(account.getAccountId())
                    .orElseThrow(() -> new IllegalStateException("Restaurant profile not found."));

            if (!myRestaurant.getRestaurantId().equals(restaurantId)) {
                throw new AccessDeniedException("You are only authorized to view reviews for your own restaurant.");
            }
        }

        return reviewRepository.findByRestaurantId(restaurantId);
    }

    @Transactional
    public void review(UUID customerId, ReviewRequest request, UUID loggedinId) {
        Customer myCustomer = customerRepository.findByAccountId(loggedinId)
                .orElseThrow(() -> new IllegalStateException("No customer profile found for this account."));

        if (!myCustomer.getCustomerId().equals(customerId)) {
            throw new AccessDeniedException("You are only authorized to review vendors from your own profile.");
        }

        Reservation myRes = reservationRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new IllegalStateException("Past reservation not found."));

        if (!myRes.getRestaurantId().equals(request.restaurantId())) {
            throw new AccessDeniedException("You are only authorized to review restaurants you've been to before.");
        }

        Review newReview = Review.builder()
                .reviewId(UUID.randomUUID())
                .restaurantId(request.restaurantId())
                .customerId(myCustomer.getCustomerId())
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
