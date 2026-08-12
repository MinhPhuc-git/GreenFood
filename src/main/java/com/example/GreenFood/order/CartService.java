package com.example.GreenFood.order;

import com.example.GreenFood.model.Cart;
import com.example.GreenFood.model.CartItem;
import com.example.GreenFood.model.Customer;
import com.example.GreenFood.model.Product;
import com.example.GreenFood.order.CartItemRepository;
import com.example.GreenFood.order.CartRepository;
import com.example.GreenFood.user.CustomerRepository;
import com.example.GreenFood.product.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository,
            CustomerRepository customerRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    // Lấy giỏ hàng theo customerId (tạo mới nếu chưa có)
    public Cart getOrCreateCart(int customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với id: " + customerId));

        return cartRepository.findByCustomer(customer)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setCustomer(customer);
                    return cartRepository.save(newCart);
                });
    }

    // Thêm sản phẩm vào giỏ hàng
    @Transactional
    public Cart addItem(int customerId, int productId, int quantity) {
        if (quantity <= 0) {
            throw new RuntimeException("Số lượng phải lớn hơn 0");
        }

        Cart cart = getOrCreateCart(customerId);

        Product product = productRepository.findByIdWithLock(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với id: " + productId));

        // Kiểm tra trạng thái sản phẩm - chấp nhận cà tiếng Việt lẫn tiếng Anh
        String status = product.getStatus() != null ? product.getStatus().trim().toLowerCase() : "";
        boolean isAvailable = status.equals("active")
                || status.equals("sẵn sàng")
                || status.equals("còn hàng")
                || status.equals("in-stock")
                || status.equals("low-stock")
                || status.equals("sắp hết");
        if (!isAvailable) {
            throw new RuntimeException("Sản phẩm đã ngừng kinh doanh hoặc hết hàng");
        }

        // Kiểm tra tồn kho
        BigDecimal requestedQty = BigDecimal.valueOf(quantity);
        if (product.getStock().compareTo(requestedQty) < 0) {
            throw new RuntimeException(
                    "Sản phẩm chỉ còn " + product.getStock().intValue() + " " + product.getUnit() + " trong kho");
        }

        // Nếu sản phẩm đã có trong giỏ thì cộng thêm số lượng
        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            BigDecimal newQty = item.getQuantity().add(requestedQty);

            // Kiểm tra lại tồn kho sau khi cộng
            if (product.getStock().compareTo(newQty) < 0) {
                throw new RuntimeException(
                        "Sản phẩm chỉ còn " + product.getStock().intValue() + " " + product.getUnit() + " trong kho");
            }

            item.setQuantity(newQty);
            cartItemRepository.save(item);
        } else {
            // Thêm mới CartItem
            CartItem newItem = new CartItem(requestedQty, product, cart);
            cartItemRepository.save(newItem);
        }

        return cartRepository.findById(cart.getId()).orElseThrow();
    }

    // Xóa sản phẩm khỏi giỏ hàng
    @Transactional
    public Cart removeItem(int customerId, int cartItemId) {
        Cart cart = getOrCreateCart(customerId);

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng"));

        // Kiểm tra item có thuộc giỏ của customer này không
        if (item.getCart().getId() != cart.getId()) {
            throw new RuntimeException("Bạn không có quyền xóa sản phẩm này");
        }

        cartItemRepository.delete(item);
        return cartRepository.findById(cart.getId()).orElseThrow();
    }

    // Cập nhật số lượng sản phẩm trong giỏ
    @Transactional
    public Cart updateQuantity(int customerId, int cartItemId, int quantity) {
        if (quantity <= 0) {
            throw new RuntimeException("Số lượng phải lớn hơn 0. Dùng API xóa nếu muốn bỏ sản phẩm.");
        }

        Cart cart = getOrCreateCart(customerId);

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng"));

        // Kiểm tra item có thuộc giỏ của customer này không
        if (item.getCart().getId() != cart.getId()) {
            throw new RuntimeException("Bạn không có quyền sửa sản phẩm này");
        }

        // Kiểm tra tồn kho
        BigDecimal requestedQty = BigDecimal.valueOf(quantity);
        Product product = item.getProduct();
        if (product.getStock().compareTo(requestedQty) < 0) {
            throw new RuntimeException(
                    "Sản phẩm chỉ còn " + product.getStock().intValue() + " " + product.getUnit() + " trong kho");
        }

        item.setQuantity(requestedQty);
        cartItemRepository.save(item);

        return cartRepository.findById(cart.getId()).orElseThrow();
    }

    // Xóa toàn bộ sản phẩm trong giỏ hàng (khi đăng xuất hoặc thanh toán xong)
    @Transactional
    public Cart clearCart(int customerId) {
        Cart cart = getOrCreateCart(customerId);
        cartItemRepository.deleteAll(cart.getListCartItems());
        cart.getListCartItems().clear();
        return cartRepository.save(cart);
    }
}