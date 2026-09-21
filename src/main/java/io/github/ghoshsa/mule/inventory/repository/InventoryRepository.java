package io.github.ghoshsa.mule.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import io.github.ghoshsa.mule.inventory.model.InventoryItem;

public interface InventoryRepository extends JpaRepository<InventoryItem, String> {
    @Modifying
    @Transactional
    @Query ("""
        UPDATE InventoryItem i SET i.availableQuantity = i.availableQuantity - :quantity WHERE i.productId = :productId AND i.availableQuantity >= :quantity
    """)
    int reserveStock(String productId, int quantity);
}