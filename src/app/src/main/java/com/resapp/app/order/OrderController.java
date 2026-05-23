package com.resapp.app.order;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers/{customerId}/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<Order> getCustomerOrders(@PathVariable UUID customerId) {
        return orderService.getCustomerOrders(customerId);
    }

    @GetMapping("/{orderId}/items")
    public List<OrderItem> getOrderItems(@PathVariable UUID customerId, @PathVariable UUID orderId) {
        // Note: we would also verify that this orderId actually belongs to this customer before returning the items
        return orderService.findOrderItems(orderId);
    }

    @GetMapping("/{orderId}/items/{itemId}")
    public OrderItem getItem(@PathVariable UUID customerId, @PathVariable UUID orderId,
                                             @PathVariable UUID itemId) {
        // Note: we would also verify that this orderId actually belongs to this customer before returning the items
        return orderService.findItem(itemId, orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order item not found."));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void placeOrder(@PathVariable UUID customerId, @Valid @RequestBody OrderRequest request) {
        orderService.placeOrder(customerId, request);
    }

    @PatchMapping("/{orderId}/refund")
    @ResponseStatus(HttpStatus.OK)
    public void refundOrder(
            @PathVariable UUID customerId,
            @PathVariable UUID orderId) {
        orderService.refundOrder(customerId, orderId);
    }
}
