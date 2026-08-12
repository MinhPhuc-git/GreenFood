package com.example.GreenFood.order;

import com.example.GreenFood.model.Cart;
import com.example.GreenFood.order.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // GET /api/cart/{customerId} - Lấy giỏ hàng
    @GetMapping("/{customerId}")
    public ResponseEntity<?> getCart(@PathVariable int customerId) {
        try {
            Cart cart = cartService.getOrCreateCart(customerId);
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // POST /api/cart/{customerId}/add - Thêm sản phẩm vào giỏ
    // Body: { "productId": 1, "quantity": 2 }
    @PostMapping("/{customerId}/add")
    public ResponseEntity<?> addItem(@PathVariable int customerId,
                                     @RequestBody Map<String, Integer> body) {
        try {
            Integer productId = body.get("productId");
            Integer quantity = body.get("quantity");

            if (productId == null || quantity == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Thiếu productId hoặc quantity"));
            }

            Cart cart = cartService.addItem(customerId, productId, quantity);
            return ResponseEntity.ok(Map.of(
                    "message", "Thêm vào giỏ hàng thành công",
                    "cart", cart
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // DELETE /api/cart/{customerId}/remove/{cartItemId} - Xóa sản phẩm khỏi giỏ
    @DeleteMapping("/{customerId}/remove/{cartItemId}")
    public ResponseEntity<?> removeItem(@PathVariable int customerId,
                                        @PathVariable int cartItemId) {
        try {
            Cart cart = cartService.removeItem(customerId, cartItemId);
            return ResponseEntity.ok(Map.of(
                    "message", "Đã xóa sản phẩm khỏi giỏ hàng",
                    "cart", cart
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // PUT /api/cart/{customerId}/update/{cartItemId} - Cập nhật số lượng
    // Body: { "quantity": 3 }
    @PutMapping("/{customerId}/update/{cartItemId}")
    public ResponseEntity<?> updateQuantity(@PathVariable int customerId,
                                            @PathVariable int cartItemId,
                                            @RequestBody Map<String, Integer> body) {
        try {
            Integer quantity = body.get("quantity");

            if (quantity == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Thiếu quantity"));
            }

            Cart cart = cartService.updateQuantity(customerId, cartItemId, quantity);
            return ResponseEntity.ok(Map.of(
                    "message", "Cập nhật số lượng thành công",
                    "cart", cart
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // DELETE /api/cart/{customerId}/clear - Xóa toàn bộ sản phẩm khỏi giỏ hàng
    @DeleteMapping("/{customerId}/clear")
    public ResponseEntity<?> clearCart(@PathVariable int customerId) {
        try {
            Cart cart = cartService.clearCart(customerId);
            return ResponseEntity.ok(Map.of(
                    "message", "Đã xóa toàn bộ giỏ hàng",
                    "cart", cart
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}