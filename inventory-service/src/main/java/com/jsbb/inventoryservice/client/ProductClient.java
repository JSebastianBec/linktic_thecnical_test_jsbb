package com.jsbb.inventoryservice.client;

import com.jsbb.inventoryservice.dto.response.ProductResponse;
import com.jsbb.inventoryservice.exception.ProductNotFoundException;
import com.jsbb.inventoryservice.exception.ProductServiceUnavailableException;
import com.jsbb.inventoryservice.util.ApiConstants;
import com.jsbb.inventoryservice.util.ErrorMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.Map;

@Component
@Slf4j
public class ProductClient {

    private final WebClient webClient;

    public ProductClient(@Value("${product.service.url}") String baseUrl,
                         @Value("${product.service.api-key}") String apiKey) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(ApiConstants.API_KEY_HEADER, apiKey)
                .build();
    }

    public ProductResponse getProduct(Long productId) {
        log.info("Fetching product from product-service: id={}", productId);
        try {
            Map response = webClient.get()
                    .uri(ApiConstants.PRODUCTS_PATH, productId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(3))
                    .retry(2)
                    .block();

            Map data = (Map) response.get("data");
            Map attributes = (Map) data.get("attributes");

            return new ProductResponse(
                    productId,
                    (String) attributes.get("name"),
                    ((Number) attributes.get("price")).doubleValue(),
                    (String) attributes.get("description")
            );
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new ProductNotFoundException(productId);
            }
            log.error("Error calling product-service: status={}", e.getStatusCode());
            throw new ProductServiceUnavailableException(ErrorMessages.PRODUCT_SERVICE_ERROR + e.getStatusCode());
        } catch (Exception e) {
            log.error("Product service unavailable: {}", e.getMessage());
            throw new ProductServiceUnavailableException(ErrorMessages.PRODUCT_SERVICE_UNAVAILABLE);
        }
    }
}
