package com.resapp.app.order;

import com.resapp.app.menu.MenuItem;
import com.resapp.app.menu.MenuItemRepository;
import com.resapp.app.reservation.Reservation;
import com.resapp.app.reservation.ReservationRepository;
import com.resapp.app.reservation.ReservationStatus;
import com.resapp.app.restaurant.Restaurant;
import com.resapp.app.restaurant.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {
    private final ReservationRepository resRepository;
    private final MenuItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final RestaurantRepository restaurantRepository;

    public OrderService(ReservationRepository resRepository, MenuItemRepository itemRepository,
                        OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                        RestaurantRepository restaurantRepository) {
        this.resRepository = resRepository;
        this.itemRepository = itemRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.restaurantRepository = restaurantRepository;
    }

    public List<Order> getCustomerOrders(UUID customerId) {
        return orderRepository.findAllByCustomerId(customerId);
    }

    public Optional<OrderItem> findItem(UUID menuItemId, UUID orderId) {
        return orderItemRepository.findByItemId(menuItemId, orderId);
    }

    public List<OrderItem> findOrderItems(UUID orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    @Transactional
    public void placeOrder(UUID customerId, OrderRequest request) {
        Reservation res = resRepository.findByResId(request.resId())
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found."));

        if (!res.getCustomerId().equals(customerId)) {
            throw new IllegalStateException("This reservation does not belong to you.");
        }

        if (res.getStatus() == ReservationStatus.CANCELLED || res.getStatus() == ReservationStatus.COMPLETED) {
            throw new IllegalStateException("You can only place an order for an active reservation (pending or confirmed).");
        }

        if (orderRepository.findByResId(request.resId()).isPresent()) {
            throw new IllegalStateException("An order has already been placed for this reservation.");
        }

        Restaurant restaurant = restaurantRepository.findByRestaurantId(res.getRestaurantId())
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found."));
        UUID activeMenuId = restaurant.getMenuId();

        BigDecimal sumTotal = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.items()) {
            MenuItem menuItem = itemRepository.findByItemId(itemRequest.menuItemId())
                    .orElseThrow(() -> new IllegalArgumentException("Menu item not found: " + itemRequest.menuItemId()));

            if (!menuItem.getMenuId().equals(activeMenuId)) {
                throw new IllegalStateException("Item '" + menuItem.getName() + "' does not belong to this restaurant's menu.");
            }

            if (!menuItem.isAvailable()) {
                throw new IllegalStateException("Item '" + menuItem.getName() + "' is currently unavailable.");
            }

            // Multiply the DB price by the requested quantity securely
            BigDecimal itemTotal = menuItem.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity()));
            sumTotal = sumTotal.add(itemTotal);
        }

        UUID newOrderId = UUID.randomUUID();

        Order newOrder = Order.builder()
                .orderId(newOrderId)
                .resId(request.resId())
                .customerId(customerId)
                .totalPrice(sumTotal)
                .ccNum(request.ccNum())
                .status(OrderStatus.PAID) // Assuming payment succeeds upon submission
                .build();

        orderRepository.create(newOrder);

        // For the Order Items
        for (OrderItemRequest itemRequest : request.items()) {
            OrderItem orderItem = OrderItem.builder()
                    .orderId(newOrderId)
                    .menuItemId(itemRequest.menuItemId())
                    .quantity(itemRequest.quantity())
                    .build();

            orderItemRepository.create(orderItem);
        }
    }

    @Transactional
    public void refundOrder(UUID customerId, UUID orderId) {
        Order existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found."));

        if (!existingOrder.getCustomerId().equals(customerId)) {
            throw new IllegalStateException("This order does not belong to you.");
        }

        if (existingOrder.getStatus() == OrderStatus.REFUNDED) {
            throw new IllegalStateException("Order is already refunded.");
        }

        existingOrder.setStatus(OrderStatus.REFUNDED);
        orderRepository.update(existingOrder);
    }
}
