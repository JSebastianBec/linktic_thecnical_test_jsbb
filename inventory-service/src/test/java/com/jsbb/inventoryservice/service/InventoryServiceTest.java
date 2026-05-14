package com.jsbb.inventoryservice.service;

import com.jsbb.inventoryservice.client.ProductClient;
import com.jsbb.inventoryservice.dto.request.PurchaseRequestDto;
import com.jsbb.inventoryservice.dto.request.UpdateStockRequest;
import com.jsbb.inventoryservice.dto.response.ProductResponse;
import com.jsbb.inventoryservice.dto.response.PurchaseStatusResponse;
import com.jsbb.inventoryservice.dto.response.StockResponse;
import com.jsbb.inventoryservice.exception.InventoryNotFoundException;
import com.jsbb.inventoryservice.model.Inventory;
import com.jsbb.inventoryservice.model.PurchaseRequest;
import com.jsbb.inventoryservice.repository.InventoryRepository;
import com.jsbb.inventoryservice.repository.PurchaseRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InventoryServiceTest {

    @Mock private InventoryRepository inventoryRepository;
    @Mock private PurchaseRequestRepository purchaseRequestRepository;
    @Mock private ProductClient productClient;
    @Mock private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private InventoryService service;

    private final ProductResponse product = new ProductResponse(1L, "Laptop", 1500.0, null);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getStock_shouldReturnStockWithProductInfo() {
        Inventory inventory = Inventory.builder().productId(1L).stock(10).build();
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));
        when(productClient.getProduct(1L)).thenReturn(product);

        StockResponse result = service.getStock(1L);

        assertThat(result.stock()).isEqualTo(10);
        assertThat(result.product().name()).isEqualTo("Laptop");
    }

    @Test
    void getStock_shouldThrow_whenInventoryNotFound() {
        when(inventoryRepository.findByProductId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getStock(99L))
                .isInstanceOf(InventoryNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void updateStock_shouldUpdateAndReturn() {
        Inventory inventory = Inventory.builder().productId(1L).stock(5).build();
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));
        when(productClient.getProduct(1L)).thenReturn(product);
        when(inventoryRepository.save(any())).thenReturn(inventory);

        StockResponse result = service.updateStock(1L, new UpdateStockRequest(20));

        assertThat(result.stock()).isEqualTo(20);
        verify(inventoryRepository).save(any());
    }

    @Test
    void requestPurchase_shouldSaveAndPublishEvent() {
        when(productClient.getProduct(1L)).thenReturn(product);
        when(purchaseRequestRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        PurchaseStatusResponse result = service.requestPurchase(new PurchaseRequestDto(1L, 3));

        assertThat(result.status()).isEqualTo(PurchaseRequest.PurchaseStatus.PENDING);
        assertThat(result.productId()).isEqualTo(1L);
        assertThat(result.quantity()).isEqualTo(3);
        verify(purchaseRequestRepository).save(any());
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void getPurchaseStatus_shouldReturnCurrentStatus() {
        UUID id = UUID.randomUUID();
        PurchaseRequest pr = PurchaseRequest.builder()
                .id(id).productId(1L).quantity(2)
                .status(PurchaseRequest.PurchaseStatus.PENDING)
                .attemptCount(1).build();
        when(purchaseRequestRepository.findById(id)).thenReturn(Optional.of(pr));

        PurchaseStatusResponse result = service.getPurchaseStatus(id);

        assertThat(result.status()).isEqualTo(PurchaseRequest.PurchaseStatus.PENDING);
    }

    @Test
    void getPurchaseStatus_shouldThrow_whenNotFound() {
        UUID id = UUID.randomUUID();
        when(purchaseRequestRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPurchaseStatus(id))
                .isInstanceOf(com.jsbb.inventoryservice.exception.PurchaseRequestNotFoundException.class);
    }

    @Test
    void updateStock_shouldThrow_whenInventoryNotFound() {
        when(inventoryRepository.findByProductId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateStock(99L, new UpdateStockRequest(10)))
                .isInstanceOf(InventoryNotFoundException.class);
    }
}
