package com.microapp.orderservice.dto;

import java.math.BigDecimal;

/**
 * DTO representing the product data returned by the product-service REST API.
 * <p>
 * Kept intentionally separate from the product-service's own DTO to maintain
 * microservice independence (no shared libraries).
 *
 * @param id    Database-generated product ID (Long matches product-service
 *              entity).
 * @param name  Product name.
 * @param price Unit price.
 * @param stock Current stock level.
 */
public record ProductDto(
                Long id,
                String name,
                BigDecimal price,
                int stock) {
}
