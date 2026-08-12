package com.example.GreenFood.order;

import com.example.GreenFood.model.Orders;
import com.example.GreenFood.loyalty.LoyaltyPointService;
import com.example.GreenFood.order.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;
    private final LoyaltyPointService loyaltyPointService;

    public OrderController(OrderService orderService, LoyaltyPointService loyaltyPointService) {
        this.orderService = orderService;
        this.loyaltyPointService = loyaltyPointService;
    }

    // GET /api/order/preview/{customerId}
    // Preview tổng tiền trước khi đặt
    // Params: voucherCode (optional), usePoints (optional, default false)
    @GetMapping("/preview/{customerId}")
    public ResponseEntity<?> previewOrder(
            @PathVariable int customerId,
            @RequestParam(required = false) String voucherCode,
            @RequestParam(defaultValue = "false") boolean usePoints) {
        try {
            OrderService.OrderSummary summary = orderService.calculateOrder(customerId, voucherCode, usePoints);
            return ResponseEntity.ok(Map.of(
                    "subtotal",        summary.subtotal(),
                    "deliveryFee",     summary.deliveryFee(),
                    "voucherDiscount", summary.voucherDiscount(),
                    "pointsDiscount",  summary.pointsDiscount(),
                    "totalDiscount",   summary.totalDiscount(),
                    "totalAmount",     summary.totalAmount(),
                    "pointsEarned",    summary.pointsEarned(),
                    "pointsUsed",      summary.pointsUsed()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // POST /api/order/place/{customerId}
    // Đặt hàng
    // Body:
    // {
    //   "deliveryAddress": "123 Nguyễn Huệ, Q1, TP.HCM",
    //   "voucherCode": "GREEN10",   (optional)
    //   "usePoints": true            (optional)
    // }
    @PostMapping("/place/{customerId}")
    public ResponseEntity<?> placeOrder(
            @PathVariable int customerId,
            @RequestBody Map<String, Object> body) {
        try {
            String deliveryAddress = (String) body.get("deliveryAddress");
            String voucherCode     = (String) body.getOrDefault("voucherCode", null);
            boolean usePoints      = Boolean.parseBoolean(body.getOrDefault("usePoints", "false").toString());

            Orders order = orderService.placeOrder(customerId, deliveryAddress, voucherCode, usePoints);

            return ResponseEntity.ok(Map.of(
                    "message",   "Đặt hàng thành công!",
                    "orderId",   order.getId(),
                    "status",    order.getStatus(),
                    "total",     order.getTotalAmount(),
                    "orderDate", order.getOrderDate().toString()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // PUT /api/admin/orders/{orderId}/status
    // Body: { "status": "COMPLETED" }
    @PutMapping("/admin/orders/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable int orderId,
            @RequestBody Map<String, String> body) {
        try {
            String newStatus = body.get("status");
            if (newStatus == null || newStatus.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Phải có trường status"));
            }
            
            Orders order = orderService.updateOrderStatus(orderId, newStatus);
            return ResponseEntity.ok(Map.of(
                    "message", "Order status updated successfully",
                    "orderId", order.getId(),
                    "status", order.getStatus()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ═══════════════════════════════════════════
    // CHECKOUT (Thanh toán đơn hàng)
    // ═══════════════════════════════════════════
    // POST /api/orders/checkout
    // Body:
    // {
    //   "customerId": 1,
    //   "deliveryAddress": "123 Nguyễn Huệ, Q1, TP.HCM",
    //   "paymentMethod": "CASH" | "BANK_TRANSFER",
    //   "voucherCode": "GREEN10",   (optional)
    //   "usePoints": true           (optional)
    // }
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody Map<String, Object> body) {
        try {
            int customerId = Integer.parseInt(body.get("customerId").toString());
            String deliveryAddress = (String) body.get("deliveryAddress");
            String paymentMethod   = (String) body.getOrDefault("paymentMethod", "CASH");
            String voucherCode     = (String) body.getOrDefault("voucherCode", null);
            boolean usePoints      = Boolean.parseBoolean(body.getOrDefault("usePoints", "false").toString());

            Orders order = orderService.checkoutOrder(customerId, deliveryAddress, voucherCode, usePoints, paymentMethod);

            int totalPoints = loyaltyPointService.getCustomerPoints(customerId).getLoyaltyPoints();
            int pointsEarned = 0;
            String loyaltyMessage = "";
            if (order.isRewardProcessed() && LoyaltyPointService.isEligiblePaidStatus(order.getStatus())) {
                pointsEarned = LoyaltyPointService.calculateEarnedPoints(order.getTotalAmount());
                if (pointsEarned > 0) {
                    loyaltyMessage = "Bạn đã nhận được +" + pointsEarned
                            + " điểm thưởng. Tổng điểm hiện tại: " + totalPoints + " điểm.";
                }
            }

            return ResponseEntity.ok(Map.of(
                    "message",        "BANK_TRANSFER".equalsIgnoreCase(paymentMethod)
                                        ? "Chuyển khoản và đặt hàng thành công!"
                                        : "Đặt hàng thành công! Đơn hàng của bạn đang được xử lý.",
                    "orderId",        order.getId(),
                    "status",         order.getStatus(),
                    "paymentMethod",  order.getPaymentMethod(),
                    "total",          order.getTotalAmount(),
                    "orderDate",      order.getOrderDate().toString(),
                    "pointsEarned",   pointsEarned,
                    "totalLoyaltyPoints", totalPoints,
                    "loyaltyMessage", loyaltyMessage
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // PUT /api/orders/{id}/confirm-cash
    // Admin xác nhận đã thu tiền mặt -> PENDING => SUCCESS
    @PutMapping("/{id}/confirm-cash")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> confirmCashPayment(@PathVariable int id) {
        try {
            Orders order = orderService.confirmCashPayment(id);
            int customerId = order.getCustomer().getId();
            int totalPoints = loyaltyPointService.getCustomerPoints(customerId).getLoyaltyPoints();
            int pointsEarned = order.isRewardProcessed()
                    ? LoyaltyPointService.calculateEarnedPoints(order.getTotalAmount()) : 0;
            String loyaltyMessage = pointsEarned > 0
                    ? "Bạn đã nhận được +" + pointsEarned + " điểm thưởng. Tổng điểm hiện tại: "
                    + totalPoints + " điểm." : "";
            return ResponseEntity.ok(Map.of(
                    "message", "Xác nhận thu tiền mặt thành công!",
                    "orderId", order.getId(),
                    "status",  order.getStatus(),
                    "pointsEarned", pointsEarned,
                    "totalLoyaltyPoints", totalPoints,
                    "loyaltyMessage", loyaltyMessage
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // PUT /api/order/{id}/cancel
    // Hủy đơn hàng bởi người dùng
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable int id) {
        try {
            Orders order = orderService.cancelOrder(id);
            return ResponseEntity.ok(Map.of(
                    "message", "Hủy đơn hàng thành công!",
                    "orderId", order.getId(),
                    "status",  order.getStatus()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // PUT /api/order/admin/{id}/cancel
    // Hủy đơn hàng bởi Admin
    @PutMapping("/admin/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> adminCancelOrder(@PathVariable int id) {
        try {
            Orders order = orderService.adminCancelOrder(id);
            return ResponseEntity.ok(Map.of(
                    "message", "Hủy đơn hàng thành công!",
                    "orderId", order.getId(),
                    "status",  order.getStatus()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}