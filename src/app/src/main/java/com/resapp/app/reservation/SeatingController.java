package com.resapp.app.reservation;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

    @GetMapping
    public List<SeatingArea> getSeatingAreas(@PathVariable UUID restaurantId) {
        return seatingService.findByRestaurantId(restaurantId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createSeating(@PathVariable UUID restaurantId, @Valid @RequestBody SeatingAreaRequest request) {
        seatingService.createSeating(restaurantId, request);
    }

    @PutMapping("/{areaId}")
    @ResponseStatus(HttpStatus.OK)
    public void updateSeating(@PathVariable UUID restaurantId, @PathVariable UUID areaId,
                              @Valid @RequestBody SeatingAreaRequest request) {
        seatingService.updateSeatingArea(restaurantId, areaId, request);
    }

    @DeleteMapping("/{areaId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteSeating(@PathVariable UUID restaurantId, @PathVariable UUID areaId) {
        seatingService.deleteSeatingArea(restaurantId, areaId);
    }
}
