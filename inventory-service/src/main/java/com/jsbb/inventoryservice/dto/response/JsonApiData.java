package com.jsbb.inventoryservice.dto.response;

public record JsonApiData<T>(String id, String type, T attributes) {}
