package com.jsbb.inventoryservice.service;

import com.jsbb.inventoryservice.client.ProductClient;
import com.jsbb.inventoryservice.dto.request.PurchaseRequestDto;
import com.jsbb.inventoryservice.dto.request.UpdateStockRequest;
import com.jsbb.inventoryservice.dto.response.ProductResponse;
import com.jsbb.inventoryservice.dto.response.PurchaseStatusResponse;
import com.jsbb.inventoryservice.dto.response.StockResponse;
import com.jsbb.inventoryservice.exception.InventoryNotFoundException;
import com.jsbb.inventoryservice.exception.PurchaseRequestNotFoundException;
import com.jsbb.inventoryservice.messaging.MessagingConfig;
import com.jsbb.inventoryservice.messaging.event.PurchaseRequestedEvent;
import com.jsbb.inventoryservice.model.Inventory;
import com.jsbb.inventoryservice.model.PurchaseRequest;
import com.jsbb.inventoryservice.model.PurchaseRequest.PurchaseStatus;
import com.jsbb.inventoryservice.repository.InventoryRepository;
import com.jsbb.inventoryservice.repository.PurchaseRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final PurchaseRequestRepository purchaseRequestRepository;
    private final ProductClient productClient;
    private final RabbitTemplate rabbitTemplate;

    @Transactional(readOnly = true)
    public List<StockResponse> getAll() {
        return inventoryRepository.findAll().stream()
                .map(inv -> {
                    ProductResponse product = productClient.getProduct(inv.getProductId());
                    return new StockResponse(inv.getProductId(), product, inv.getStock());
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public StockResponse getStock(Long productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryNotFoundException(productId));

        ProductResponse product = productClient.getProduct(productId);
        log.info("Stock fetched: productId={}, stock={}", productId, inventory.getStock());
        return new StockResponse(productId, product, inventory.getStock());
    }

    @Transactional
    public StockResponse updateStock(Long productId, UpdateStockRequest request) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryNotFoundException(productId));

        inventory.setStock(request.stock());
        inventoryRepository.save(inventory);

        ProductResponse product = productClient.getProduct(productId);
        log.info("Stock updated: productId={}, newStock={}", productId, request.stock());
        return new StockResponse(productId, product, inventory.getStock());
    }

    @Transactional
    public PurchaseStatusResponse requestPurchase(PurchaseRequestDto dto) {
        // Valida que el producto existe antes de encolar
        productClient.getProduct(dto.productId());

        UUID purchaseId = UUID.randomUUID();

        PurchaseRequest purchaseRequest = PurchaseRequest.builder()
                .id(purchaseId)
                .productId(dto.productId())
                .quantity(dto.quantity())
                .status(PurchaseStatus.PENDING)
                .attemptCount(0)
                .createdAt(Instant.now())
                .build();

        purchaseRequestRepository.save(purchaseRequest);

        rabbitTemplate.convertAndSend(
                MessagingConfig.PURCHASE_EXCHANGE,
                MessagingConfig.PURCHASE_REQUESTED_KEY,
                new PurchaseRequestedEvent(purchaseId, dto.productId(), dto.quantity(), 1)
        );

        log.info("Purchase request enqueued: id={}, productId={}, qty={}", purchaseId, dto.productId(), dto.quantity());
        return toStatusResponse(purchaseRequest);
    }

    @Transactional(readOnly = true)
    public PurchaseStatusResponse getPurchaseStatus(UUID purchaseId) {
        PurchaseRequest request = purchaseRequestRepository.findById(purchaseId)
                .orElseThrow(() -> new PurchaseRequestNotFoundException(purchaseId));
        return toStatusResponse(request);
    }

    private PurchaseStatusResponse toStatusResponse(PurchaseRequest r) {
        return new PurchaseStatusResponse(
                r.getId(), r.getProductId(), r.getQuantity(),
                r.getStatus(), r.getAttemptCount(), r.getFailureReason(),
                r.getCreatedAt(), r.getResolvedAt()
        );
    }
}
