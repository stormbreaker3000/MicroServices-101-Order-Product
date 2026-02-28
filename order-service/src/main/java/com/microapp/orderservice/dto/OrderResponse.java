package com.microapp.orderservice.dto;

import com.microapp.orderservice.model.Order;

/**
 * Data Transfer Object returned after creating an order.
 * <p>
 * Converted from the {@link Order} JPA entity in the service layer.
 *
 * @param orderId   UUID of the created order.
 * @param productId ID of the ordered product.
 * @param quantity  Number of units ordered.
 * @param status    Current order status (e.g. "CREATED").
 * @param createdAt ISO-8601 creation timestamp.
 */
public record OrderResponse(
                String orderId,
                String productId,
                int quantity,
                String status,
                String createdAt) {
        /**
         * Static factory: convert an {@link Order} entity to an {@link OrderResponse}
         * DTO.
         *
         * @param order The persisted entity.
         * @return A populated DTO safe to expose via the REST API.
         */
        public static OrderResponse from(Order order) {
                return new OrderResponse(
                                order.getId(),
                                order.getProductId(),
                                order.getQuantity(),
                                order.getStatus(),
                                order.getCreatedAt().toString());
        }
}
