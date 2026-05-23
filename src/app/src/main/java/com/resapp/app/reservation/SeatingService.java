package com.resapp.app.reservation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SeatingService {
    private final SeatingAreaRepository seatingRepository;

    public SeatingService(SeatingAreaRepository seatingRepository) {
        this.seatingRepository = seatingRepository;
    }

    public List<SeatingArea> findByRestaurantId(UUID restaurantId) {
        return seatingRepository.findByRestaurantId(restaurantId);
    }

    @Transactional
    public void createSeating(UUID restaurantId, SeatingAreaRequest request) {
        SeatingArea newArea = SeatingArea.builder()
                .areaId(UUID.randomUUID())
                .restaurantId(restaurantId)
                .areaName(request.areaName())
                .capacity(request.capacity())
                .build();

        seatingRepository.create(newArea);
    }

    @Transactional
    public void updateSeatingArea(UUID restaurantId, UUID areaId, SeatingAreaRequest request) {
        SeatingArea existingArea = seatingRepository.findById(areaId)
                .orElseThrow(() -> new IllegalArgumentException("Seating area not found."));

        if (!existingArea.getRestaurantId().equals(restaurantId)) {
            throw new IllegalStateException("This seating area does not belong to your restaurant.");
        }

        existingArea.setAreaName(request.areaName());
        existingArea.setCapacity(request.capacity());

        seatingRepository.update(existingArea);
    }

    @Transactional
    public void deleteSeatingArea(UUID restaurantId, UUID areaId) {
        SeatingArea existingArea = seatingRepository.findById(areaId)
                .orElseThrow(() -> new IllegalArgumentException("Seating area not found."));

        if (!existingArea.getRestaurantId().equals(restaurantId)) {
            throw new IllegalStateException("This seating area does not belong to your restaurant.");
        }

        seatingRepository.delete(areaId);
    }
}
