package com.jsbb.productservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jsbb.productservice.dto.request.ProductRequest;
import com.jsbb.productservice.dto.response.ProductResponse;
import com.jsbb.productservice.config.ApiKeyFilter;
import com.jsbb.productservice.config.SecurityConfig;
import com.jsbb.productservice.exception.GlobalExceptionHandler;
import com.jsbb.productservice.exception.ProductNotFoundException;
import com.jsbb.productservice.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProductController.class)
@Import({SecurityConfig.class, ApiKeyFilter.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = "security.api-key=test-key")
class ProductControllerTest {

    private static final String API_KEY = "test-key";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService service;

    @Test
    void create_shouldReturn201WithJsonApiFormat() throws Exception {
        ProductRequest request = new ProductRequest("Laptop", 1500.0, "Gaming");
        ProductResponse response = new ProductResponse(1L, "Laptop", 1500.0, "Gaming");
        when(service.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/products")
                        .header("X-API-KEY", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("1"))
                .andExpect(jsonPath("$.data.type").value("products"))
                .andExpect(jsonPath("$.data.attributes.name").value("Laptop"))
                .andExpect(jsonPath("$.data.attributes.price").value(1500.0));
    }

    @Test
    void create_shouldReturn400_whenNameIsBlank() throws Exception {
        ProductRequest request = new ProductRequest("", 1500.0, "Gaming");

        mockMvc.perform(post("/api/v1/products")
                        .header("X-API-KEY", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].status").value("400"));
    }

    @Test
    void getById_shouldReturn200WithJsonApiFormat() throws Exception {
        ProductResponse response = new ProductResponse(1L, "Laptop", 1500.0, "Gaming");
        when(service.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/products/1")
                        .header("X-API-KEY", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("1"))
                .andExpect(jsonPath("$.data.type").value("products"))
                .andExpect(jsonPath("$.data.attributes.name").value("Laptop"));
    }

    @Test
    void getById_shouldReturn404_whenNotFound() throws Exception {
        when(service.getById(99L)).thenThrow(new ProductNotFoundException(99L));

        mockMvc.perform(get("/api/v1/products/99")
                        .header("X-API-KEY", API_KEY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].status").value("404"))
                .andExpect(jsonPath("$.errors[0].title").value("Product Not Found"));
    }

    @Test
    void getAll_shouldReturn200WithJsonApiListFormat() throws Exception {
        List<ProductResponse> products = List.of(
                new ProductResponse(1L, "Laptop", 1500.0, "Gaming"),
                new ProductResponse(2L, "Mouse", 50.0, null)
        );
        when(service.getAll()).thenReturn(products);

        mockMvc.perform(get("/api/v1/products")
                        .header("X-API-KEY", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].attributes.name").value("Laptop"))
                .andExpect(jsonPath("$.data[1].attributes.name").value("Mouse"));
    }

    @Test
    void shouldReturn401_whenApiKeyIsMissing() throws Exception {
        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isUnauthorized());
    }
}
