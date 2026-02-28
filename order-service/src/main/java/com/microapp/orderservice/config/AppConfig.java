package com.microapp.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * General application-level bean configuration for the order-service.
 */
@Configuration
public class AppConfig {

    /**
     * Provide a {@link RestTemplate} bean used by
     * {@link com.microapp.orderservice.client.ProductClient}
     * to make synchronous HTTP calls to the product-service.
     *
     * @return A simple {@link RestTemplate} with default configuration.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
