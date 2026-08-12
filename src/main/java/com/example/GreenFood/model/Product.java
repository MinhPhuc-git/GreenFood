package com.example.GreenFood.model;

import java.math.BigDecimal;

import jakarta.persistence.*;

@Entity
@Table(name = "product")
public class Product {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Column(name = "category_id", nullable = false)
	private int categoryId;
	private String name;
	private BigDecimal price;
	private BigDecimal stock;
	private String unit;
	private String status;
	private String description;
	private String image;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "category_id", insertable = false, updatable = false)
	private Category category;

	public Product() {
	}

	public Product(int categoryId, String name, BigDecimal price, BigDecimal stock, String unit, String status, String description, Category category) {
		this.categoryId = categoryId;
		this.name = name;
		this.price = price;
		this.stock = stock;
		this.unit = unit;
		this.status = status;
		this.description = description;
		this.category = category;
	}

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }
	public int getCategoryId() { return categoryId; }
	public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public BigDecimal getPrice() { return price; }
	public void setPrice(BigDecimal price) { this.price = price; }
	public BigDecimal getStock() { return stock; }
	public void setStock(BigDecimal stock) { this.stock = stock; }
	public String getUnit() { return unit; }
	public void setUnit(String unit) { this.unit = unit; }
	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public Category getCategory() { return category; }
	public void setCategory(Category category) { this.category = category; }
	public String getImage() { return image; }
	public void setImage(String image) { this.image = image; }
}
