package com.jsbb.inventoryservice.exception;

import com.jsbb.inventoryservice.util.ErrorMessages;

import java.util.UUID;

public class PurchaseRequestNotFoundException extends RuntimeException {
    public PurchaseRequestNotFoundException(UUID id) {
        super(ErrorMessages.PURCHASE_NOT_FOUND_DETAIL + id);
    }
}
