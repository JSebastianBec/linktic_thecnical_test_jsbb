package com.jsbb.inventoryservice.service;

import com.jsbb.inventoryservice.messaging.event.PurchaseRequestedEvent;
import com.jsbb.inventoryservice.messaging.listener.PurchaseRequestedListener;
import com.jsbb.inventoryservice.model.Inventory;
import com.jsbb.inventoryservice.model.PurchaseRequest;
import com.jsbb.inventoryservice.model.PurchaseRequest.PurchaseStatus;
import com.jsbb.inventoryservice.repository.InventoryRepository;
import com.jsbb.inventoryservice.repository.PurchaseRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static com.jsbb.inventoryservice.messaging.MessagingConfig.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PurchaseRequestedListenerTest {

    @Mock private InventoryRepository inventoryRepository;
    @Mock private PurchaseRequestRepository purchaseRequestRepository;
    @Mock private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private PurchaseRequestedListener listener;

    private UUID purchaseId;
    private PurchaseRequest purchaseRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        purchaseId = UUID.randomUUID();
        purchaseRequest = PurchaseRequest.builder()
                .id(purchaseId).productId(1L).quantity(3)
                .status(PurchaseStatus.PENDING).attemptCount(0)
                .createdAt(Instant.now()).build();
    }

    @Test
    void handle_shouldCompleteWhenStockAvailable() {
        Inventory inventory = Inventory.builder().productId(1L).stock(10).build();
        when(purchaseRequestRepository.findById(purchaseId)).thenReturn(Optional.of(purchaseRequest));
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));

        listener.handle(new PurchaseRequestedEvent(purchaseId, 1L, 3, 1));

        assertThat(purchaseRequest.getStatus()).isEqualTo(PurchaseStatus.COMPLETED);
        assertThat(inventory.getStock()).isEqualTo(7);
        verify(inventoryRepository).save(inventory);
        verify(purchaseRequestRepository).save(purchaseRequest);
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void handle_shouldRetryWhenNoStockAndAttemptsRemaining() {
        Inventory inventory = Inventory.builder().productId(1L).stock(1).build();
        when(purchaseRequestRepository.findById(purchaseId)).thenReturn(Optional.of(purchaseRequest));
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));

        listener.handle(new PurchaseRequestedEvent(purchaseId, 1L, 3, 1));

        verify(rabbitTemplate).convertAndSend(eq(PURCHASE_EXCHANGE), eq(PURCHASE_WAIT_KEY), any(Object.class));
        assertThat(purchaseRequest.getStatus()).isEqualTo(PurchaseStatus.PENDING);
    }

    @Test
    void handle_shouldFailWhenMaxAttemptsReached() {
        Inventory inventory = Inventory.builder().productId(1L).stock(1).build();
        when(purchaseRequestRepository.findById(purchaseId)).thenReturn(Optional.of(purchaseRequest));
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));

        listener.handle(new PurchaseRequestedEvent(purchaseId, 1L, 3, MAX_ATTEMPTS));

        assertThat(purchaseRequest.getStatus()).isEqualTo(PurchaseStatus.FAILED);
        assertThat(purchaseRequest.getFailureReason()).contains("attempts");
        verifyNoMoreInteractions(rabbitTemplate);
    }

    @Test
    void handle_shouldSkipWhenPurchaseRequestNotFound() {
        when(purchaseRequestRepository.findById(purchaseId)).thenReturn(Optional.empty());

        listener.handle(new PurchaseRequestedEvent(purchaseId, 1L, 3, 1));

        verifyNoInteractions(inventoryRepository);
        verifyNoInteractions(rabbitTemplate);
    }
}
