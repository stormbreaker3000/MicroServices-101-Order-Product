package com.microapp.orderservice.controller;

import com.microapp.orderservice.dto.CreateOrderRequest;
import com.microapp.orderservice.dto.OrderResponse;
import com.microapp.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller exposing order endpoints.
 * <p>
 * Base path: /orders
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Retrieve all orders from the database.
     *
     * <pre>
     * GET /orders → 200 OK with list
     * </pre>
     */
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.findAll());
    }

    /**
     * Retrieve a single order by its UUID.
     *
     * <pre>
     * GET /orders/{orderId}
     * 200 OK  – found
     * 404     – not found
     * </pre>
     *
     * @param orderId UUID string of the order.
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable String orderId) {
        return orderService.findById(orderId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity
                        .status(404)
                        .body(Map.of("message", "Order not found")));
    }

    /**
     * Create a new order.
     *
     * <pre>
     * POST /orders
     * Content-Type: application/json
     * Body: { "productId": "1", "quantity": 2 }
     *
     * 201 Created  – order successfully created and persisted
     * 400 Bad Request – validation error, product not found, or insufficient stock
     * </pre>
     *
     * @param request Validated request body.
     */
    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        try {
            OrderResponse response = orderService.createOrder(request);
            return ResponseEntity.status(201).body(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }
}
