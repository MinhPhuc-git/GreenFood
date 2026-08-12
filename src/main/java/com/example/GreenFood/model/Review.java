package com.example.GreenFood.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table (name = "review")
public class Review {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	private int rating;
	private String comment;
	private LocalDateTime reviewDate;
	
	@ManyToOne
	@JoinColumn (name="customer_id")
	private Customer customer;
	
	@ManyToOne
	@JoinColumn (name="product_id")
	private Product product;

	public Review() {
	}

	public Review(int rating, String comment, LocalDateTime reviewDate, Customer customer, Product product) {
		this.rating = rating;
		this.comment = comment;
		this.reviewDate = reviewDate;
		this.customer = customer;
		this.product = product;
	}

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }
	public int getRating() { return rating; }
	public void setRating(int rating) { this.rating = rating; }
	public String getComment() { return comment; }
	public void setComment(String comment) { this.comment = comment; }
	public LocalDateTime getReviewDate() { return reviewDate; }
	public void setReviewDate(LocalDateTime reviewDate) { this.reviewDate = reviewDate; }
	public Customer getCustomer() { return customer; }
	public void setCustomer(Customer customer) { this.customer = customer; }
	public Product getProduct() { return product; }
	public void setProduct(Product product) { this.product = product; }
}
