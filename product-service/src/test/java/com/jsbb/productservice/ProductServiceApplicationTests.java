package com.jsbb.productservice;

import com.jsbb.productservice.exception.ApiErrorResponse;
import com.jsbb.productservice.exception.ProductNotFoundException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductServiceApplicationTests {

    @Test
    void apiErrorResponse_shouldContainCorrectFields() {
        ApiErrorResponse response = ApiErrorResponse.of("404", "Not Found", "detail");
        assertThat(response.errors()).hasSize(1);
        assertThat(response.errors().get(0).status()).isEqualTo("404");
        assertThat(response.timestamp()).isNotNull();
    }

    @Test
    void productNotFoundException_shouldContainId() {
        ProductNotFoundException ex = new ProductNotFoundException(42L);
        assertThat(ex.getMessage()).contains("42");
    }
}
