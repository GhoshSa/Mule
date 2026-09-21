package io.github.ghoshsa.mule.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.ghoshsa.mule.order.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {}