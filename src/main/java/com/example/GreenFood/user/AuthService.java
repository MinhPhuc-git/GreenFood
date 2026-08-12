package com.example.GreenFood.user;

import com.example.GreenFood.model.Customer;
import com.example.GreenFood.model.User;
import com.example.GreenFood.admin.AdminRepository;
import com.example.GreenFood.user.CustomerRepository;
import com.example.GreenFood.user.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final AdminRepository adminRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, CustomerRepository customerRepository, AdminRepository adminRepository) {
        this.userRepository    = userRepository;
        this.customerRepository = customerRepository;
        this.adminRepository = adminRepository;
        this.passwordEncoder   = new BCryptPasswordEncoder();
    }

    // ═══════════════════════════════════════════
    // ĐĂNG NHẬP
    // ═══════════════════════════════════════════
    public Map<String, Object> login(String account, String pwd) {
        if (account == null || account.isBlank()) {
            throw new RuntimeException("Vui lòng nhập tài khoản");
        }
        if (pwd == null || pwd.isBlank()) {
            throw new RuntimeException("Vui lòng nhập mật khẩu");
        }

        User user = userRepository.findByAccount(account)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));

        // Cho phép mọi status trừ 'locked'
        if ("locked".equalsIgnoreCase(user.getStatus())) {
            throw new RuntimeException("Tài khoản đã bị khóa");
        }

        // Verify password using BCryptPasswordEncoder (handles both hashed and plain passwords)
        if (!passwordEncoder.matches(pwd, user.getPwd())) {
            // If password is not a BCrypt hash, fall back to plain comparison
            if (!pwd.equals(user.getPwd())) {
                throw new RuntimeException("Mật khẩu không đúng");
            }
        }

        // Xác định role
        String role = adminRepository.existsById(user.getId()) ? "ADMIN" : "CUSTOMER";

        // Lấy thêm thông tin
        String name = account;
        Customer customer = customerRepository.findById(user.getId()).orElse(null);
        if (customer != null && customer.getName() != null) {
            name = customer.getName();
        }

        return Map.of(
                "userId",   user.getId(),
                "account",  user.getAccount(),
                "name",     name,
                "role",     role,
                "message",  "Đăng nhập thành công"
        );
    }

    // ═══════════════════════════════════════════
    // ĐĂNG KÝ
    // ═══════════════════════════════════════════
    @Transactional
    public Map<String, Object> register(String account, String pwd, String confirmPwd,
                                        String name, String phone) {
        // Validate
        if (account == null || account.isBlank()) {
            throw new RuntimeException("Vui lòng nhập tài khoản (email)");
        }
        if (!account.contains("@")) {
            throw new RuntimeException("Tài khoản phải là địa chỉ email hợp lệ");
        }
        if (pwd == null || pwd.length() < 6) {
            throw new RuntimeException("Mật khẩu phải có ít nhất 6 ký tự");
        }
        if (!pwd.equals(confirmPwd)) {
            throw new RuntimeException("Mật khẩu xác nhận không khớp");
        }
        if (name == null || name.isBlank()) {
            throw new RuntimeException("Vui lòng nhập họ tên");
        }
        if (phone == null || phone.isBlank()) {
            throw new RuntimeException("Vui lòng nhập số điện thoại");
        }
        if (userRepository.existsByAccount(account)) {
            throw new RuntimeException("Tài khoản đã tồn tại");
        }

        // Tạo Customer mới (Customer extends User)
        Customer customer = new Customer();
        customer.setAccount(account);
        customer.setPwd(passwordEncoder.encode(pwd)); // mã hóa mật khẩu
        customer.setStatus("Active");
        customer.setCreatedAt(LocalDateTime.now());
        customer.setName(name);
        customer.setPhone(phone);

        customerRepository.save(customer);

        return Map.of(
                "userId",  customer.getId(),
                "account", customer.getAccount(),
                "name",    customer.getName(),
                "role",    "CUSTOMER",
                "message", "Đăng ký thành công! Chào mừng bạn đến với GreenFood 🌿"
        );
    }
}