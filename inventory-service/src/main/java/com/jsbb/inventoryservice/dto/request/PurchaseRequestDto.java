package com.jsbb.inventoryservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PurchaseRequestDto(
        @NotNull Long productId,
        @NotNull @Min(value = 1, message = "Quantity must be at least 1") Integer quantity
) {}
