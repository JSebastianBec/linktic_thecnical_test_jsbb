package com.jsbb.inventoryservice.util;

public class ErrorMessages {

    private ErrorMessages() {}

    // ── Títulos de error JSON API ──────────────────────────────────────────────
    public static final String INVENTORY_NOT_FOUND_TITLE      = "Inventory Not Found";
    public static final String PRODUCT_NOT_FOUND_TITLE        = "Product Not Found";
    public static final String PURCHASE_NOT_FOUND_TITLE       = "Purchase Request Not Found";
    public static final String SERVICE_UNAVAILABLE_TITLE      = "Service Unavailable";
    public static final String VALIDATION_ERROR_TITLE         = "Validation Error";
    public static final String INTERNAL_ERROR_TITLE           = "Internal Server Error";

    // ── Mensajes de detalle ────────────────────────────────────────────────────
    public static final String INVENTORY_NOT_FOUND_DETAIL     = "Inventory not found for product id: ";
    public static final String PRODUCT_NOT_FOUND_DETAIL       = "Product not found with id: ";
    public static final String PURCHASE_NOT_FOUND_DETAIL      = "Purchase request not found with id: ";
    public static final String PRODUCT_SERVICE_ERROR          = "Product service error: ";
    public static final String PRODUCT_SERVICE_UNAVAILABLE    = "Product service is unavailable";
    public static final String INSUFFICIENT_STOCK_AFTER       = "Insufficient stock after ";
    public static final String ATTEMPTS_SUFFIX                = " attempts";
    public static final String INVALID_OR_MISSING_API_KEY     = "Invalid or missing API key";

    // ── Códigos de estado ─────────────────────────────────────────────────────
    public static final String STATUS_400 = "400";
    public static final String STATUS_401 = "401";
    public static final String STATUS_404 = "404";
    public static final String STATUS_500 = "500";
    public static final String STATUS_503 = "503";
}
