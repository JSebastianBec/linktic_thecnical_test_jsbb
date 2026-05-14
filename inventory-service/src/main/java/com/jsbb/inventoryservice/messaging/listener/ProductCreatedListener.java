package com.jsbb.inventoryservice.messaging.listener;

import com.jsbb.inventoryservice.messaging.event.ProductCreatedEvent;
import com.jsbb.inventoryservice.model.Inventory;
import com.jsbb.inventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.jsbb.inventoryservice.messaging.MessagingConfig.PRODUCT_CREATED_QUEUE;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductCreatedListener {

    private final InventoryRepository inventoryRepository;

    @RabbitListener(queues = PRODUCT_CREATED_QUEUE)
    public void handle(ProductCreatedEvent event) {
        log.info("Product created event received: productId={}, name={}", event.productId(), event.name());

        if (inventoryRepository.findByProductId(event.productId()).isPresent()) {
            log.warn("Inventory already exists for productId={}, skipping", event.productId());
            return;
        }

        Inventory inventory = Inventory.builder()
                .productId(event.productId())
                .stock(0)
                .build();

        inventoryRepository.save(inventory);
        log.info("Inventory initialized for productId={} with stock=0", event.productId());
    }
}
