package com.jsbb.inventoryservice.exception;

import java.util.UUID;

public class PurchaseRequestNotFoundException extends RuntimeException {
    public PurchaseRequestNotFoundException(UUID id) {
        super("Purchase request not found with id: " + id);
    }
}
