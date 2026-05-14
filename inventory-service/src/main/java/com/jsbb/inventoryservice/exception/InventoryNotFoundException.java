package com.jsbb.inventoryservice.exception;

import com.jsbb.inventoryservice.util.ErrorMessages;

public class InventoryNotFoundException extends RuntimeException {
    public InventoryNotFoundException(Long productId) {
        super(ErrorMessages.INVENTORY_NOT_FOUND_DETAIL + productId);
    }
}
