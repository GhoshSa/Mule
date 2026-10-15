package io.github.ghoshsa.mule.inventory;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import io.github.ghoshsa.mule.inventory.model.InventoryItem;
import io.github.ghoshsa.mule.inventory.repository.InventoryRepository;

@Component
public class DataSeeder implements CommandLineRunner {
    private final InventoryRepository inventoryRepository;

    public DataSeeder(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public void run(String... args) {
        seedIfMissing("product-1", 10, new BigDecimal("4.99"));
        seedIfMissing("product-2", 0, new BigDecimal("1.99"));
        seedIfMissing("product-concurrent", 10, new BigDecimal("1.99"));
    }

    private void seedIfMissing(String productId, int quantity, BigDecimal price) {
        if (!inventoryRepository.existsById(productId)) {
            inventoryRepository.save(new InventoryItem(productId, quantity, price));
        }
    }
}