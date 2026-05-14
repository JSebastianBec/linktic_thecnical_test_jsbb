package com.jsbb.productservice.dto.response;

public record ProductResponse(
        Long id,
        String name,
        Double price,
        String description
) {}
