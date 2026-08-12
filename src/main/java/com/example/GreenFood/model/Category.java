package com.example.GreenFood.model;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "category")
public class Category {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String name;
	
	@OneToMany (mappedBy = "category", cascade = CascadeType.ALL)
	@com.fasterxml.jackson.annotation.JsonIgnore
	private List<Product> listProducts;

	public Category() {
	}

	public Category(String name, List<Product> listProducts) {
		this.name = name;
		this.listProducts = listProducts;
	}

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public List<Product> getListProducts() { return listProducts; }
	public void setListProducts(List<Product> listProducts) { this.listProducts = listProducts; }
}

