package com.jsbb.inventoryservice.dto.response;

public record StockResponse(Long productId, ProductResponse product, Integer stock) {}
