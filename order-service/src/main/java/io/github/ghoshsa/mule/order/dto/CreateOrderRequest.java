package io.github.ghoshsa.mule.order.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOrderRequest(@NotBlank String customerId, @Size (min = 1, max = 1) @Valid List<OrderItemRequest> items) {}