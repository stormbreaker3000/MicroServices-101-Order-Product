package com.microapp.orderservice.dto;

/**
 * Event published to RabbitMQ when a new order is created.
 * <p>
 * Consumed by the product-service (and any other interested services)
 * to react asynchronously (e.g. reduce stock).
 *
 * @param orderId   UUID of the newly created order.
 * @param productId ID of the ordered product.
 * @param quantity  Number of units ordered.
 * @param createdAt ISO-8601 timestamp of the order creation.
 */
public record OrderCreatedEvent(
        String orderId,
        String productId,
        int quantity,
        String createdAt) {
}
