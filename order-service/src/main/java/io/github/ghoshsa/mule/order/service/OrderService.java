package io.github.ghoshsa.mule.order.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.github.ghoshsa.mule.order.exception.OrderNotFound;
import io.github.ghoshsa.mule.order.model.Order;
import io.github.ghoshsa.mule.order.model.OrderItem;
import io.github.ghoshsa.mule.order.model.OrderStatus;
import io.github.ghoshsa.mule.order.repository.OrderRepository;

@Service
public class OrderService {
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Order createOrder(String customerId, List<OrderItem> orderItems, BigDecimal amount) {
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setTotalAmount(amount);
        orderItems.forEach(order::addItem);
        return orderRepository.save(order);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Order markConfirmed(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFound(orderId));
        order.setOrderStatus(OrderStatus.CONFIRMED);
        return orderRepository.save(order);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Order markFailed(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFound(orderId));
        order.setOrderStatus(OrderStatus.FAILED);
        return orderRepository.save(order);
    }

    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFound(orderId));
    }
}