package com.microapp.orderservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Smoke test – verifies that the Spring application context loads without
 * errors.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.rabbitmq.host=localhost",
        "spring.rabbitmq.port=5672",
        "spring.rabbitmq.username=guest",
        "spring.rabbitmq.password=guest",
        "spring.rabbitmq.virtual-host=/",
        "product-service.base-url=http://localhost:8081"
})
class OrderServiceApplicationTests {

    @Test
    void contextLoads() {
        // Passes if the application context starts without throwing an exception.
    }
}
