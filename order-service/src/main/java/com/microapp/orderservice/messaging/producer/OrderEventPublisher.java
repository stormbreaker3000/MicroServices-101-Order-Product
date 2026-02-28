package com.microapp.orderservice.messaging.producer;

import com.microapp.orderservice.dto.OrderCreatedEvent;
import com.microapp.orderservice.messaging.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes {@link OrderCreatedEvent} messages to RabbitMQ.
 * <p>
 * The event is routed to any queue bound to {@code orders.exchange}
 * with routing key {@code order.created}. Currently that is only the
 * {@code product.order.created.queue} in the product-service, but other
 * services can subscribe without modifying this publisher.
 */
@Component
public class OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public OrderEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publish an {@link OrderCreatedEvent} to the orders exchange.
     *
     * @param event The event to publish. Serialized to JSON by the
     *              {@link com.microapp.orderservice.messaging.config.RabbitMQConfig#jsonMessageConverter()}.
     */
    public void publish(OrderCreatedEvent event) {
        log.info("Publishing OrderCreatedEvent: orderId={}, productId={}, qty={}",
                event.orderId(), event.productId(), event.quantity());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDERS_EXCHANGE,
                RabbitMQConfig.ORDER_CREATED_ROUTING_KEY,
                event);
        log.debug("OrderCreatedEvent published successfully for orderId={}", event.orderId());
    }
}
