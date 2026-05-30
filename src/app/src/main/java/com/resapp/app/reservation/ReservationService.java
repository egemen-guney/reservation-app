package com.resapp.app.reservation;

import com.resapp.app.account.Account;
import com.resapp.app.account.AccountRole;
import com.resapp.app.customer.Customer;
import com.resapp.app.customer.CustomerRepository;
import com.resapp.app.restaurant.Restaurant;
import com.resapp.app.restaurant.RestaurantRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetTime;
import java.util.List;
import java.util.UUID;

@Service
public class ReservationService {
    private final ReservationRepository resRepository;
    private final SeatingAreaRepository seatingRepository;
    private final RestaurantRepository restaurantRepository;
    private final CustomerRepository customerRepository;

    public ReservationService(ReservationRepository resRepository, SeatingAreaRepository seatingRepository, RestaurantRepository restaurantRepository, CustomerRepository customerRepository) {
        this.resRepository = resRepository;
        this.seatingRepository = seatingRepository;
        this.restaurantRepository = restaurantRepository;
        this.customerRepository = customerRepository;
    }

    public List<Reservation> getResByRestaurant(UUID restaurantId, Account account) {
        if (account.getRole() == AccountRole.RESTAURANT) {
            Restaurant myRestaurant = restaurantRepository.findByAccountId(account.getAccountId())
                    .orElseThrow(() -> new IllegalStateException("Restaurant not found."));

            if (!myRestaurant.getRestaurantId().equals(restaurantId)) {
                throw new AccessDeniedException("You are only authorized to view reservations for your own restaurant.");
            }
        }

        return resRepository.findByRestaurantId(restaurantId);
    }

    public List<Reservation> getResByCustomer(UUID customerId, Account account) {
        if (account.getRole() == AccountRole.CUSTOMER) {
            Customer myCustomer = customerRepository.findByAccountId(account.getAccountId())
                    .orElseThrow(() -> new IllegalStateException("Customer profile not found."));

            if (!myCustomer.getCustomerId().equals(customerId)) {
                throw new AccessDeniedException("You are only authorized to view reservations of your own.");
            }
        }
        return resRepository.findAllByCustomerId(customerId);
    }

    @Transactional
    public void makeReservation(UUID customerId, ReservationRequest request, UUID loggedinId) {
        Customer myCustomer = customerRepository.findByAccountId(loggedinId)
                .orElseThrow(() -> new IllegalStateException("Customer profile not found."));

        if (!myCustomer.getCustomerId().equals(customerId)) {
            throw new AccessDeniedException("You are only authorized to make reservations for your own.");
        }

        SeatingArea area = seatingRepository.findById(request.areaId())
                .orElseThrow(() -> new IllegalArgumentException("Seating area not found."));

        validateOperatingHours(area.getRestaurantId(), request.start(), request.end());

        int currentGuests = resRepository.getOverlappingGuestCount(
                area.getAreaId(),
                request.date(),
                request.start(),
                request.end()
        );

        if (currentGuests + request.size() > area.getCapacity()) {
            throw new IllegalStateException("Not enough capacity in this seating area for the requested time.");
        }

        Reservation newReservation = Reservation.builder()
                .resId(UUID.randomUUID())
                .restaurantId(area.getRestaurantId())
                .customerId(customerId)
                .areaId(area.getAreaId())
                .resDate(request.date())
                .startTime(request.start())
                .endTime(request.end())
                .numPeople(request.size())
                .note(request.note())
                .status(ReservationStatus.PENDING)
                .build();

        resRepository.create(newReservation);
    }

