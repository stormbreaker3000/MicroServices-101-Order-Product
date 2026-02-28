package com.microapp.productservice.dto;

/**
 * Event DTO representing an order that has been created in the order-service.
 * <p>
 * This object is deserialized from the JSON message published on the
 * {@code orders.exchange} with routing key {@code order.created}.
 *
 * @param orderId   UUID of the newly created order.
 * @param productId Identifier of the product that was ordered.
 * @param quantity  Number of units ordered.
 * @param createdAt ISO-8601 timestamp of when the order was created.
 */
public record OrderCreatedEvent(
        String orderId,
        String productId,
        int quantity,
        String createdAt
) {}
