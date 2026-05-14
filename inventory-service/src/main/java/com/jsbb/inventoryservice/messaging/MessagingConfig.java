package com.jsbb.inventoryservice.messaging;

import com.jsbb.inventoryservice.util.ApiConstants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfig {

    // ── Exchanges ──────────────────────────────────────────────────────────────
    public static final String PRODUCTS_EXCHANGE  = "products.exchange";
    public static final String PURCHASE_EXCHANGE  = "purchase.exchange";
    public static final String DLX_EXCHANGE       = "dlx.exchange";

    // ── Routing keys ───────────────────────────────────────────────────────────
    public static final String PRODUCT_CREATED_KEY   = "product.created";
    public static final String PURCHASE_REQUESTED_KEY = "purchase.requested";
    public static final String PURCHASE_WAIT_KEY      = "purchase.wait";

    // ── Queue names ────────────────────────────────────────────────────────────
    public static final String PRODUCT_CREATED_QUEUE  = "inventory.product-created";
    public static final String PURCHASE_REQUESTED_QUEUE = "purchase.requested";
    public static final String PURCHASE_WAIT_QUEUE    = "purchase.wait";
    public static final String PURCHASE_DLQ           = "purchase.dlq";

    public static final int MAX_ATTEMPTS = 3;
    public static final int WAIT_TTL_MS  = 10_000;

    // ── Exchanges ──────────────────────────────────────────────────────────────
    @Bean
    public TopicExchange productsExchange() {
        return new TopicExchange(PRODUCTS_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange purchaseExchange() {
        return new TopicExchange(PURCHASE_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange dlxExchange() {
        return new TopicExchange(DLX_EXCHANGE, true, false);
    }

    // ── Queues ─────────────────────────────────────────────────────────────────
    @Bean
    public Queue productCreatedQueue() {
        return QueueBuilder.durable(PRODUCT_CREATED_QUEUE)
                .withArgument(ApiConstants.RABBIT_ARG_DEAD_LETTER_EXCHANGE, DLX_EXCHANGE)
                .build();
    }

    @Bean
    public Queue purchaseRequestedQueue() {
        return QueueBuilder.durable(PURCHASE_REQUESTED_QUEUE).build();
    }

    // Cola de espera: TTL expira → vuelve a purchase.requested
    @Bean
    public Queue purchaseWaitQueue() {
        return QueueBuilder.durable(PURCHASE_WAIT_QUEUE)
                .withArgument(ApiConstants.RABBIT_ARG_MESSAGE_TTL, WAIT_TTL_MS)
                .withArgument(ApiConstants.RABBIT_ARG_DEAD_LETTER_EXCHANGE, PURCHASE_EXCHANGE)
                .withArgument(ApiConstants.RABBIT_ARG_DEAD_LETTER_ROUTING, PURCHASE_REQUESTED_KEY)
                .build();
    }

    @Bean
    public Queue purchaseDlq() {
        return QueueBuilder.durable(PURCHASE_DLQ).build();
    }

    // ── Bindings ───────────────────────────────────────────────────────────────
    @Bean
    public Binding productCreatedBinding() {
        return BindingBuilder.bind(productCreatedQueue())
                .to(productsExchange())
                .with(PRODUCT_CREATED_KEY);
    }

    @Bean
    public Binding purchaseRequestedBinding() {
        return BindingBuilder.bind(purchaseRequestedQueue())
                .to(purchaseExchange())
                .with(PURCHASE_REQUESTED_KEY);
    }

    @Bean
    public Binding purchaseWaitBinding() {
        return BindingBuilder.bind(purchaseWaitQueue())
                .to(purchaseExchange())
                .with(PURCHASE_WAIT_KEY);
    }

    // ── Converter ─────────────────────────────────────────────────────────────
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
