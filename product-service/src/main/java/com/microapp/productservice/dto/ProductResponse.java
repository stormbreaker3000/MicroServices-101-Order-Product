package com.microapp.productservice.dto;

import com.microapp.productservice.model.Product;
import java.math.BigDecimal;

/**
 * Data Transfer Object representing a product returned by the REST API.
 * <p>
 * Converted from the {@link Product} JPA entity in the service layer.
 * Clients never receive raw entity objects — this keeps the API contract
 * stable even when the internal entity changes.
 *
 * @param id    Database-generated product ID (as String for API friendliness).
 * @param name  Human-readable product name.
 * @param price Unit price of the product.
 * @param stock Current number of units available in stock.
 */
public record ProductResponse(
                String id,
                String name,
                BigDecimal price,
                int stock) {
        /**
         * Static factory: convert a {@link Product} entity to a {@link ProductResponse}
         * DTO.
         *
         * @param product The entity retrieved from the database.
         * @return A populated DTO safe to expose via the REST API.
         */
        public static ProductResponse from(Product product) {
                return new ProductResponse(
                                String.valueOf(product.getId()),
                                product.getName(),
                                product.getPrice(),
                                product.getStock());
        }
}
