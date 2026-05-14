package com.jsbb.inventoryservice.messaging.event;

import java.util.UUID;

public record PurchaseRequestedEvent(
        UUID purchaseId,
        Long productId,
        Integer quantity,
        int attemptCount
) {
    public PurchaseRequestedEvent nextAttempt() {
        return new PurchaseRequestedEvent(purchaseId, productId, quantity, attemptCount + 1);
    }
}
