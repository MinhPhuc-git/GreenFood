package com.example.GreenFood.order;

import com.example.GreenFood.model.Customer;
import com.example.GreenFood.model.Orders;
import com.example.GreenFood.user.CustomerRepository;
import com.example.GreenFood.order.OrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/order")
public class OrderHistoryController {

        private final OrderRepository orderRepository;
        private final CustomerRepository customerRepository;

        public OrderHistoryController(OrderRepository orderRepository,
                        CustomerRepository customerRepository) {
                this.orderRepository = orderRepository;
                this.customerRepository = customerRepository;
        }

        // GET /api/order/history/{customerId}
        // Lấy tất cả đơn hàng của customer
        // Param: status (optional) — PENDING, CONFIRMED, DELIVERING, SUCCESS, CANCELLED
        @GetMapping("/history/{customerId}")
        public ResponseEntity<?> getOrderHistory(
                        @PathVariable int customerId,
                        @RequestParam(required = false) String status) {
                try {
                        Customer customer = customerRepository.findById(customerId)
                                        .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

                        List<Orders> orders = (status != null && !status.isBlank())
                                        ? orderRepository.findByCustomerAndStatusOrderByOrderDateDesc(customer,
                                                        status.toUpperCase())
                                        : orderRepository.findByCustomerOrderByOrderDateDesc(customer);

                        // Map sang dạng gọn cho frontend
                        List<Map<String, Object>> result = orders.stream().map(o -> {
                                List<Map<String, Object>> items = o.getListOrderItems() == null ? List.of()
                                                : o.getListOrderItems().stream().map(item -> Map.<String, Object>of(
                                                                "productId", item.getProduct() != null ? item.getProduct().getId() : 0,
                                                                "productName", item.getProduct() != null ? item.getProduct().getName() : "Sản phẩm đã bị xóa",
                                                                "image", item.getProduct() != null ? item.getProduct().getImage() : "",
                                                                "quantity", item.getQuantity(),
                                                                "unitPrice", item.getUnitPrice()))
                                                                .collect(Collectors.toList());

                                return Map.<String, Object>of(
                                                "orderId", o.getId(),
                                                "orderDate",
                                                o.getOrderDate() != null ? o.getOrderDate().toString() : "",
                                                "status", o.getStatus(),
                                                "totalAmount", o.getTotalAmount(),
                                                "deliveryFee", o.getDeliveryFee(),
                                                "discountAmount", o.getDiscountAmount(),
                                                "deliveryAddress",
                                                o.getDeliveryAddress() != null ? o.getDeliveryAddress() : "",
                                                "items", items);
                        }).collect(Collectors.toList());

                        return ResponseEntity.ok(result);
                } catch (RuntimeException e) {
                        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
                }
        }

        // GET /api/order/{orderId}/detail
        // Lấy chi tiết 1 đơn hàng
        @GetMapping("/{orderId}/detail")
        public ResponseEntity<?> getOrderDetail(@PathVariable int orderId) {
                try {
                        Orders o = orderRepository.findById(orderId)
                                        .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

                        List<Map<String, Object>> items = o.getListOrderItems() == null ? List.of()
                                        : o.getListOrderItems().stream().map(item -> Map.<String, Object>of(
                                                        "productId", item.getProduct() != null ? item.getProduct().getId() : 0,
                                                        "productName", item.getProduct() != null ? item.getProduct().getName() : "Sản phẩm đã bị xóa",
                                                        "unit", item.getProduct() != null ? item.getProduct().getUnit() : "—",
                                                        "image", item.getProduct() != null ? item.getProduct().getImage() : "",
                                                        "quantity", item.getQuantity(),
                                                        "unitPrice", item.getUnitPrice(),
                                                        "lineTotal", item.getUnitPrice().multiply(item.getQuantity())))
                                                        .collect(Collectors.toList());

                        Map<String, Object> result = Map.of(
                                        "orderId", o.getId(),
                                        "orderDate", o.getOrderDate() != null ? o.getOrderDate().toString() : "",
                                        "status", o.getStatus(),
                                        "totalAmount", o.getTotalAmount(),
                                        "deliveryFee", o.getDeliveryFee(),
                                        "discountAmount", o.getDiscountAmount(),
                                        "deliveryAddress", o.getDeliveryAddress() != null ? o.getDeliveryAddress() : "",
                                        "voucher", o.getVoucher() != null ? o.getVoucher().getVoucherCode() : "",
                                        "items", items);

                        return ResponseEntity.ok(result);
                } catch (RuntimeException e) {
                        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
                }
        }
}