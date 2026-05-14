package com.jsbb.inventoryservice.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer stock;

    public boolean hasEnoughStock(int quantity) {
        return this.stock >= quantity;
    }

    public void deduct(int quantity) {
        this.stock -= quantity;
    }

    public void add(int quantity) {
        this.stock += quantity;
    }
}
