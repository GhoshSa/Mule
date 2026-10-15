package io.github.ghoshsa.mule.inventory.dto;

import java.math.BigDecimal;

public record InventoryItemResponse(
    String productId,
    BigDecimal price
) {}