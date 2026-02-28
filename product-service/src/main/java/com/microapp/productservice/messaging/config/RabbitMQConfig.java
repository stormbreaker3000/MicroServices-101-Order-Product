package com.microapp.productservice.messaging.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ topology configuration for the product-service.
 * <p>
 * Declares the shared exchange, the product service's queue, and the
 * binding that routes {@code order.created} events to that queue.
 *
 * <pre>
 * Exchange : orders.exchange  (topic)
 * Queue    : product.order.created.queue
 * Binding  : routing key → order.created
 * </pre>
 */
@Configuration
public class RabbitMQConfig {

    /** Name of the topic exchange shared by all services. */
    public static final String ORDERS_EXCHANGE = "orders.exchange";

    /** Queue exclusive to the product-service that receives order-created events. */
    public static final String PRODUCT_ORDER_QUEUE = "product.order.created.queue";

    /** Routing key used when publishing / binding order-created events. */
    public static final String ORDER_CREATED_ROUTING_KEY = "order.created";

    // ------------------------------------------------------------------
    // Exchange
    // ------------------------------------------------------------------

    /**
     * Declare the topic exchange. {@code durable = true} ensures the exchange
     * survives broker restarts.
     */
    @Bean
    public TopicExchange ordersExchange() {
        return new TopicExchange(ORDERS_EXCHANGE, true, false);
    }

    // ------------------------------------------------------------------
    // Queue
    // ------------------------------------------------------------------

    /**
     * Declare the durable queue consumed by this service.
     */
    @Bean
    public Queue productOrderQueue() {
        return new Queue(PRODUCT_ORDER_QUEUE, true);
    }

    // ------------------------------------------------------------------
    // Binding
    // ------------------------------------------------------------------

    /**
     * Bind the queue to the exchange using the {@code order.created} routing key.
     */
    @Bean
    public Binding orderCreatedBinding(Queue productOrderQueue, TopicExchange ordersExchange) {
        return BindingBuilder
                .bind(productOrderQueue)
                .to(ordersExchange)
                .with(ORDER_CREATED_ROUTING_KEY);
    }

    // ------------------------------------------------------------------
    // Message converter – use JSON instead of Java serialization
    // ------------------------------------------------------------------

    /**
     * Configure Jackson as the message converter so messages are
     * serialized / deserialized as JSON.
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
