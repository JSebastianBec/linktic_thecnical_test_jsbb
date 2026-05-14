package com.jsbb.productservice.service;

import com.jsbb.productservice.dto.request.ProductRequest;
import com.jsbb.productservice.dto.response.ProductResponse;
import com.jsbb.productservice.exception.ProductNotFoundException;
import com.jsbb.productservice.messaging.MessagingConfig;
import com.jsbb.productservice.messaging.ProductCreatedEvent;
import com.jsbb.productservice.model.Product;
import com.jsbb.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository repository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = Product.builder()
                .name(request.name())
                .price(request.price())
                .description(request.description())
                .build();

        Product saved = repository.save(product);
        log.info("Product created: id={}, name={}", saved.getId(), saved.getName());

        rabbitTemplate.convertAndSend(
                MessagingConfig.PRODUCTS_EXCHANGE,
                "product.created",
                new ProductCreatedEvent(saved.getId(), saved.getName())
        );

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAll() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getDescription()
        );
    }
}
