package com.example.GreenFood.model;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table (name="cart")
public class Cart {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@OneToOne
	@JoinColumn(name ="customer_id", unique = true)
	private Customer customer;
	
	@OneToMany (mappedBy = "cart", cascade = CascadeType.ALL)
	private List<CartItem> listCartItems;

	public Cart() {
	}

	public Cart(Customer customer, List<CartItem> listCartItems) {
		this.customer = customer;
		this.listCartItems = listCartItems;
	}

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }
	public Customer getCustomer() { return customer; }
	public void setCustomer(Customer customer) { this.customer = customer; }
	public List<CartItem> getListCartItems() { return listCartItems; }
	public void setListCartItems(List<CartItem> listCartItems) { this.listCartItems = listCartItems; }
}
