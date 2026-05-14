package com.jsbb.inventoryservice.dto.response;

public record JsonApiResponse<T>(JsonApiData<T> data) {

    public static <T> JsonApiResponse<T> of(String id, String type, T attributes) {
        return new JsonApiResponse<>(new JsonApiData<>(id, type, attributes));
    }
}
