package com.jsbb.inventoryservice;

import com.jsbb.inventoryservice.exception.ApiErrorResponse;
import com.jsbb.inventoryservice.exception.InventoryNotFoundException;
import com.jsbb.inventoryservice.model.Inventory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryServiceApplicationTests {

    @Test
    void apiErrorResponse_shouldContainCorrectFields() {
        ApiErrorResponse response = ApiErrorResponse.of("404", "Not Found", "detail");
        assertThat(response.errors()).hasSize(1);
        assertThat(response.errors().get(0).status()).isEqualTo("404");
        assertThat(response.timestamp()).isNotNull();
    }

    @Test
    void inventory_hasEnoughStock_shouldReturnTrueWhenSufficient() {
        Inventory inventory = Inventory.builder().productId(1L).stock(10).build();
        assertThat(inventory.hasEnoughStock(5)).isTrue();
        assertThat(inventory.hasEnoughStock(10)).isTrue();
        assertThat(inventory.hasEnoughStock(11)).isFalse();
    }

    @Test
    void inventory_deduct_shouldReduceStock() {
        Inventory inventory = Inventory.builder().productId(1L).stock(10).build();
        inventory.deduct(3);
        assertThat(inventory.getStock()).isEqualTo(7);
    }

    @Test
    void inventoryNotFoundException_shouldContainProductId() {
        InventoryNotFoundException ex = new InventoryNotFoundException(42L);
        assertThat(ex.getMessage()).contains("42");
    }
}
