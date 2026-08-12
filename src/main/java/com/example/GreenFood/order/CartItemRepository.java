package com.example.GreenFood.order;

import com.example.GreenFood.model.Cart;
import com.example.GreenFood.model.CartItem;
import com.example.GreenFood.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
}