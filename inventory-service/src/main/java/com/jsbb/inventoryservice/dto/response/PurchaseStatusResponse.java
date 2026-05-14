package com.jsbb.inventoryservice.dto.response;

import com.jsbb.inventoryservice.model.PurchaseRequest.PurchaseStatus;

import java.time.Instant;
import java.util.UUID;

public record PurchaseStatusResponse(
        UUID purchaseId,
        Long productId,
        Integer quantity,
        PurchaseStatus status,
        Integer attemptCount,
        String failureReason,
        Instant createdAt,
        Instant resolvedAt
) {}
