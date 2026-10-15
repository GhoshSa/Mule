package io.github.ghoshsa.mule.order.saga;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;

import io.github.ghoshsa.mule.order.inventory.client.InventoryClient;
import io.github.ghoshsa.mule.order.model.Order;
import io.github.ghoshsa.mule.order.model.OrderItem;
import io.github.ghoshsa.mule.order.payment.client.PaymentClient;
import io.github.ghoshsa.mule.order.service.OrderService;

@Component
public class OrderSagaOrchestrator {
    private final OrderService orderService;
    private final InventoryClient inventoryClient;
    private final PaymentClient paymentClient;

    public OrderSagaOrchestrator(OrderService orderService, InventoryClient inventoryClient, PaymentClient paymentClient) {
        this.orderService = orderService;
        this.inventoryClient = inventoryClient;
        this.paymentClient = paymentClient;
    }

    public Order placeOrder(String customerId, List<OrderItem> orderItems) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItem item : orderItems) {
            BigDecimal unitPrice = inventoryClient.getPrice(item.getProductId());
            totalAmount = totalAmount.add(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        Order order = orderService.createOrder(customerId, orderItems, totalAmount);

        boolean reservedSuccessfully = true;
        for (OrderItem item : orderItems) {
            boolean reserved = inventoryClient.reserveStock(item.getProductId(), item.getQuantity());
            if (!reserved) {
                reservedSuccessfully = false;
                break;
            }
        }

        if (!reservedSuccessfully) {
            return orderService.markFailed(order.getId());
        }

        boolean charged = paymentClient.chargePayment(order.getId(), order.getTotalAmount());

        return charged ? orderService.markConfirmed(order.getId()) : orderService.markFailed(order.getId());
    }
}