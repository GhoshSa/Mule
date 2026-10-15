package io.github.ghoshsa.mule.order.exception;

public class OrderNotFound extends RuntimeException {
    public OrderNotFound(Long id) {
        super("Order not found: " + id);
    }
}