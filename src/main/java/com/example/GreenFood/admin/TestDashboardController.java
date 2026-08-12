package com.example.GreenFood.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.GreenFood.admin.DashboardService;
import java.util.HashMap;
import java.util.Map;

@RestController
public class TestDashboardController {
    private final DashboardService dashboardService;

    public TestDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/api/test-dashboard")
    public Map<String, Object> test() {
        System.out.println("DEBUG: Starting test dashboard queries...");
        Map<String, Object> map = new HashMap<>();
        
        System.out.println("DEBUG: Querying categories...");
        map.put("categories_count", dashboardService.getCategories().size());
        
        System.out.println("DEBUG: Querying products...");
        map.put("products_count", dashboardService.getProducts().size());
        
        System.out.println("DEBUG: Querying customers...");
        map.put("customers_count", dashboardService.getCustomers().size());
        
        System.out.println("DEBUG: Querying orders...");
        map.put("orders_count", dashboardService.getOrders().size());

        System.out.println("DEBUG: Querying recipes...");
        map.put("recipes_count", dashboardService.getRecipes().size());
        
        System.out.println("DEBUG: Test dashboard queries completed!");
        return map;
    }
}
