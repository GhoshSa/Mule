package io.github.ghoshsa.mule.order.inventory.client.dto;

public record ReservationRequest(String productId, int quantity) {}