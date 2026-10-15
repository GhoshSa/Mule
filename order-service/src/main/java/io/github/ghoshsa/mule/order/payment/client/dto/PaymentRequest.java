package io.github.ghoshsa.mule.order.payment.client.dto;

import java.math.BigDecimal;

public record PaymentRequest(
    Long orderId,
    BigDecimal amount
) {}