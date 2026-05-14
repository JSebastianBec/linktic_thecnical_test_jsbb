package com.jsbb.inventoryservice.exception;

import com.jsbb.inventoryservice.util.ErrorMessages;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Long productId) {
        super(ErrorMessages.PRODUCT_NOT_FOUND_DETAIL + productId);
    }
}
