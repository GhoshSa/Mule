package io.github.ghoshsa.mule.inventory.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.ghoshsa.mule.inventory.dto.InventoryItemResponse;
import io.github.ghoshsa.mule.inventory.dto.ReserveStockRequest;
import io.github.ghoshsa.mule.inventory.dto.ReserveStockResponse;
import io.github.ghoshsa.mule.inventory.model.InventoryItem;
import io.github.ghoshsa.mule.inventory.service.InventoryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping ("/inventory")
public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping ("/{productId}")
    public ResponseEntity<InventoryItemResponse> getItem(@PathVariable String productId) {
        InventoryItem item = inventoryService.getItem(productId);
        return ResponseEntity.ok(new InventoryItemResponse(item.getProductId(), item.getPrice()));
    }

    @PostMapping ("/reserve")
    public ResponseEntity<ReserveStockResponse> reserveStock(@Valid @RequestBody ReserveStockRequest request) {
        boolean reserved = inventoryService.reserve(request.productId(), request.quantity());
        return ResponseEntity.ok(new ReserveStockResponse(reserved));
    }
}