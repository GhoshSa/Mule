package io.github.ghoshsa.mule.payment.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import io.github.ghoshsa.mule.payment.model.Payment;
import io.github.ghoshsa.mule.payment.model.PaymentStatus;
import io.github.ghoshsa.mule.payment.repository.PaymentRepository;

@Service
public class PaymentService {
    private static final BigDecimal DECLINE_THRESHOLD = new BigDecimal("500");

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public boolean charge(Long orderId, BigDecimal amount) {
        PaymentStatus status = amount.compareTo(DECLINE_THRESHOLD) >= 0 ? PaymentStatus.DECLINED : PaymentStatus.SUCCEEDED;
        paymentRepository.save(new Payment(orderId, amount, status));
        return status == PaymentStatus.SUCCEEDED;
    }
}