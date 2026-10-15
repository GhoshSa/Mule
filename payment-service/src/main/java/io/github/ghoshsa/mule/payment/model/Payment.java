package io.github.ghoshsa.mule.payment.model;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table (name = "Payments")
@Getter
@Setter
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (nullable = false)
    private Long orderId;

    @Column (nullable = false)
    private BigDecimal amount;

    @Column (nullable = false)
    @Enumerated (EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Column (nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Payment(Long orderId, BigDecimal amount, PaymentStatus status) {
        this.orderId = orderId;
        this.amount = amount;
        this.paymentStatus = status;
    }
}