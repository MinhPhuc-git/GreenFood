package com.example.GreenFood.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.GreenFood.admin.DashboardService;

@Controller
public class DashboardController {
	private final DashboardService dashboardService;

	public DashboardController(DashboardService dashboardService) {
		this.dashboardService = dashboardService;
	}

	@GetMapping({"/dashboard", "/admin"})
	public String showDashboard(Model model) {
		model.addAttribute("products", dashboardService.getProducts());
		model.addAttribute("customers", dashboardService.getCustomers());
		model.addAttribute("orders", dashboardService.getOrders());
		model.addAttribute("recipes", dashboardService.getRecipes());
		return "admin/dashboard";
	}

	@GetMapping("/test-dashboard")
	public String testDashboard(Model model) {
		model.addAttribute("products", dashboardService.getProducts());
		model.addAttribute("customers", dashboardService.getCustomers());
		model.addAttribute("orders", dashboardService.getOrders());
		model.addAttribute("recipes", dashboardService.getRecipes());
		return "admin/test-dashboard";
	}
}
