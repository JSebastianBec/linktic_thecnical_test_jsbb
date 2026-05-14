package com.jsbb.inventoryservice.messaging.event;

public record ProductCreatedEvent(Long productId, String name) {}
