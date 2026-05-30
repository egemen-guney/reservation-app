package com.resapp.app.reservation;

import com.resapp.app.account.Account;
import com.resapp.app.account.AccountRole;
import com.resapp.app.restaurant.Restaurant;
import com.resapp.app.restaurant.RestaurantRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SeatingService {
    private final SeatingAreaRepository seatingRepository;
    private final RestaurantRepository restaurantRepository;

    public SeatingService(SeatingAreaRepository seatingRepository, RestaurantRepository restaurantRepository) {
        this.seatingRepository = seatingRepository;
        this.restaurantRepository = restaurantRepository;
    }

    public List<SeatingArea> findByRestaurantId(UUID restaurantId, Account account) {
        if (account.getRole() == AccountRole.RESTAURANT) {
            Restaurant myRestaurant = restaurantRepository.findByAccountId(account.getAccountId())
                    .orElseThrow(() -> new IllegalStateException("Restaurant profile not found."));

            if (!myRestaurant.getRestaurantId().equals(restaurantId)) {
                throw new AccessDeniedException("You are only authorized to view seating areas of your own restaurant.");
            }
        }
        return seatingRepository.findByRestaurantId(restaurantId);
    }

    @Transactional
    public void createSeating(UUID restaurantId, SeatingAreaRequest request, UUID loggedinId) {
        Restaurant myRestaurant = restaurantRepository.findByAccountId(loggedinId)
                .orElseThrow(() -> new IllegalStateException("Restaurant profile not found."));

        if (!myRestaurant.getRestaurantId().equals(restaurantId)) {
            throw new AccessDeniedException("You are only authorized to view seating areas of your own restaurant.");
        }

        SeatingArea newArea = SeatingArea.builder()
                .areaId(UUID.randomUUID())
                .restaurantId(restaurantId)
                .areaName(request.areaName())
                .capacity(request.capacity())
                .build();

        seatingRepository.create(newArea);
    }

    @Transactional
    public void updateSeatingArea(UUID restaurantId, UUID areaId, SeatingAreaRequest request, UUID loggedinId) {
        Restaurant myRestaurant = restaurantRepository.findByAccountId(loggedinId)
                .orElseThrow(() -> new IllegalStateException("Restaurant profile not found."));

        if (!myRestaurant.getRestaurantId().equals(restaurantId)) {
            throw new AccessDeniedException("You are only authorized to view seating areas of your own restaurant.");
        }

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
    public void deleteSeatingArea(UUID restaurantId, UUID areaId, UUID loggedinId) {
        Restaurant myRestaurant = restaurantRepository.findByAccountId(loggedinId)
                .orElseThrow(() -> new IllegalStateException("Restaurant profile not found."));

        if (!myRestaurant.getRestaurantId().equals(restaurantId)) {
            throw new AccessDeniedException("You are only authorized to view seating areas of your own restaurant.");
        }

        SeatingArea existingArea = seatingRepository.findById(areaId)
                .orElseThrow(() -> new IllegalArgumentException("Seating area not found."));

        if (!existingArea.getRestaurantId().equals(restaurantId)) {
            throw new IllegalStateException("This seating area does not belong to your restaurant.");
        }

        seatingRepository.delete(areaId);
    }
}
