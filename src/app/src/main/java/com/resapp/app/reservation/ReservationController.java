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
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @GetMapping("/customers/{customerId}")
    public List<Reservation> getCustomerReservations(@PathVariable UUID customerId, @AuthenticationPrincipal AccountPrincipal principal) {
        return resService.getResByCustomer(customerId, principal.getAccount());
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/customers/{customerId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void makeReservation(@PathVariable UUID customerId, @Valid @RequestBody ReservationRequest request,
                                @AuthenticationPrincipal AccountPrincipal principal) {
        resService.makeReservation(customerId, request, principal.getAccount().getAccountId());
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PutMapping("/customers/{customerId}/{resId}")
    @ResponseStatus(HttpStatus.OK)
    public void updateReservation(@PathVariable UUID customerId, @PathVariable UUID resId,
                                  @Valid @RequestBody ReservationRequest request,
                                  @AuthenticationPrincipal AccountPrincipal principal) {
        resService.updateReservation(resId, customerId, request, principal.getAccount().getAccountId());
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @DeleteMapping("/customers/{customerId}/{resId}")
    @ResponseStatus(HttpStatus.OK)
    public void cancelReservation(@PathVariable UUID customerId, @PathVariable UUID resId,
                                  @AuthenticationPrincipal AccountPrincipal principal) {
        resService.cancelReservation(customerId, resId, principal.getAccount().getAccountId());
    }
    // --CUSTOMERS--

    // --RESTAURANTS--
    @PreAuthorize("hasAnyRole('ADMIN','RESTAURANT')")
    @GetMapping("/restaurants/{restaurantId}")
    public List<Reservation> getRestaurantReservations(@PathVariable UUID restaurantId, @AuthenticationPrincipal AccountPrincipal principal) {
        return resService.getResByRestaurant(restaurantId, principal.getAccount());
    }

    @PreAuthorize("hasRole('RESTAURANT')")
    @PatchMapping("/restaurants/{restaurantId}/{resId}/status")
    @ResponseStatus(HttpStatus.OK)
    public void updateReservationStatus(
            @PathVariable UUID restaurantId,
            @PathVariable UUID resId,
            @RequestParam ReservationStatus status,
            @AuthenticationPrincipal AccountPrincipal principal) {
        resService.updateReservationStatus(restaurantId, resId, status, principal.getAccount().getAccountId());
    }
    // --RESTAURANTS--

    // --ADMINS--
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{resId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReservation(@PathVariable UUID resId) {
        resService.deleteReservation(resId);
    }
    // -- ADMINS--
}
