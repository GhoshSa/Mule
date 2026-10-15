package io.github.ghoshsa.mule.payment.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(@NotNull Long orderId, @NotNull @DecimalMin ("0.01") BigDecimal amount) {}