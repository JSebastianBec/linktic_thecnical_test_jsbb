package com.jsbb.productservice.dto.response;

public record JsonApiData<T>(String id, String type, T attributes) {}