    @Transactional
    public void cancelReservation(UUID customerId, UUID resId, UUID loggedinId) {
        Customer myCustomer = customerRepository.findByAccountId(loggedinId)
                .orElseThrow(() -> new IllegalStateException("Customer profile not found."));

        if (!myCustomer.getCustomerId().equals(customerId)) {
            throw new AccessDeniedException("You are only authorized to update reservations of your own.");
        }

        Reservation existingRes = resRepository.findByResId(resId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found."));

        if (!existingRes.getCustomerId().equals(customerId)) {
            throw new IllegalStateException("This reservation does not belong to you.");
        }

        // Can't cancel something already completed or cancelled
        if (existingRes.getStatus() == ReservationStatus.CANCELLED || existingRes.getStatus() == ReservationStatus.COMPLETED) {
            throw new IllegalStateException("This reservation is either already cancelled or completed.");
        }

        existingRes.setStatus(ReservationStatus.CANCELLED);
        resRepository.update(existingRes);
    }

    @Transactional
    public void updateReservation(UUID resId, UUID customerId, ReservationRequest request, UUID loggedinId) {
        Customer myCustomer = customerRepository.findByAccountId(loggedinId)
                .orElseThrow(() -> new IllegalStateException("Customer profile not found."));

        if (!myCustomer.getCustomerId().equals(customerId)) {
            throw new AccessDeniedException("You are only authorized to update reservations of your own.");
        }

        Reservation existingRes = resRepository.findByResId(resId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found."));

        if (!existingRes.getCustomerId().equals(customerId)) {
            throw new IllegalStateException("This reservation does not belong to you.");
        }

        if (existingRes.getStatus() != ReservationStatus.PENDING && existingRes.getStatus() != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("Only active (pending or confirmed) reservations can be updated.");
        }

        SeatingArea area = seatingRepository.findById(request.areaId())
                .orElseThrow(() -> new IllegalArgumentException("Seating area not found."));

        validateOperatingHours(area.getRestaurantId(), request.start(), request.end());

        int currentGuests = resRepository.getOverlappingGuestCount(
                area.getAreaId(),
                request.date(),
                request.start(),
                request.end(),
                existingRes.getResId()
        );

        if (currentGuests + request.size() > area.getCapacity()) {
            throw new IllegalStateException("Not enough capacity in this seating area for the newly requested time.");
        }

        existingRes.setAreaId(request.areaId());
        existingRes.setResDate(request.date());
        existingRes.setStartTime(request.start());
        existingRes.setEndTime(request.end());
        existingRes.setNumPeople(request.size());
        existingRes.setNote(request.note());
        resRepository.update(existingRes);
    }

    @Transactional
    public void updateReservationStatus(UUID restaurantId, UUID resId, ReservationStatus status, UUID loggedinId) {
        Restaurant myRestaurant = restaurantRepository.findByAccountId(loggedinId)
                .orElseThrow(() -> new IllegalStateException("Restaurant profile not found."));

        if (!myRestaurant.getRestaurantId().equals(restaurantId)) {
            throw new AccessDeniedException("You are only authorized to update reservations for your own restaurant.");
        }

        Reservation existingRes = resRepository.findByResId(resId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found."));

        if (!existingRes.getRestaurantId().equals(restaurantId)) {
            throw new IllegalStateException("You do not have permission to modify this reservation.");
        }

        existingRes.setStatus(status);
        resRepository.update(existingRes);
    }

    @Transactional
    public void deleteReservation(UUID resId) {
        resRepository.delete(resId);
    }

    private void validateOperatingHours(UUID restaurantId, OffsetTime reqStart, OffsetTime reqEnd) {
        Restaurant restaurant = restaurantRepository.findByRestaurantId(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found."));

        if (!restaurant.isOpen()) {
            throw new IllegalStateException("This restaurant is currently closed and not accepting reservations.");
        }

        java.time.LocalTime localReqStart = reqStart.toLocalTime();
        java.time.LocalTime localReqEnd = reqEnd.toLocalTime();

        if (localReqStart.isBefore(restaurant.getOpeningHours()) || localReqEnd.isAfter(restaurant.getClosingHours())) {
            throw new IllegalStateException("Requested time falls outside of the restaurant's operating hours ("
                    + restaurant.getOpeningHours() + " to " + restaurant.getClosingHours() + ").");
        }
    }
}
