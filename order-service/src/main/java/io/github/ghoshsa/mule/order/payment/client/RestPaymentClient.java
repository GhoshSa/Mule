package io.github.ghoshsa.mule.order.payment.client;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import io.github.ghoshsa.mule.order.payment.client.dto.PaymentRequest;
import io.github.ghoshsa.mule.order.payment.client.dto.PaymentResponse;

@Component
public class RestPaymentClient implements PaymentClient {
    private final RestClient restClient;

    public RestPaymentClient(RestClient.Builder builder, @Value ("${payment.service.base-url}") String url) {
        this.restClient = builder.baseUrl(url).build();
    }

    @Override
    public boolean chargePayment(Long orderId, BigDecimal amount) {
        PaymentResponse response = restClient.post().uri("/payment").body(new PaymentRequest(orderId, amount)).retrieve().body(PaymentResponse.class);
        return response.charged();
    }
}