package io.github.ghoshsa.mule.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.ghoshsa.mule.payment.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {}