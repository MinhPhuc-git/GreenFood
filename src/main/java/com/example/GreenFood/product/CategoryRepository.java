package com.example.GreenFood.product;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.GreenFood.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
}
