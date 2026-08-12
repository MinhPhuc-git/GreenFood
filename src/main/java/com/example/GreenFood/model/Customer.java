package com.example.GreenFood.model;

import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "customer")
@PrimaryKeyJoinColumn(name = "id")
public class Customer extends User{
	@Column(nullable = false)
	private String name;
	
	@Column(nullable = false, unique = true)
	private String phone;

	@Column(name = "loyalty_points", nullable = false)
	private int loyaltyPoints = 0;
	
	@OneToMany (mappedBy = "customer")
	private List<CustomerAddress> customerAdrresses;

	public Customer() {
	}

	public Customer(String name, String phone, List<CustomerAddress> customerAdrresses) {
		this.name = name;
		this.phone = phone;
		this.customerAdrresses = customerAdrresses;
	}

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getPhone() { return phone; }
	public void setPhone(String phone) { this.phone = phone; }
	public int getLoyaltyPoints() { return loyaltyPoints; }
	public void setLoyaltyPoints(int loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }
	public List<CustomerAddress> getCustomerAdrresses() { return customerAdrresses; }
	public void setCustomerAdrresses(List<CustomerAddress> customerAdrresses) { this.customerAdrresses = customerAdrresses; }

	public Customer(String account, String pwd, String status, java.time.LocalDateTime createdAt, String name, String phone, List<CustomerAddress> customerAdrresses) {
		super(account, pwd, status, createdAt);
		this.name = name;
		this.phone = phone;
		this.customerAdrresses = customerAdrresses;
	}
}
