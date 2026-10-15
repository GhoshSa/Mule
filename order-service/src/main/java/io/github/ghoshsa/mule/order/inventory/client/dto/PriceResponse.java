package io.github.ghoshsa.mule.order.inventory.client.dto;

import java.math.BigDecimal;

public record PriceResponse(
    BigDecimal price
) {}