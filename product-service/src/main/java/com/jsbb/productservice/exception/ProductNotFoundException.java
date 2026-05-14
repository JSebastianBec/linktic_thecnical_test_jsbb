package com.jsbb.productservice.exception;

import com.jsbb.productservice.util.ErrorMessages;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long id) {
        super(ErrorMessages.PRODUCT_NOT_FOUND_DETAIL + id);
    }
}
