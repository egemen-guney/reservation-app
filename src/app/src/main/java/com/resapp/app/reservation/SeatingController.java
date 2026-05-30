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
@RequestMapping("/api/restaurants/{restaurantId}/seating")
public class SeatingController {
    private final SeatingService seatingService;

    public SeatingController(SeatingService seatingService) {
        this.seatingService = seatingService;
    }

    /**
     * RESTAURANT CANNOT ACCESS OTHER RESTAURANTS' SEATING AREAS
     */
    @GetMapping
    public List<SeatingArea> getSeatingAreas(@PathVariable UUID restaurantId, @AuthenticationPrincipal AccountPrincipal principal) {
        return seatingService.findByRestaurantId(restaurantId, principal.getAccount());
    }

    /**
     * RESTAURANTS CANNOT CREATE SEATING AREAS ON BEHALF OF OTHER RESTAURANTS
     */
    @PreAuthorize("hasRole('RESTAURANT')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createSeating(@PathVariable UUID restaurantId, @Valid @RequestBody SeatingAreaRequest request,
                              @AuthenticationPrincipal AccountPrincipal principal) {
        seatingService.createSeating(restaurantId, request, principal.getAccount().getAccountId());
    }

    /**
     * RESTAURANTS CANNOT UPDATE SEATING AREAS ON BEHALF OF OTHER RESTAURANTS
     */
    @PreAuthorize("hasRole('RESTAURANT')")
    @PutMapping("/{areaId}")
    @ResponseStatus(HttpStatus.OK)
    public void updateSeating(@PathVariable UUID restaurantId, @PathVariable UUID areaId,
                              @Valid @RequestBody SeatingAreaRequest request,
                              @AuthenticationPrincipal AccountPrincipal principal) {
        seatingService.updateSeatingArea(restaurantId, areaId, request, principal.getAccount().getAccountId());
    }

    /**
     * RESTAURANTS CANNOT UPDATE SEATING AREAS ON BEHALF OF OTHER RESTAURANTS
     */
    @PreAuthorize("hasRole('RESTAURANT')")
    @DeleteMapping("/{areaId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteSeating(@PathVariable UUID restaurantId, @PathVariable UUID areaId,
                              @AuthenticationPrincipal AccountPrincipal principal) {
        seatingService.deleteSeatingArea(restaurantId, areaId, principal.getAccount().getAccountId());
    }
}
