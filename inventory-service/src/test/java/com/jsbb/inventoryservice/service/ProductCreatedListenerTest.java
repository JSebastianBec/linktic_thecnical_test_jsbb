package com.jsbb.inventoryservice.service;

import com.jsbb.inventoryservice.messaging.event.ProductCreatedEvent;
import com.jsbb.inventoryservice.messaging.listener.ProductCreatedListener;
import com.jsbb.inventoryservice.model.Inventory;
import com.jsbb.inventoryservice.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductCreatedListenerTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private ProductCreatedListener listener;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handle_shouldCreateInventoryWithZeroStock() {
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.empty());

        listener.handle(new ProductCreatedEvent(1L, "Laptop"));

        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void handle_shouldSkipWhenInventoryAlreadyExists() {
        Inventory existing = Inventory.builder().productId(1L).stock(5).build();
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(existing));

        listener.handle(new ProductCreatedEvent(1L, "Laptop"));

        verify(inventoryRepository, never()).save(any());
    }
}
