package com.microapp.productservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Product Microservice.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Expose a REST API to fetch product details (GET /products/{id})</li>
 *   <li>Listen for OrderCreated events from RabbitMQ and reduce stock accordingly</li>
 * </ul>
 * Runs on port 8081.
 */
@SpringBootApplication
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
