package io.github.ghoshsa.mule.inventory.service;

import org.springframework.stereotype.Service;

import io.github.ghoshsa.mule.inventory.repository.InventoryRepository;

@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public boolean reserve(String productId, int quantity) {
        int rowsAffected = inventoryRepository.reserveStock(productId, quantity);
        return rowsAffected > 0;
    }
}