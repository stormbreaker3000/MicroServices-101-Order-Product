package com.microapp.orderservice.client;

import com.microapp.orderservice.dto.ProductDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

/**
 * HTTP client that calls the product-service REST API synchronously.
 * <p>
 * Uses Spring's {@link RestTemplate} (no extra dependencies).
 * The base URL is read from {@code product-service.base-url} in
 * application.yml.
 *
 * <p>
 * The product ID passed as a path variable must be the database-generated
 * Long PK from the product-service (e.g. 1, 2, 3).
 */
@Component
public class ProductClient {

    private static final Logger log = LoggerFactory.getLogger(ProductClient.class);

    private final RestTemplate restTemplate;
    private final String productServiceBaseUrl;

    public ProductClient(
            RestTemplate restTemplate,
            @Value("${product-service.base-url}") String productServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.productServiceBaseUrl = productServiceBaseUrl;
    }

    /**
     * Fetch a product from the product-service by its numeric ID.
     *
     * @param productId The product's numeric ID (String representation of a Long).
     * @return An {@link Optional} containing the product DTO if found (HTTP 200),
     *         or empty if the product does not exist (HTTP 404).
     * @throws RuntimeException for unexpected HTTP errors (5xx, network issues,
     *                          etc.)
     */
    public Optional<ProductDto> getProduct(String productId) {
        String url = productServiceBaseUrl + "/products/" + productId;
        log.debug("Calling product-service: GET {}", url);
        try {
            ProductDto product = restTemplate.getForObject(url, ProductDto.class);
            return Optional.ofNullable(product);
        } catch (HttpClientErrorException.NotFound ex) {
            log.warn("Product {} not found in product-service", productId);
            return Optional.empty();
        } catch (Exception ex) {
            log.error("Error calling product-service for product {}: {}", productId, ex.getMessage());
            throw new RuntimeException("Failed to reach product-service: " + ex.getMessage(), ex);
        }
    }
}
