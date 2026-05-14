package com.jsbb.inventoryservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jsbb.inventoryservice.config.ApiKeyFilter;
import com.jsbb.inventoryservice.config.SecurityConfig;
import com.jsbb.inventoryservice.dto.request.PurchaseRequestDto;
import com.jsbb.inventoryservice.dto.request.UpdateStockRequest;
import com.jsbb.inventoryservice.dto.response.ProductResponse;
import com.jsbb.inventoryservice.dto.response.PurchaseStatusResponse;
import com.jsbb.inventoryservice.dto.response.StockResponse;
import com.jsbb.inventoryservice.exception.GlobalExceptionHandler;
import com.jsbb.inventoryservice.exception.InventoryNotFoundException;
import com.jsbb.inventoryservice.model.PurchaseRequest.PurchaseStatus;
import com.jsbb.inventoryservice.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = InventoryController.class)
@Import({SecurityConfig.class, ApiKeyFilter.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = "security.api-key=test-key")
class InventoryControllerTest {

    private static final String API_KEY = "test-key";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private InventoryService service;

    private final ProductResponse product = new ProductResponse(1L, "Laptop", 1500.0, null);

    @Test
    void getStock_shouldReturn200WithJsonApiFormat() throws Exception {
        when(service.getStock(1L)).thenReturn(new StockResponse(1L, product, 10));

        mockMvc.perform(get("/api/v1/inventory/1").header("X-API-KEY", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.type").value("inventory"))
                .andExpect(jsonPath("$.data.attributes.stock").value(10))
                .andExpect(jsonPath("$.data.attributes.product.name").value("Laptop"));
    }

    @Test
    void getStock_shouldReturn404_whenNotFound() throws Exception {
        when(service.getStock(99L)).thenThrow(new InventoryNotFoundException(99L));

        mockMvc.perform(get("/api/v1/inventory/99").header("X-API-KEY", API_KEY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].status").value("404"));
    }

    @Test
    void updateStock_shouldReturn200() throws Exception {
        when(service.updateStock(eq(1L), any())).thenReturn(new StockResponse(1L, product, 50));

        mockMvc.perform(put("/api/v1/inventory/1/stock")
                        .header("X-API-KEY", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateStockRequest(50))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attributes.stock").value(50));
    }

    @Test
    void requestPurchase_shouldReturn202WithPendingStatus() throws Exception {
        UUID purchaseId = UUID.randomUUID();
        PurchaseStatusResponse response = new PurchaseStatusResponse(
                purchaseId, 1L, 3, PurchaseStatus.PENDING, 0, null, Instant.now(), null);

        when(service.requestPurchase(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/inventory/purchases")
                        .header("X-API-KEY", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PurchaseRequestDto(1L, 3))))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.type").value("purchases"))
                .andExpect(jsonPath("$.data.attributes.status").value("PENDING"));
    }

    @Test
    void getPurchaseStatus_shouldReturn200() throws Exception {
        UUID purchaseId = UUID.randomUUID();
        PurchaseStatusResponse response = new PurchaseStatusResponse(
                purchaseId, 1L, 3, PurchaseStatus.COMPLETED, 1, null, Instant.now(), Instant.now());

        when(service.getPurchaseStatus(purchaseId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/inventory/purchases/" + purchaseId)
                        .header("X-API-KEY", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attributes.status").value("COMPLETED"));
    }

    @Test
    void shouldReturn401_whenApiKeyMissing() throws Exception {
        mockMvc.perform(get("/api/v1/inventory/1"))
                .andExpect(status().isUnauthorized());
    }
}
