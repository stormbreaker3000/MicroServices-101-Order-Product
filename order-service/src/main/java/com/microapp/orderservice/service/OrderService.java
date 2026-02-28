package com.microapp.orderservice.service;

import com.microapp.orderservice.client.ProductClient;
import com.microapp.orderservice.dto.*;
import com.microapp.orderservice.messaging.producer.OrderEventPublisher;
import com.microapp.orderservice.model.Order;
import com.microapp.orderservice.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Core business logic for the Order service.
 * <p>
 * All data is persisted via {@link OrderRepository} (Spring Data JPA / H2).
 * <p>
 * Workflow for {@code createOrder}:
 * <ol>
 * <li>Validate the request (quantity &gt; 0 enforced by Bean Validation)</li>
 * <li>Call product-service synchronously to confirm product exists and has
 * enough stock</li>
 * <li>Persist the {@link Order} entity via JPA</li>
 * <li>Publish {@link OrderCreatedEvent} to RabbitMQ</li>
 * <li>Return the mapped {@link OrderResponse} DTO</li>
 * </ol>
 */
@Service
public class OrderService {

        private static final Logger log = LoggerFactory.getLogger(OrderService.class);

        private final OrderRepository orderRepository;
        private final ProductClient productClient;
        private final OrderEventPublisher eventPublisher;

        public OrderService(OrderRepository orderRepository,
                        ProductClient productClient,
                        OrderEventPublisher eventPublisher) {
                this.orderRepository = orderRepository;
                this.productClient = productClient;
                this.eventPublisher = eventPublisher;
        }

        // ------------------------------------------------------------------
        // Query operations
        // ------------------------------------------------------------------

        /**
         * Retrieve all orders from the database.
         *
         * @return List of {@link OrderResponse} DTOs.
         */
        @Transactional(readOnly = true)
        public List<OrderResponse> findAll() {
                return orderRepository.findAll()
                                .stream()
                                .map(OrderResponse::from)
                                .toList();
        }

        /**
         * Retrieve a single order by its ID.
         *
         * @param orderId UUID string of the order.
         * @return The mapped DTO if found.
         */
        @Transactional(readOnly = true)
        public java.util.Optional<OrderResponse> findById(String orderId) {
                return orderRepository.findById(orderId).map(OrderResponse::from);
        }

        // ------------------------------------------------------------------
        // Command operations
        // ------------------------------------------------------------------

        /**
         * Create and persist a new order.
         *
         * @param request Validated order request containing productId and quantity.
         * @return The persisted {@link OrderResponse} DTO.
         * @throws IllegalArgumentException if the product is not found or stock is
         *                                  insufficient.
         */
        @Transactional
        public OrderResponse createOrder(CreateOrderRequest request) {

                // --- Step 1: Validate product & stock (synchronous REST call) ---
                ProductDto product = productClient.getProduct(request.productId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Product not found: " + request.productId()));

                log.debug("Product fetched: id={}, stock={}, requested qty={}",
                                product.id(), product.stock(), request.quantity());

                if (product.stock() < request.quantity()) {
                        throw new IllegalArgumentException(
                                        String.format("Insufficient stock for product %s: available %d, requested %d",
                                                        request.productId(), product.stock(), request.quantity()));
                }

                // --- Step 2: Build and persist Order entity ---
                Instant now = Instant.now();
                Order order = new Order(
                                UUID.randomUUID().toString(),
                                request.productId(),
                                request.quantity(),
                                "CREATED",
                                now);
                Order saved = orderRepository.save(order);
                log.info("Order persisted: {}", saved);

                // --- Step 3: Publish async event to RabbitMQ ---
                OrderCreatedEvent event = new OrderCreatedEvent(
                                saved.getId(),
                                saved.getProductId(),
                                saved.getQuantity(),
                                now.toString());
                eventPublisher.publish(event);

                return OrderResponse.from(saved);
        }
}
