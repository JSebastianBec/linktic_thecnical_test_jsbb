package com.jsbb.inventoryservice.util;

public class ApiConstants {

    private ApiConstants() {}

    // ── Base paths ─────────────────────────────────────────────────────────────
    public static final String INVENTORY_BASE_PATH   = "/api/v1/inventory";
    public static final String PRODUCTS_PATH         = "/api/v1/products/{id}";

    // ── JSON API resource types ────────────────────────────────────────────────
    public static final String TYPE_INVENTORY = "inventory";
    public static final String TYPE_PURCHASES = "purchases";

    // ── Security ───────────────────────────────────────────────────────────────
    public static final String API_KEY_HEADER = "X-API-KEY";
    public static final String UNAUTHORIZED_BODY =
            "{\"errors\":[{\"status\":\"401\",\"title\":\"Unauthorized\",\"detail\":\"Invalid or missing API key\"}]}";

    // ── Paths excluidos de autenticación ──────────────────────────────────────
    public static final String SWAGGER_UI_PATH = "/swagger-ui/**";
    public static final String API_DOCS_PATH   = "/v3/api-docs/**";
    public static final String ACTUATOR_PATH   = "/actuator/**";

    // ── Claves de argumentos RabbitMQ ─────────────────────────────────────────
    public static final String RABBIT_ARG_MESSAGE_TTL         = "x-message-ttl";
    public static final String RABBIT_ARG_DEAD_LETTER_EXCHANGE = "x-dead-letter-exchange";
    public static final String RABBIT_ARG_DEAD_LETTER_ROUTING  = "x-dead-letter-routing-key";
}
