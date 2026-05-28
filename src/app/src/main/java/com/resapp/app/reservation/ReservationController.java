package com.resapp.app.reservation;

import com.resapp.app.account.AccountPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    private final ReservationService resService;

    public ReservationController(ReservationService resService) {
        this.resService = resService;
    }

    // --CUSTOMERS--
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/customers/{customerId}")
    public List<Reservation> getCustomerReservations(@PathVariable UUID customerId) {
        return resService.getResByCustomer(customerId);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/customers/{customerId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void makeReservation(@PathVariable UUID customerId, @Valid @RequestBody ReservationRequest request) {
        resService.makeReservation(customerId, request);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PutMapping("/customers/{customerId}/{resId}")
    @ResponseStatus(HttpStatus.OK)
    public void updateReservation(@PathVariable UUID customerId, @PathVariable UUID resId,
                                  @Valid @RequestBody ReservationRequest request) {
        resService.updateReservation(resId, customerId, request);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @DeleteMapping("/customers/{customerId}/{resId}")
    @ResponseStatus(HttpStatus.OK)
    public void cancelReservation(@PathVariable UUID customerId, @PathVariable UUID resId) {
        resService.cancelReservation(customerId, resId);
    }
    // --CUSTOMERS--

    // --RESTAURANTS--
    @PreAuthorize("hasRole('RESTAURANT')")
    @GetMapping("/restaurants/{restaurantId}")
    public List<Reservation> getRestaurantReservations(@PathVariable UUID restaurantId) {
        return resService.getResByRestaurant(restaurantId);
    }

    @PreAuthorize("hasRole('RESTAURANT')")
    @PatchMapping("/restaurants/{restaurantId}/{resId}/status")
    @ResponseStatus(HttpStatus.OK)
    public void updateReservationStatus(
            @PathVariable UUID restaurantId,
            @PathVariable UUID resId,
            @RequestParam ReservationStatus status) {
        resService.updateReservationStatus(restaurantId, resId, status);
    }
    // --RESTAURANTS--
}
