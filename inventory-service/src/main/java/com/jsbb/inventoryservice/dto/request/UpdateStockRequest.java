package com.jsbb.inventoryservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateStockRequest(
        @NotNull @Min(value = 0, message = "Stock cannot be negative") Integer stock
) {}
