package com.example.GreenFood.order;

import com.example.GreenFood.order.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final VNPayService vnPayService;

    public PaymentController(VNPayService vnPayService) {
        this.vnPayService = vnPayService;
    }

    // POST /api/payment/create/{orderId}
    // Tạo URL thanh toán VNPAY → trả về link để frontend redirect
    @PostMapping("/create/{orderId}")
    public ResponseEntity<?> createPayment(@PathVariable int orderId,
                                           HttpServletRequest request) {
        try {
            String paymentUrl = vnPayService.createPaymentUrl(orderId, request);
            return ResponseEntity.ok(Map.of(
                    "paymentUrl", paymentUrl,
                    "message", "Chuyển hướng đến VNPAY để thanh toán"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // GET /api/payment/vnpay-return
    // VNPAY sẽ redirect về đây sau khi khách hàng thanh toán
    @GetMapping("/vnpay-return")
    public ResponseEntity<?> vnpayReturn(@RequestParam Map<String, String> params) {
        try {
            Map<String, String> result = vnPayService.processReturn(params);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}