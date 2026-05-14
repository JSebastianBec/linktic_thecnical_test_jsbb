package com.jsbb.inventoryservice.repository;

import com.jsbb.inventoryservice.model.PurchaseRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequest, UUID> {
}
