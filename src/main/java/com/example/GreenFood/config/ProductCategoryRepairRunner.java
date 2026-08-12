package com.example.GreenFood.config;

import com.example.GreenFood.model.Category;
import com.example.GreenFood.model.Product;
import com.example.GreenFood.product.CategoryRepository;
import com.example.GreenFood.product.ProductRepository;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ProductCategoryRepairRunner implements ApplicationRunner {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductCategoryRepairRunner(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<Category> categories = categoryRepository.findAll();
        if (categories.isEmpty()) {
            return;
        }

        List<Product> products = productRepository.findAll();
        boolean changed = false;
        int index = 0;

        for (Product product : products) {
            int categoryId = product.getCategoryId();
            boolean invalid = categoryId <= 0 || !categoryRepository.existsById(categoryId);
            if (!invalid) {
                continue;
            }

            Category assigned = categories.get(index % categories.size());
            product.setCategoryId(assigned.getId());
            changed = true;
            index++;
        }

        if (changed) {
            productRepository.saveAll(products);
        }
    }
}
