package io.github.ghoshsa.mule.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ReserveStockRequest(@NotBlank String productId, @Min (1) int quantity) {}