package com.jsbb.productservice.util;

public class ErrorMessages {

    private ErrorMessages() {}

    // ── Títulos de error JSON API ──────────────────────────────────────────────
    public static final String PRODUCT_NOT_FOUND_TITLE    = "Product Not Found";
    public static final String VALIDATION_ERROR_TITLE     = "Validation Error";
    public static final String INTERNAL_ERROR_TITLE       = "Internal Server Error";

    // ── Mensajes de detalle ────────────────────────────────────────────────────
    public static final String PRODUCT_NOT_FOUND_DETAIL   = "Product not found with id: ";
    public static final String INVALID_OR_MISSING_API_KEY = "Invalid or missing API key";

    // ── Códigos de estado ─────────────────────────────────────────────────────
    public static final String STATUS_400 = "400";
    public static final String STATUS_401 = "401";
    public static final String STATUS_404 = "404";
    public static final String STATUS_500 = "500";
}
