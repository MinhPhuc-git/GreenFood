package com.example.GreenFood.loyalty;

import com.example.GreenFood.loyalty.CustomerPointsResponse;
import com.example.GreenFood.loyalty.LoyaltyPointService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * API tích điểm khách hàng.
 *
 * Mẫu:
 * GET /api/customer/12/points
 * => { "customerId": 12, "customerName": "abc", "loyaltyPoints": 45 }
 */
@RestController
@RequestMapping("/api/customer")
public class CustomerLoyaltyController {

    private final LoyaltyPointService loyaltyPointService;

    public CustomerLoyaltyController(LoyaltyPointService loyaltyPointService) {
        this.loyaltyPointService = loyaltyPointService;
    }

    @GetMapping("/{id}/points")
    public ResponseEntity<?> getCustomerPoints(@PathVariable("id") int customerId) {
        try {
            CustomerPointsResponse response = loyaltyPointService.getCustomerPoints(customerId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
