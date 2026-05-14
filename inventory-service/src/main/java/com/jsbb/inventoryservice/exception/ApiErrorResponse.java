package com.jsbb.inventoryservice.exception;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(List<ApiError> errors, Instant timestamp) {

    public static ApiErrorResponse of(String status, String title, String detail) {
        return new ApiErrorResponse(List.of(new ApiError(status, title, detail)), Instant.now());
    }
}
