package io.github.ghoshsa.mule.order.inventory.client;

import java.math.BigDecimal;

public interface InventoryClient {
    BigDecimal getPrice(String productId);
    boolean reserveStock(String productId, int quantity);
}