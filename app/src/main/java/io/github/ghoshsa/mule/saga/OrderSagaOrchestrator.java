package io.github.ghoshsa.mule.saga;

import java.util.List;

import org.springframework.stereotype.Component;

import io.github.ghoshsa.mule.inventory.service.InventoryService;
import io.github.ghoshsa.mule.order.model.Order;
import io.github.ghoshsa.mule.order.model.OrderItem;
import io.github.ghoshsa.mule.order.service.OrderService;

@Component
public class OrderSagaOrchestrator {
    private final OrderService orderService;
    private final InventoryService inventoryService;

    public OrderSagaOrchestrator(OrderService orderService, InventoryService inventoryService) {
        this.orderService = orderService;
        this.inventoryService = inventoryService;
    }

    public Order placeOrder(String customerId, List<OrderItem> orderItems) {
        Order order = orderService.createOrder(customerId, orderItems);

        boolean reservedSuccessfully = true;
        for (OrderItem item : orderItems) {
            boolean reserved = inventoryService.reserve(item.getProductId(), item.getQuantity());
            if (!reserved) {
                reservedSuccessfully = false;
                break;
            }
        }

        if (reservedSuccessfully) {
            order = orderService.markConfirmed(order.getId());
        } else {
            order = orderService.markFailed(order.getId());
        }

        return order;
    }
}