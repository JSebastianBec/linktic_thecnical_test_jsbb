package com.jsbb.inventoryservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "purchase_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseRequest {

    @Id
    private UUID id;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PurchaseStatus status;

    private String failureReason;

    @Column(nullable = false)
    private Integer attemptCount;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant resolvedAt;

    public enum PurchaseStatus {
        PENDING, COMPLETED, FAILED
    }
}
