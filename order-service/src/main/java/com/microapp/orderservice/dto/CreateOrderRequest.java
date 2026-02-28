package com.microapp.orderservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Incoming request body when creating a new order.
 *
 * @param productId  The ID of the product to order. Must not be blank.
 * @param quantity   Number of units to order. Must be at least 1.
 */
public record CreateOrderRequest(
        @NotBlank(message = "productId must not be blank")
        String productId,

        @Min(value = 1, message = "quantity must be at least 1")
        int quantity
) {}
