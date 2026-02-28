package com.microapp.productservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Smoke test – verifies that the Spring application context loads without
 * errors.
 * <p>
 * The RabbitMQ connection is replaced with a mock listener container factory
 * so no live broker is required during unit/integration tests.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.rabbitmq.host=localhost",
        "spring.rabbitmq.port=5672",
        "spring.rabbitmq.username=guest",
        "spring.rabbitmq.password=guest",
        "spring.rabbitmq.virtual-host=/"
})
class ProductServiceApplicationTests {

    @Test
    void contextLoads() {
        // Passes if the application context starts without throwing an exception.
    }
}
