package com.example.GreenFood.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "payment")
public class Payment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String status;
	private BigDecimal amount;
	
	@OneToOne
	@JoinColumn(name="order_id", unique = true)
	private Orders orders;
	
	private String method;
	private String transactionCode;

	public Payment() {
	}

	public Payment(String status, BigDecimal amount, Orders orders, String method, String transactionCode) {
		this.status = status;
		this.amount = amount;
		this.orders = orders;
		this.method = method;
		this.transactionCode = transactionCode;
	}

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }
	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
	public BigDecimal getAmount() { return amount; }
	public void setAmount(BigDecimal amount) { this.amount = amount; }
	public Orders getOrders() { return orders; }
	public void setOrders(Orders orders) { this.orders = orders; }
	public String getMethod() { return method; }
	public void setMethod(String method) { this.method = method; }
	public String getTransactionCode() { return transactionCode; }
	public void setTransactionCode(String transactionCode) { this.transactionCode = transactionCode; }
}
