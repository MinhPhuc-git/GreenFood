package com.example.GreenFood.admin;

import com.example.GreenFood.model.Customer;
import com.example.GreenFood.model.User;
import com.example.GreenFood.user.CustomerRepository;
import com.example.GreenFood.order.OrderRepository;
import com.example.GreenFood.user.CustomerAddressRepository;
import com.example.GreenFood.user.UserRepository;
import com.example.GreenFood.user.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/customers")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerAddressRepository customerAddressRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    @GetMapping
    public ResponseEntity<?> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Customer> customers = customerRepository.findAll(pageable);
        
        return ResponseEntity.ok(Map.of(
                "content", customers.getContent(),
                "totalPages", customers.getTotalPages(),
                "totalElements", customers.getTotalElements(),
                "currentPage", customers.getNumber()
        ));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateCustomerStatus(
            @PathVariable int id, 
            @RequestBody Map<String, String> body) {
        
        String status = body.get("status");
        if (status == null || (!status.equalsIgnoreCase("ACTIVE") && !status.equalsIgnoreCase("BLOCKED"))) {
            return ResponseEntity.badRequest().body("Trạng thái không hợp lệ. Dùng ACTIVE hoặc BLOCKED");
        }

        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setStatus(status.toUpperCase());
            userRepository.save(user);
            return ResponseEntity.ok("Customer status updated successfully");
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<?> addCustomer(@RequestBody Map<String, String> body) {
        try {
            // Dùng AuthService để tận dụng logic validate và mã hóa mật khẩu
            Map<String, Object> result = authService.register(
                    body.get("account"),
                    body.get("pwd"),
                    body.get("pwd"), // Tự động xác nhận mật khẩu giống nhau
                    body.get("name"),
                    body.get("phone")
            );
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable int id) {
        try {
            Optional<Customer> customerOpt = customerRepository.findById(id);
            if (customerOpt.isPresent()) {
                Customer customer = customerOpt.get();
                // Delete associated orders
                orderRepository.deleteAll(orderRepository.findByCustomerOrderByOrderDateDesc(customer));
                // Delete associated addresses
                customerAddressRepository.deleteAll(customerAddressRepository.findByCustomer(customer));
                
                // Delete the user/customer
                userRepository.deleteById(id);
                return ResponseEntity.ok(Map.of("message", "Đã xóa người dùng và dữ liệu liên quan thành công"));
            } else {
                return ResponseEntity.status(404).body(Map.of("error", "Không tìm thấy người dùng"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Không thể xóa người dùng: " + e.getMessage()));
        }
    }
}
