package io.github.ghoshsa.mule.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.ghoshsa.mule.payment.dto.PaymentRequest;
import io.github.ghoshsa.mule.payment.dto.PaymentResponse;
import io.github.ghoshsa.mule.payment.service.PaymentService;
import jakarta.validation.Valid;

@RestController
@RequestMapping ("/payment")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> charge(@Valid @RequestBody PaymentRequest request) {
        boolean charged = paymentService.charge(request.orderId(), request.amount());
        return ResponseEntity.ok(new PaymentResponse(charged));
    }
}