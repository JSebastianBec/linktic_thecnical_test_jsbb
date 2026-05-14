package com.jsbb.productservice.service;

import com.jsbb.productservice.dto.request.ProductRequest;
import com.jsbb.productservice.dto.response.ProductResponse;
import com.jsbb.productservice.exception.ProductNotFoundException;
import com.jsbb.productservice.model.Product;
import com.jsbb.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private ProductService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create_shouldSaveProductAndPublishEvent() {
        ProductRequest request = new ProductRequest("Laptop", 1500.0, "Gaming laptop");

        Product saved = Product.builder().id(1L).name("Laptop").price(1500.0)
                .description("Gaming laptop").build();
        when(repository.save(any())).thenReturn(saved);

        ProductResponse result = service.create(request);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Laptop");
        verify(repository).save(any());
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void getById_shouldReturnProduct_whenExists() {
        Product product = Product.builder().id(1L).name("Laptop").price(1500.0).build();
        when(repository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse result = service.getById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Laptop");
    }

    @Test
    void getById_shouldThrow_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAll_shouldReturnAllProducts() {
        List<Product> products = List.of(
                Product.builder().id(1L).name("Laptop").price(1500.0).build(),
                Product.builder().id(2L).name("Mouse").price(50.0).build()
        );
        when(repository.findAll()).thenReturn(products);

        List<ProductResponse> result = service.getAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Laptop");
    }
}
