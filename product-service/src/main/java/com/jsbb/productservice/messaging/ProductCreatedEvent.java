package com.jsbb.productservice.messaging;

public record ProductCreatedEvent(Long productId, String name) {}
