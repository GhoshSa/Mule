package io.github.ghoshsa.mule.order.exception;

public class ProductNotFound extends RuntimeException {
    public ProductNotFound(String productId) {
        super("Product not found: " + productId);
    }
}