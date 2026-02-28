package com.microapp.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Order Microservice.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Expose a REST API to create orders (POST /orders)</li>
 *   <li>Call product-service synchronously to validate product + stock</li>
 *   <li>Publish an {@code OrderCreatedEvent} to RabbitMQ after a successful order</li>
 * </ul>
 * Runs on port 8080.
 */
@SpringBootApplication
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
