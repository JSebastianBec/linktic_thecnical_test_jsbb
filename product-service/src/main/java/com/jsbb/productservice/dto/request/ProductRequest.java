package com.jsbb.productservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ProductRequest(
        @NotBlank(message = "Name is required") String name,
        @Positive(message = "Price must be positive") Double price,
        String description
) {}
