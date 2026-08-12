package com.example.GreenFood.order;

import com.example.GreenFood.model.Cart;
import com.example.GreenFood.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {
    Optional<Cart> findByCustomer(Customer customer);
}