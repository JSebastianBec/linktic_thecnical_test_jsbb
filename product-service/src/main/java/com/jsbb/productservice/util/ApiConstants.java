package com.jsbb.productservice.util;

public class ApiConstants {

    private ApiConstants() {}

    // ── Base paths ─────────────────────────────────────────────────────────────
    public static final String PRODUCTS_BASE_PATH = "/api/v1/products";

    // ── JSON API resource types ────────────────────────────────────────────────
    public static final String TYPE_PRODUCTS = "products";

    // ── Security ───────────────────────────────────────────────────────────────
    public static final String API_KEY_HEADER = "X-API-KEY";
    public static final String UNAUTHORIZED_BODY =
            "{\"errors\":[{\"status\":\"401\",\"title\":\"Unauthorized\",\"detail\":\"Invalid or missing API key\"}]}";

    // ── Paths excluidos de autenticación ──────────────────────────────────────
    public static final String SWAGGER_UI_PATH   = "/swagger-ui/**";
    public static final String API_DOCS_PATH     = "/v3/api-docs/**";
    public static final String ACTUATOR_PATH     = "/actuator/**";
}
