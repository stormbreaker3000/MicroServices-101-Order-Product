package com.microapp.productservice.service;

import com.microapp.productservice.dto.OrderCreatedEvent;
import com.microapp.productservice.dto.ProductResponse;
import com.microapp.productservice.model.Product;
import com.microapp.productservice.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Core business logic for the Product service.
 * <p>
 * All data is persisted via {@link ProductRepository} (Spring Data JPA / H2).
 * There are no hardcoded or in-memory products — products must be created
 * through the API (or via the H2 console / a data.sql file in tests).
 */
@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // ------------------------------------------------------------------
    // REST-facing operations
    // ------------------------------------------------------------------

    /**
     * Retrieve all products from the database.
     *
     * @return List of {@link ProductResponse} DTOs (may be empty).
     */
    @Transactional(readOnly = true)
    public java.util.List<ProductResponse> findAll() {
        return productRepository.findAll()
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    /**
     * Retrieve a single product by its database ID.
     *
     * @param id Product primary key (Long).
     * @return An {@link Optional} containing the mapped DTO if found.
     */
    @Transactional(readOnly = true)
    public Optional<ProductResponse> findById(Long id) {
        return productRepository.findById(id)
                .map(ProductResponse::from);
    }

    /**
     * Persist a new product.
     *
     * @param product Unsaved {@link Product} entity (id must be null).
     * @return The saved entity with its generated ID, mapped to a DTO.
     */
    @Transactional
    public ProductResponse save(Product product) {
        Product saved = productRepository.save(product);
        log.info("Product saved: {}", saved);
        return ProductResponse.from(saved);
    }

    // ------------------------------------------------------------------
    // Messaging-facing operations
    // ------------------------------------------------------------------

    /**
     * Reduce stock when an {@link OrderCreatedEvent} is consumed from RabbitMQ.
     * <p>
     * The update is wrapped in a {@code @Transactional} so the stock change
     * is committed atomically. If stock would go negative, the update is skipped
     * and a warning is logged.
     *
     * @param event The deserialized order-created event.
     */
    @Transactional
    public void reserveStock(OrderCreatedEvent event) {
        Long productId;
        try {
            productId = Long.parseLong(event.productId());
        } catch (NumberFormatException ex) {
            log.error("reserveStock – invalid productId '{}' in event for orderId {}",
                    event.productId(), event.orderId());
            return;
        }

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            log.warn("reserveStock – product {} not found for order {}", event.productId(), event.orderId());
            return;
        }

        int newStock = product.getStock() - event.quantity();
        if (newStock < 0) {
            log.warn("reserveStock – insufficient stock for product {} (requested {}, available {})",
                    event.productId(), event.quantity(), product.getStock());
            return;
        }

        product.setStock(newStock);
        productRepository.save(product);
        log.info("reserveStock – stock for product {} updated to {}", product.getId(), newStock);
    }
}
