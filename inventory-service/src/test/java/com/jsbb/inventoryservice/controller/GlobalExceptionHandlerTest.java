package com.jsbb.inventoryservice.controller;

import com.jsbb.inventoryservice.config.ApiKeyFilter;
import com.jsbb.inventoryservice.config.SecurityConfig;
import com.jsbb.inventoryservice.exception.*;
import com.jsbb.inventoryservice.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = InventoryController.class)
@Import({SecurityConfig.class, ApiKeyFilter.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = "security.api-key=test-key")
class GlobalExceptionHandlerTest {

    private static final String API_KEY = "test-key";

    @Autowired private MockMvc mockMvc;
    @MockitoBean private InventoryService service;

    @Test
    void shouldReturn404_whenInventoryNotFound() throws Exception {
        when(service.getStock(1L)).thenThrow(new InventoryNotFoundException(1L));

        mockMvc.perform(get("/api/v1/inventory/1").header("X-API-KEY", API_KEY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].status").value("404"))
                .andExpect(jsonPath("$.errors[0].title").value("Inventory Not Found"));
    }

    @Test
    void shouldReturn404_whenProductNotFound() throws Exception {
        when(service.getStock(1L)).thenThrow(new ProductNotFoundException(1L));

        mockMvc.perform(get("/api/v1/inventory/1").header("X-API-KEY", API_KEY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].title").value("Product Not Found"));
    }

    @Test
    void shouldReturn503_whenProductServiceUnavailable() throws Exception {
        when(service.getStock(1L)).thenThrow(new ProductServiceUnavailableException("down"));

        mockMvc.perform(get("/api/v1/inventory/1").header("X-API-KEY", API_KEY))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.errors[0].status").value("503"));
    }

    @Test
    void shouldReturn404_whenPurchaseRequestNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.getPurchaseStatus(id)).thenThrow(new PurchaseRequestNotFoundException(id));

        mockMvc.perform(get("/api/v1/inventory/purchases/" + id).header("X-API-KEY", API_KEY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].title").value("Purchase Request Not Found"));
    }

    @Test
    void shouldReturn500_whenGenericException() throws Exception {
        when(service.getStock(1L)).thenThrow(new RuntimeException("unexpected"));

        mockMvc.perform(get("/api/v1/inventory/1").header("X-API-KEY", API_KEY))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errors[0].status").value("500"));
    }
}
