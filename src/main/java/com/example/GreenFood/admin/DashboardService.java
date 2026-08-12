package com.example.GreenFood.admin;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.GreenFood.model.Category;
import com.example.GreenFood.model.Customer;
import com.example.GreenFood.model.Orders;
import com.example.GreenFood.model.Product;
import com.example.GreenFood.model.Recipe;
import com.example.GreenFood.product.CategoryRepository;
import com.example.GreenFood.user.CustomerRepository;
import com.example.GreenFood.order.OrderRepository;
import com.example.GreenFood.product.ProductRepository;
import com.example.GreenFood.product.RecipeRepository;

@Service
public class DashboardService {
	private final CategoryRepository categoryRepository;
	private final ProductRepository productRepository;
	private final CustomerRepository customerRepository;
	private final OrderRepository orderRepository;
	private final RecipeRepository recipeRepository;

	public DashboardService(CategoryRepository categoryRepository, ProductRepository productRepository,
			CustomerRepository customerRepository, OrderRepository orderRepository,
			RecipeRepository recipeRepository) {
		this.categoryRepository = categoryRepository;
		this.productRepository = productRepository;
		this.customerRepository = customerRepository;
		this.orderRepository = orderRepository;
		this.recipeRepository = recipeRepository;
	}

	public List<Category> getCategories() {
		return categoryRepository.findAll();
	}

	public List<Product> getProducts() {
		return productRepository.findAllWithCategory().stream()
				.sorted(Comparator.comparingInt(Product::getId).reversed())
				.toList();
	}

	public List<Customer> getCustomers() {
		return customerRepository.findAll().stream()
				.sorted(Comparator.comparingInt(Customer::getId).reversed())
				.toList();
	}

	public List<Orders> getOrders() {
		return orderRepository.findAll().stream()
				.sorted(Comparator.comparing(Orders::getOrderDate, Comparator.nullsLast(Comparator.reverseOrder())))
				.toList();
	}

	public List<Recipe> getRecipes() {
		return recipeRepository.findAll().stream()
				.sorted(Comparator.comparingInt(Recipe::getId).reversed())
				.toList();
	}
}
