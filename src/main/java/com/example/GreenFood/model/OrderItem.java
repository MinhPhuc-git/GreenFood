package com.example.GreenFood.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.math.BigDecimal;

@Entity
@Table (name = "orderitem")
public class OrderItem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private BigDecimal quantity;
	private BigDecimal unitPrice;
	
	@ManyToOne
	@JoinColumn (name="order_id")
	@JsonBackReference
	private Orders orders;
	
	@ManyToOne
	@JoinColumn (name="product_id")
	private Product product;

	public OrderItem() {
	}

	public OrderItem(BigDecimal quantity, BigDecimal unitPrice, Orders orders, Product product) {
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.orders = orders;
		this.product = product;
	}

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }
	public BigDecimal getQuantity() { return quantity; }
	public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
	public BigDecimal getUnitPrice() { return unitPrice; }
	public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
	public Orders getOrders() { return orders; }
	public void setOrders(Orders orders) { this.orders = orders; }
	public Product getProduct() { return product; }
	public void setProduct(Product product) { this.product = product; }
}
