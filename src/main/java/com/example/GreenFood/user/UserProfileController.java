package com.example.GreenFood.user;

import com.example.GreenFood.model.Admin;
import com.example.GreenFood.model.Customer;
import com.example.GreenFood.model.User;
import com.example.GreenFood.admin.AdminRepository;
import com.example.GreenFood.user.CustomerRepository;
import com.example.GreenFood.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Unified profile controller for both Admin and Customer.
 * Cho phép admin xem/chỉnh sửa thông tin hồ sơ như người dùng bình thường.
 */
@RestController
@RequestMapping("/api/user/profile")
public class UserProfileController {

    @Autowired private UserRepository userRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private AdminRepository adminRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * GET /api/user/profile/{id}
     * Lấy thông tin hồ sơ theo id (cả admin lẫn customer).
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getProfile(@PathVariable int id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Không tìm thấy người dùng"));
        }
        User user = userOpt.get();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", user.getId());
        data.put("account", user.getAccount());
        data.put("status", user.getStatus());
        data.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : "");

        // Nếu là Admin
        boolean isAdmin = adminRepository.existsById(id);
        data.put("role", isAdmin ? "ADMIN" : "CUSTOMER");

        // Thông tin name, phone từ Customer nếu có
        Optional<Customer> customerOpt = customerRepository.findById(id);
        if (customerOpt.isPresent()) {
            data.put("name", customerOpt.get().getName() != null ? customerOpt.get().getName() : (isAdmin ? "Admin" : ""));
            data.put("phone", customerOpt.get().getPhone() != null ? customerOpt.get().getPhone() : "");
        } else {
            // Admin chưa có customer record → trả mặc định
            data.put("name", isAdmin ? "Admin" : "");
            data.put("phone", "");
        }

        return ResponseEntity.ok(data);
    }

    /**
     * PUT /api/user/profile/{id}
     * Cập nhật tên, số điện thoại (và tuỳ chọn mật khẩu).
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProfile(@PathVariable int id,
                                           @RequestBody Map<String, String> body) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Không tìm thấy người dùng"));
        }
        User user = userOpt.get();

        // Đổi mật khẩu nếu được cung cấp
        String newPwd = body.get("pwd");
        if (newPwd != null && !newPwd.isBlank()) {
            user.setPwd(passwordEncoder.encode(newPwd));
            userRepository.save(user);
        }

        // Cập nhật name & phone qua Customer record
        Optional<Customer> customerOpt = customerRepository.findById(id);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            if (body.containsKey("name") && !body.get("name").isBlank()) {
                customer.setName(body.get("name"));
            }
            if (body.containsKey("phone")) {
                customer.setPhone(body.get("phone"));
            }
            customerRepository.save(customer);
        } else if (adminRepository.existsById(id)) {
            // Admin chưa có customer record → tạo mới để lưu name/phone
            String name = body.getOrDefault("name", "Admin");
            String phone = body.getOrDefault("phone", "");
            customerRepository.insertCustomer(id, name, phone);
        }

        return ResponseEntity.ok(Map.of("message", "Cập nhật hồ sơ thành công"));
    }
}
