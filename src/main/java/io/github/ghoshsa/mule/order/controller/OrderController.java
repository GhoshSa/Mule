package io.github.ghoshsa.mule.order.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import io.github.ghoshsa.mule.order.dto.CreateOrderRequest;
import io.github.ghoshsa.mule.order.dto.OrderResponse;
import io.github.ghoshsa.mule.order.model.Order;
import io.github.ghoshsa.mule.order.model.OrderItem;
import io.github.ghoshsa.mule.order.service.OrderService;
import io.github.ghoshsa.mule.saga.OrderSagaOrchestrator;
import jakarta.validation.Valid;

@RestController
@RequestMapping ("/orders")
public class OrderController {
    private final OrderSagaOrchestrator orchestrator;
    private final OrderService orderService;

    public OrderController(OrderSagaOrchestrator orchestrator, OrderService orderService) {
        this.orchestrator = orchestrator;
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody @Valid CreateOrderRequest request) {
        List<OrderItem> items = request.items().stream().map(i -> {
            OrderItem item = new OrderItem();
            item.setProductId(i.productId());
            item.setQuantity(i.quantity());
            return item;
        }).toList();

        Order order = orchestrator.placeOrder(request.customerId(), items);
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    @GetMapping ("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        Order order = orderService.getOrder(id);
        return ResponseEntity.ok(OrderResponse.from(order));
    }
}