package com.jsbb.productservice.controller;

import com.jsbb.productservice.dto.request.ProductRequest;
import com.jsbb.productservice.dto.response.JsonApiData;
import com.jsbb.productservice.dto.response.JsonApiListResponse;
import com.jsbb.productservice.dto.response.JsonApiResponse;
import com.jsbb.productservice.dto.response.ProductResponse;
import com.jsbb.productservice.service.ProductService;
import com.jsbb.productservice.util.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.PRODUCTS_BASE_PATH)
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product management endpoints")
public class ProductController {

    private final ProductService service;

    @Operation(summary = "Create a new product")
    @PostMapping
    public ResponseEntity<JsonApiResponse<ProductResponse>> create(
            @Valid @RequestBody ProductRequest request) {
        ProductResponse product = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(JsonApiResponse.of(String.valueOf(product.id()), ApiConstants.TYPE_PRODUCTS, product));
    }

    @Operation(summary = "Get product by ID")
    @GetMapping("/{id}")
    public ResponseEntity<JsonApiResponse<ProductResponse>> getById(@PathVariable Long id) {
        ProductResponse product = service.getById(id);
        return ResponseEntity.ok(
                JsonApiResponse.of(String.valueOf(product.id()), ApiConstants.TYPE_PRODUCTS, product));
    }

    @Operation(summary = "List all products")
    @GetMapping
    public ResponseEntity<JsonApiListResponse<ProductResponse>> getAll() {
        var items = service.getAll().stream()
                .map(p -> new JsonApiData<>(String.valueOf(p.id()), ApiConstants.TYPE_PRODUCTS, p))
                .toList();
        return ResponseEntity.ok(JsonApiListResponse.of(items));
    }
}
