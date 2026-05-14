package com.jsbb.inventoryservice.client;

import com.jsbb.inventoryservice.exception.ProductNotFoundException;
import com.jsbb.inventoryservice.exception.ProductServiceUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductClientTest {

    @Test
    void getProduct_shouldThrowProductNotFoundException_on404() {
        ProductClient client = new ProductClient("http://localhost:8080", "key") {
            @Override
            public com.jsbb.inventoryservice.dto.response.ProductResponse getProduct(Long productId) {
                throw new WebClientResponseException(HttpStatus.NOT_FOUND.value(), "Not Found", null, null, null);
            }
        };

        assertThatThrownBy(() -> client.getProduct(99L))
                .isInstanceOf(WebClientResponseException.class);
    }

    @Test
    void productNotFoundException_shouldContainProductId() {
        ProductNotFoundException ex = new ProductNotFoundException(42L);
        org.assertj.core.api.Assertions.assertThat(ex.getMessage()).contains("42");
    }

    @Test
    void productServiceUnavailableException_shouldContainMessage() {
        ProductServiceUnavailableException ex = new ProductServiceUnavailableException("Service down");
        org.assertj.core.api.Assertions.assertThat(ex.getMessage()).isEqualTo("Service down");
    }
}
