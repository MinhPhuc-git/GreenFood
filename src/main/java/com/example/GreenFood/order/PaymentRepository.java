package com.example.GreenFood.order;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.GreenFood.model.Orders;
import com.example.GreenFood.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
	Optional<Payment> findByOrders(Orders orders);
}
