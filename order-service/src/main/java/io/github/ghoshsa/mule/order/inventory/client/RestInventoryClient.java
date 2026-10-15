package io.github.ghoshsa.mule.order.inventory.client;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import io.github.ghoshsa.mule.order.exception.ProductNotFound;
import io.github.ghoshsa.mule.order.inventory.client.dto.PriceResponse;
import io.github.ghoshsa.mule.order.inventory.client.dto.ReservationRequest;
import io.github.ghoshsa.mule.order.inventory.client.dto.ReservationResponse;

@Component
public class RestInventoryClient implements InventoryClient {
    private final RestClient restClient;

    public RestInventoryClient(RestClient.Builder builder, @Value ("${inventory.service.base-url}") String url) {
        this.restClient = builder.baseUrl(url).build();
    }

    @Override
    public BigDecimal getPrice(String productId) {
        try {
            PriceResponse response = restClient.get().uri("/inventory/{ProductId}", productId).retrieve().body(PriceResponse.class);
            return response.price();
        } catch (HttpClientErrorException.NotFound exception) {
            throw new ProductNotFound(productId);
        }
    }

    @Override
    public boolean reserveStock(String productId, int quantity) {
        ReservationResponse response = restClient.post().uri("/inventory/reserve").body(new ReservationRequest(productId, quantity)).retrieve().body(ReservationResponse.class);
        return response.reserved();
    }
}