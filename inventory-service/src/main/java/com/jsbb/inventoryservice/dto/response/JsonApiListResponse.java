package com.jsbb.inventoryservice.dto.response;

import java.util.List;

public record JsonApiListResponse<T>(List<JsonApiData<T>> data) {

    public static <T> JsonApiListResponse<T> of(List<JsonApiData<T>> items) {
        return new JsonApiListResponse<>(items);
    }
}
