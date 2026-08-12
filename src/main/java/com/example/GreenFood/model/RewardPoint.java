package com.example.GreenFood.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table (name = "rewardpoint")
public class RewardPoint {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	private int points;
	private LocalDateTime expiryDate;
	
	@ManyToOne
	@JoinColumn (name="customer_id")
	private Customer customer;

	@OneToOne
	@JoinColumn(name = "order_id", unique = true)
	private Orders orders;

	public RewardPoint() {
	}

	public RewardPoint(int points, LocalDateTime expiryDate, Customer customer, Orders orders) {
		this.points = points;
		this.expiryDate = expiryDate;
		this.customer = customer;
		this.orders = orders;
	}

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }
	public int getPoints() { return points; }
	public void setPoints(int points) { this.points = points; }
	public LocalDateTime getExpiryDate() { return expiryDate; }
	public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }
	public Customer getCustomer() { return customer; }
	public void setCustomer(Customer customer) { this.customer = customer; }
	public Orders getOrders() { return orders; }
	public void setOrders(Orders orders) { this.orders = orders; }
}
