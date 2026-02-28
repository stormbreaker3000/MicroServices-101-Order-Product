package com.microapp.productservice.messaging.consumer;

import com.microapp.productservice.dto.OrderCreatedEvent;
import com.microapp.productservice.messaging.config.RabbitMQConfig;
import com.microapp.productservice.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Async RabbitMQ listener that consumes {@link OrderCreatedEvent} messages.
 * <p>
 * When an order is created in the order-service, it publishes an event to
 * {@code orders.exchange}. This consumer receives it and delegates stock
 * reservation to {@link ProductService#reserveStock(OrderCreatedEvent)}.
 */
@Component
public class OrderCreatedConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedConsumer.class);

    private final ProductService productService;

    public OrderCreatedConsumer(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Listens on {@code product.order.created.queue} and processes each incoming event.
     *
     * <p>Spring AMQP automatically deserializes the JSON payload into an
     * {@link OrderCreatedEvent} using the Jackson converter configured in
     * {@link RabbitMQConfig}.
     *
     * @param event  The deserialized order-created event.
     */
    @RabbitListener(queues = RabbitMQConfig.PRODUCT_ORDER_QUEUE)
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent: orderId={}, productId={}, quantity={}",
                event.orderId(), event.productId(), event.quantity());
        try {
            productService.reserveStock(event);
            log.info("Stock reservation successful for orderId={}", event.orderId());
        } catch (Exception ex) {
            // Log and swallow – in production, consider dead-letter queue (DLQ)
            log.error("Failed to process OrderCreatedEvent orderId={}: {}",
                    event.orderId(), ex.getMessage(), ex);
        }
    }
}
