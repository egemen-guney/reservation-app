package com.resapp.app.order;

import com.resapp.app.account.AccountPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    /**
     * ONLY ADMINS AND CUSTOMERS SHOULD BE ABLE TO ACCESS
     * AND CUSTOMER CANNOT VIEW OTHER CUSTOMERS' PAST ORDERS
     */
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    @GetMapping
    public List<Order> getCustomerOrders(@PathVariable UUID customerId, @AuthenticationPrincipal AccountPrincipal principal) {
        return orderService.getCustomerOrders(customerId, principal.getAccount());
    }

    /**
     * CUSTOMERS CANNOT VIEW OTHER CUSTOMERS' ORDER DETAILS
     * RESTAURANT CAN ALSO NOT VIEW OTHER RESTAURANTS' RESERVATIONS' ORDER DETAILS
     */
    @GetMapping("/{orderId}/items")
    public List<OrderItem> getOrderItems(@PathVariable UUID customerId, @PathVariable UUID orderId,
                                         @AuthenticationPrincipal AccountPrincipal principal) {
        return orderService.findOrderItems(customerId, orderId, principal.getAccount());
    }

    /**
     * CUSTOMERS CANNOT VIEW OTHER CUSTOMERS' ORDER DETAILS
     * RESTAURANT CAN ALSO NOT VIEW OTHER RESTAURANTS' RESERVATIONS' ORDER DETAILS
     */
    @GetMapping("/{orderId}/items/{itemId}")
    public OrderItem getItem(@PathVariable UUID customerId, @PathVariable UUID orderId,
                             @PathVariable UUID itemId, @AuthenticationPrincipal AccountPrincipal principal) {
        return orderService.findItem(customerId, itemId, orderId, principal.getAccount())
                .orElseThrow(() -> new IllegalArgumentException("Order item not found."));
    }

    /**
     * CUSTOMERS CANNOT PLACE ORDERS ON BEHALF OF OTHER CUSTOMERS
     */
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void placeOrder(@PathVariable UUID customerId, @Valid @RequestBody OrderRequest request,
                           @AuthenticationPrincipal AccountPrincipal principal) {
        orderService.placeOrder(customerId, request, principal.getAccount().getAccountId());
    }

    @GetMapping("/reservations/{resId}")
    public Order getOrderByResId(@PathVariable UUID customerId, @PathVariable UUID resId,
                                 @AuthenticationPrincipal AccountPrincipal principal) {
        return orderService.findOrderByResId(customerId, resId, principal.getAccount());
    }

    // look into this
    @PatchMapping("/{orderId}/refund")
    @ResponseStatus(HttpStatus.OK)
    public void refundOrder(
            @PathVariable UUID customerId,
            @PathVariable UUID orderId) {
        orderService.refundOrder(customerId, orderId);
    }
}
