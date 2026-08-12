package com.example.GreenFood.user;

import com.example.GreenFood.admin.DashboardService;
import com.example.GreenFood.product.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.GreenFood.model.Recipe;

@Controller
public class PageController {

    private final ProductService productService;
    private final DashboardService dashboardService;

    public PageController(ProductService productService, DashboardService dashboardService) {
        this.productService = productService;
        this.dashboardService = dashboardService;
    }

    @GetMapping({"/", "/index", "/index.html"})
    public String index(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "index";
    }

    @GetMapping({"/login", "/login.html"})
    public String login() {
        return "user/login";
    }

    @GetMapping({"/register", "/register.html"})
    public String register() {
        return "user/register";
    }

    @GetMapping({"/products", "/danh-sach-san-pham", "/danh-sach-san-pham.html"})
    public String products(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "product/danh-sach-san-pham";
    }

    @GetMapping({"/product-detail", "/chi-tiet-san-pham.html"})
    public String productDetail() {
        return "product/chi-tiet-san-pham";
    }

    @GetMapping({"/cart", "/giohang", "/giohang.html"})
    public String cart() {
        return "user/giohang";
    }

    @GetMapping({"/recipes", "/recipe", "/recipe.html", "/admin/recipes"})
    public String recipes() {
        return "product/recipe";
    }

    @GetMapping({"/goiymonan", "/goiymonan.html"})
    public String recipeSuggestion() {
        return "product/goiymonan";
    }
    @GetMapping({"/chi-tiet-mon-an", "/chi-tiet-mon-an.html"})
    public String recipeDetail() {
        return "product/chi-tiet-mon-an";
    }

    @GetMapping({"/history", "/lichsu", "/lichsu.html"})
    public String orderHistory() {
        return "user/lichsu";
    }

    @GetMapping({"/privacy", "/chinhsachbaomat", "/chinhsachbaomat.html"})
    public String privacyPolicy() {
        return "chinhsachbaomat";
    }

    @GetMapping({"/admin/products", "/qlad-sanpham", "/qlad-sanpham.html"})
    public String adminProducts(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "admin/qlad-sanpham";
    }

    @GetMapping({"/admin/customers", "/quanlyadmin-nguoidung", "/quanlyadmin-nguoidung.html"})
    public String adminCustomers(Model model) {
        model.addAttribute("customers", dashboardService.getCustomers());
        return "admin/quanlyadmin-nguoidung";
    }

    @GetMapping({"/account", "/quanlynguoidung1", "/quanlynguoidung1.html"})
    public String userAccount() {
        return "admin/quanlynguoidung1";
    }

    @GetMapping({"/account/orders", "/quanlynguoidung2", "/quanlynguoidung2.html"})
    public String userOrders() {
        return "admin/quanlynguoidung2";
    }

    @GetMapping({"/account/settings", "/quanlynguoidung3", "/quanlynguoidung3.html"})
    public String userSettings() {
        return "admin/quanlynguoidung3";
    }

    @GetMapping({"/footer", "/footer.html"})
    public String footer() {
        return "fragments/footer";
    }

    @GetMapping({"/paymentsuccess", "/paymentsuccess.html"})
    public String paymentSuccess() {
        return "user/paymentsuccess";
    }
}
