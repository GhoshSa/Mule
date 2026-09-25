package io.github.ghoshsa.mule.order.dto;

import java.time.Instant;
import java.util.List;

import io.github.ghoshsa.mule.order.model.Order;
import io.github.ghoshsa.mule.order.model.OrderStatus;

public record OrderResponse(Long id, String customerId, OrderStatus status, Instant createdAt, List<OrderItemRequest> orderItems) {
    public static OrderResponse from(Order order) {
        List<OrderItemRequest> items = order.getOrderItems().stream().map(i -> (
            new OrderItemRequest(i.getProductId(), i.getQuantity())
        )).toList();

        return new OrderResponse(order.getId(), order.getCustomerId(), order.getOrderStatus(), order.getCreatedAt(), items);
    }
}