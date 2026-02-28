package com.microapp.orderservice.messaging.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for the order-service.
 * <p>
 * The order-service only needs to declare the exchange it publishes to.
 * Queue and binding declarations live in the consumer (product-service).
 *
 * <pre>
 * Exchange : orders.exchange  (topic)
 * Routing key used when publishing: order.created
 * </pre>
 */
@Configuration
public class RabbitMQConfig {

    /** Name of the topic exchange that all services share. */
    public static final String ORDERS_EXCHANGE = "orders.exchange";

    /** Routing key for order-created events. */
    public static final String ORDER_CREATED_ROUTING_KEY = "order.created";

    // ------------------------------------------------------------------
    // Exchange
    // ------------------------------------------------------------------

    /**
     * Declare the topic exchange. Idempotent – if already declared, no error.
     */
    @Bean
    public TopicExchange ordersExchange() {
        return new TopicExchange(ORDERS_EXCHANGE, true, false);
    }

    // ------------------------------------------------------------------
    // Message converter – JSON
    // ------------------------------------------------------------------

    /**
     * Use Jackson for JSON serialization instead of the default Java
     * binary serialization.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Override the default {@link RabbitTemplate} to use the JSON converter.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
