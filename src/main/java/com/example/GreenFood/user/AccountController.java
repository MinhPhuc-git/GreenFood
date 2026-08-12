package com.example.GreenFood.user;

import com.example.GreenFood.user.AddressDTO;
import com.example.GreenFood.user.UpdateProfileDTO;
import com.example.GreenFood.model.CustomerAddress;
import com.example.GreenFood.user.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    // Update profile (name, phone)
    @PutMapping("/{customerId}/profile")
    public ResponseEntity<?> updateProfile(@PathVariable int customerId,
                                           @RequestBody UpdateProfileDTO dto) {
        try {
            accountService.updateProfile(customerId, dto);
            return ResponseEntity.ok(Map.of("message", "Cập nhật hồ sơ thành công"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Add new address
    @PostMapping("/{customerId}/addresses")
    public ResponseEntity<?> addAddress(@PathVariable int customerId,
                                        @RequestBody AddressDTO dto) {
        try {
            CustomerAddress ca = accountService.addAddress(customerId, dto);
            return ResponseEntity.ok(Map.of("message", "Địa chỉ đã được thêm", "address", ca));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Get all addresses for a customer
    @GetMapping("/{customerId}/addresses")
    public ResponseEntity<?> getAddresses(@PathVariable int customerId) {
        try {
            List<CustomerAddress> list = accountService.getAddresses(customerId);
            return ResponseEntity.ok(Map.of("addresses", list));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
