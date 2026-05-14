package com.jsbb.inventoryservice.controller;

import com.jsbb.inventoryservice.dto.request.PurchaseRequestDto;
import com.jsbb.inventoryservice.dto.request.UpdateStockRequest;
import com.jsbb.inventoryservice.dto.response.JsonApiData;
import com.jsbb.inventoryservice.dto.response.JsonApiListResponse;
import com.jsbb.inventoryservice.dto.response.JsonApiResponse;
import com.jsbb.inventoryservice.dto.response.PurchaseStatusResponse;
import com.jsbb.inventoryservice.dto.response.StockResponse;
import com.jsbb.inventoryservice.service.InventoryService;
import com.jsbb.inventoryservice.util.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.INVENTORY_BASE_PATH)
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Inventory and purchase management endpoints")
public class InventoryController {

    private final InventoryService service;

    @Operation(summary = "List all inventory")
    @GetMapping
    public ResponseEntity<JsonApiListResponse<StockResponse>> getAll() {
        List<JsonApiData<StockResponse>> items = service.getAll().stream()
                .map(s -> new JsonApiData<>(String.valueOf(s.productId()), ApiConstants.TYPE_INVENTORY, s))
                .toList();
        return ResponseEntity.ok(JsonApiListResponse.of(items));
    }

    @Operation(summary = "Get stock by product ID")
    @GetMapping("/{productId}")
    public ResponseEntity<JsonApiResponse<StockResponse>> getStock(@PathVariable Long productId) {
        StockResponse stock = service.getStock(productId);
        return ResponseEntity.ok(
                JsonApiResponse.of(String.valueOf(productId), ApiConstants.TYPE_INVENTORY, stock));
    }

    @Operation(summary = "Update product stock")
    @PutMapping("/{productId}/stock")
    public ResponseEntity<JsonApiResponse<StockResponse>> updateStock(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateStockRequest request) {
        StockResponse stock = service.updateStock(productId, request);
        return ResponseEntity.ok(
                JsonApiResponse.of(String.valueOf(productId), ApiConstants.TYPE_INVENTORY, stock));
    }

    @Operation(summary = "Request a purchase (async, returns 202 PENDING)")
    @PostMapping("/purchases")
    public ResponseEntity<JsonApiResponse<PurchaseStatusResponse>> requestPurchase(
            @Valid @RequestBody PurchaseRequestDto dto) {
        PurchaseStatusResponse response = service.requestPurchase(dto);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(JsonApiResponse.of(response.purchaseId().toString(), ApiConstants.TYPE_PURCHASES, response));
    }

    @Operation(summary = "Get purchase status by ID")
    @GetMapping("/purchases/{purchaseId}")
    public ResponseEntity<JsonApiResponse<PurchaseStatusResponse>> getPurchaseStatus(
            @PathVariable UUID purchaseId) {
        PurchaseStatusResponse response = service.getPurchaseStatus(purchaseId);
        return ResponseEntity.ok(
                JsonApiResponse.of(purchaseId.toString(), ApiConstants.TYPE_PURCHASES, response));
    }
}
