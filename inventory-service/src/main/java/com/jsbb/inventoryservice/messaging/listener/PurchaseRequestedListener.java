package com.jsbb.inventoryservice.messaging.listener;

import com.jsbb.inventoryservice.messaging.MessagingConfig;
import com.jsbb.inventoryservice.util.ErrorMessages;
import com.jsbb.inventoryservice.messaging.event.PurchaseRequestedEvent;
import com.jsbb.inventoryservice.model.Inventory;
import com.jsbb.inventoryservice.model.PurchaseRequest;
import com.jsbb.inventoryservice.repository.InventoryRepository;
import com.jsbb.inventoryservice.repository.PurchaseRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

import static com.jsbb.inventoryservice.messaging.MessagingConfig.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class PurchaseRequestedListener {

    private final InventoryRepository inventoryRepository;
    private final PurchaseRequestRepository purchaseRequestRepository;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = PURCHASE_REQUESTED_QUEUE)
    @Transactional
    public void handle(PurchaseRequestedEvent event) {
        log.info("Processing purchase: id={}, productId={}, qty={}, attempt={}",
                event.purchaseId(), event.productId(), event.quantity(), event.attemptCount());

        Optional<PurchaseRequest> requestOpt = purchaseRequestRepository.findById(event.purchaseId());
        if (requestOpt.isEmpty()) {
            log.warn("Purchase request not found: {}", event.purchaseId());
            return;
        }

        PurchaseRequest purchaseRequest = requestOpt.get();
        purchaseRequest.setAttemptCount(event.attemptCount());

        Optional<Inventory> inventoryOpt = inventoryRepository.findByProductId(event.productId());

        boolean hasStock = inventoryOpt
                .map(inv -> inv.hasEnoughStock(event.quantity()))
                .orElse(false);

        if (hasStock) {
            Inventory inventory = inventoryOpt.get();
            inventory.deduct(event.quantity());
            inventoryRepository.save(inventory);

            purchaseRequest.setStatus(PurchaseRequest.PurchaseStatus.COMPLETED);
            purchaseRequest.setResolvedAt(Instant.now());
            purchaseRequestRepository.save(purchaseRequest);

            log.info("Purchase COMPLETED: id={}, remaining stock={}", event.purchaseId(), inventory.getStock());

        } else if (event.attemptCount() < MAX_ATTEMPTS) {
            purchaseRequestRepository.save(purchaseRequest);
            rabbitTemplate.convertAndSend(PURCHASE_EXCHANGE, PURCHASE_WAIT_KEY, event.nextAttempt());
            log.warn("Insufficient stock — retry scheduled (attempt {}/{})", event.attemptCount(), MAX_ATTEMPTS);

        } else {
            purchaseRequest.setStatus(PurchaseRequest.PurchaseStatus.FAILED);
            purchaseRequest.setFailureReason(ErrorMessages.INSUFFICIENT_STOCK_AFTER + MAX_ATTEMPTS + ErrorMessages.ATTEMPTS_SUFFIX);
            purchaseRequest.setResolvedAt(Instant.now());
            purchaseRequestRepository.save(purchaseRequest);
            log.warn("Purchase FAILED: id={} — insufficient stock after {} attempts", event.purchaseId(), MAX_ATTEMPTS);
        }
    }
}
