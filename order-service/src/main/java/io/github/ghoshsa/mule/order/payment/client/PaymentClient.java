package io.github.ghoshsa.mule.order.payment.client;

import java.math.BigDecimal;

public interface PaymentClient {
    boolean chargePayment(Long orderId, BigDecimal amount);
}