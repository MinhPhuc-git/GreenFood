package com.example.GreenFood.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table (name="cartitem")
public class CartItem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	private BigDecimal quantity;
	
	@ManyToOne
	@JoinColumn (name="product_id")
	private Product product;
	
	@ManyToOne
	@JoinColumn (name="cart_id")
	@com.fasterxml.jackson.annotation.JsonIgnore
	private Cart cart;

	public CartItem() {
	}

	public CartItem(BigDecimal quantity, Product product, Cart cart) {
		this.quantity = quantity;
		this.product = product;
		this.cart = cart;
	}

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }
	public BigDecimal getQuantity() { return quantity; }
	public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
	public Product getProduct() { return product; }
	public void setProduct(Product product) { this.product = product; }
	public Cart getCart() { return cart; }
	public void setCart(Cart cart) { this.cart = cart; }
}
