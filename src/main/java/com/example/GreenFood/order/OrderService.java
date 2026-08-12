package com.example.GreenFood.order;

import com.example.GreenFood.model.*;
import com.example.GreenFood.user.CustomerRepository;
import com.example.GreenFood.loyalty.VoucherRepository;
import com.example.GreenFood.loyalty.RewardPointRepository;
import com.example.GreenFood.product.ProductRepository;
import com.example.GreenFood.loyalty.RewardPointService;
import com.example.GreenFood.loyalty.LoyaltyPointService;
import com.example.GreenFood.user.EmailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private static final BigDecimal DELIVERY_FEE = new BigDecimal("30000");
    private static final int VND_PER_POINT  = 100;

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final VoucherRepository voucherRepository;
    private final RewardPointRepository rewardPointRepository;
    private final ProductRepository productRepository;
    private final RewardPointService rewardPointService;
    private final LoyaltyPointService loyaltyPointService;
    private final EmailService emailService;

    public OrderService(CartRepository cartRepository, CartItemRepository cartItemRepository,
                        OrderRepository orderRepository, CustomerRepository customerRepository,
                        VoucherRepository voucherRepository, RewardPointRepository rewardPointRepository,
                        ProductRepository productRepository, RewardPointService rewardPointService,
                        LoyaltyPointService loyaltyPointService, EmailService emailService) {
        this.cartRepository        = cartRepository;
        this.cartItemRepository    = cartItemRepository;
        this.orderRepository       = orderRepository;
        this.customerRepository    = customerRepository;
        this.voucherRepository     = voucherRepository;
        this.rewardPointRepository = rewardPointRepository;
        this.productRepository     = productRepository;
        this.rewardPointService = rewardPointService;
        this.loyaltyPointService = loyaltyPointService;
        this.emailService = emailService;
    }

    // ═══════════════════════════════════════════
    // TÍNH TỔNG TIỀN (preview trước khi đặt)
    // ═══════════════════════════════════════════
    public OrderSummary calculateOrder(int customerId, String voucherCode, boolean usePoints) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        Cart cart = cartRepository.findByCustomer(customer)
                .orElseThrow(() -> new RuntimeException("Giỏ hàng trống"));

        List<CartItem> items = cart.getListCartItems();
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("Giỏ hàng không có sản phẩm");
        }

        // 1. Tính tiền hàng
        BigDecimal subtotal = items.stream()
                .map(item -> item.getProduct().getPrice().multiply(item.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Tính giảm giá voucher
        BigDecimal voucherDiscount = BigDecimal.ZERO;
        Voucher voucher = null;
        if (voucherCode != null && !voucherCode.isBlank()) {
            voucher = voucherRepository.findByVoucherCodeIgnoreCase(voucherCode)
                    .orElseThrow(() -> new RuntimeException("Mã voucher không hợp lệ"));

            if (!Boolean.TRUE.equals(voucher.getIsActive())) {
                throw new RuntimeException("Voucher đã hết hạn hoặc không còn hiệu lực");
            }
            if (voucher.getExpiryDate() != null && voucher.getExpiryDate().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Voucher đã hết hạn");
            }
            if (subtotal.compareTo(voucher.getMinOrderValue()) < 0) {
                throw new RuntimeException("Đơn hàng tối thiểu " + voucher.getMinOrderValue() + "đ để dùng voucher này");
            }

            if ("PERCENT".equalsIgnoreCase(voucher.getDiscountType())) {
                voucherDiscount = subtotal.multiply(voucher.getDiscountValue())
                        .divide(new BigDecimal("100"));
            } else {
                voucherDiscount = voucher.getDiscountValue();
            }
        }

        // 3. Tính giảm giá điểm thưởng
        BigDecimal pointsDiscount = BigDecimal.ZERO;
        int pointsUsed = 0;
        if (usePoints) {
            int availablePoints = rewardPointRepository.sumValidPointsByCustomer(customer);
            pointsDiscount = new BigDecimal(availablePoints * VND_PER_POINT);
            pointsUsed = availablePoints;
        }

        // 4. Tổng cuối
        BigDecimal totalDiscount = voucherDiscount.add(pointsDiscount);
        BigDecimal totalAmount   = subtotal.add(DELIVERY_FEE).subtract(totalDiscount);
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) totalAmount = BigDecimal.ZERO;

        // 5. Điểm thưởng sẽ nhận được (1 điểm / 1.000đ)
        int pointsEarned = LoyaltyPointService.calculateEarnedPoints(totalAmount);

        return new OrderSummary(subtotal, DELIVERY_FEE, voucherDiscount, pointsDiscount,
                totalDiscount, totalAmount, pointsEarned, pointsUsed, voucher);
    }

    // ═══════════════════════════════════════════
    // ĐẶT HÀNG (có Pessimistic Lock)
    // ═══════════════════════════════════════════
    @Transactional
    public Orders placeOrder(int customerId, String deliveryAddress,
                             String voucherCode, boolean usePoints) {
        return checkoutOrder(customerId, deliveryAddress, voucherCode, usePoints, "CASH");
    }

    @Transactional
    public Orders checkoutOrder(int customerId, String deliveryAddress,
                                String voucherCode, boolean usePoints, String paymentMethod) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        Cart cart = cartRepository.findByCustomer(customer)
                .orElseThrow(() -> new RuntimeException("Giỏ hàng trống"));

        List<CartItem> cartItems = cart.getListCartItems();
        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng không có sản phẩm");
        }

        if (deliveryAddress == null || deliveryAddress.isBlank()) {
            throw new RuntimeException("Vui lòng nhập địa chỉ giao hàng");
        }

        OrderSummary summary = calculateOrder(customerId, voucherCode, usePoints);

        // Tạo Order
        Orders order = new Orders();
        order.setCustomer(customer);
        order.setDeliveryAddress(deliveryAddress);
        order.setDeliveryFee(summary.deliveryFee());
        order.setDiscountAmount(summary.totalDiscount());
        order.setTotalAmount(summary.totalAmount());
        order.setOrderDate(LocalDateTime.now());
        order.setPaymentMethod(paymentMethod != null ? paymentMethod.toUpperCase() : "CASH");
        // CASH -> PENDING (chờ thu tiền), BANK_TRANSFER -> SUCCESS (đã thanh toán)
        order.setStatus("BANK_TRANSFER".equalsIgnoreCase(paymentMethod) ? "SUCCESS" : "PENDING");
        order.setVoucher(summary.voucher());

        Orders savedOrder = orderRepository.save(order);

        // Tạo OrderItems + trừ kho (có LOCK)
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {

            // ★ PESSIMISTIC LOCK: khóa dòng product cho đến khi transaction xong
            // Nếu 2 người mua cùng lúc, người thứ 2 sẽ chờ ở đây
            Product product = productRepository.findByIdWithLock(cartItem.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

            // Kiểm tra tồn kho SAU KHI đã lock (đảm bảo số liệu mới nhất)
            if (product.getStock().compareTo(cartItem.getQuantity()) < 0) {
                throw new RuntimeException("Sản phẩm \"" + product.getName()
                        + "\" chỉ còn " + product.getStock() + " " + product.getUnit()
                        + " — vui lòng cập nhật lại giỏ hàng");
            }

            // Trừ tồn kho
            product.setStock(product.getStock().subtract(cartItem.getQuantity()));
            productRepository.save(product);

            // Tạo OrderItem
            OrderItem orderItem = new OrderItem();
            orderItem.setOrders(savedOrder);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(product.getPrice());
            orderItems.add(orderItem);
        }
        savedOrder.setListOrderItems(orderItems);
        orderRepository.save(savedOrder);

        // Chuyển khoản / thanh toán ngay -> cộng điểm tích lũy
        if (LoyaltyPointService.isEligiblePaidStatus(savedOrder.getStatus())) {
            loyaltyPointService.tryAwardPointsForOrder(savedOrder.getId());
            rewardPointService.updateMembershipTier(savedOrder);
        }

        // Trừ điểm nếu dùng
        if (usePoints && summary.pointsUsed() > 0) {
            RewardPoint usedPoint = new RewardPoint();
            usedPoint.setCustomer(customer);
            usedPoint.setOrders(savedOrder);
            usedPoint.setPoints(-summary.pointsUsed());
            usedPoint.setExpiryDate(LocalDateTime.now());
            rewardPointRepository.save(usedPoint);
        }

        // Xóa giỏ hàng sau khi đặt (đảm bảo đồng bộ với reset giỏ hàng ở Front-end)
        cartItemRepository.deleteAll(cartItems);
        cart.getListCartItems().clear();
        cartRepository.save(cart);

        return savedOrder;
    }

    // ═══════════════════════════════════════════
    // RECORD tóm tắt đơn hàng
    // ═══════════════════════════════════════════
    public record OrderSummary(
            BigDecimal subtotal,
            BigDecimal deliveryFee,
            BigDecimal voucherDiscount,
            BigDecimal pointsDiscount,
            BigDecimal totalDiscount,
            BigDecimal totalAmount,
            int pointsEarned,
            int pointsUsed,
            Voucher voucher
    ) {}

    // ═══════════════════════════════════════════
    // CẬP NHẬT TRẠNG THÁI ĐƠN HÀNG
    // ═══════════════════════════════════════════
    @Transactional
    public Orders updateOrderStatus(int orderId, String newStatus) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        
        String oldStatus = order.getStatus();
        String newStatusUpper = newStatus.toUpperCase();
        order.setStatus(newStatusUpper);
        Orders savedOrder = orderRepository.save(order);

        if (LoyaltyPointService.isEligiblePaidStatus(newStatusUpper)) {
            loyaltyPointService.tryAwardPointsForOrder(savedOrder.getId());
        }

        if (!"COMPLETED".equalsIgnoreCase(oldStatus) && "COMPLETED".equalsIgnoreCase(newStatusUpper)) {
            rewardPointService.updateMembershipTier(savedOrder);
            emailService.sendOrderInvoice(savedOrder);
        }

        return savedOrder;
    }

    // ═══════════════════════════════════════════
    // XÁC NHẬN THU TIỀN MẶT (Admin)
    // ═══════════════════════════════════════════
    @Transactional
    public Orders confirmCashPayment(int orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng #" + orderId));

        if (!"CASH".equalsIgnoreCase(order.getPaymentMethod())) {
            throw new RuntimeException("Đơn hàng này không phải thanh toán tiền mặt");
        }
        if ("SUCCESS".equalsIgnoreCase(order.getStatus())) {
            throw new RuntimeException("Đơn hàng đã được xác nhận thanh toán trước đó");
        }

        order.setStatus("SUCCESS");
        Orders saved = orderRepository.save(order);
        loyaltyPointService.tryAwardPointsForOrder(saved.getId());
        rewardPointService.updateMembershipTier(saved);
        emailService.sendOrderInvoice(saved);
        return saved;
    }

    // ═══════════════════════════════════════════
    // HỦY ĐƠN HÀNG LÀM BỞI USER
    // ═══════════════════════════════════════════
    @Transactional
    public Orders cancelOrder(int orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (!"PENDING".equalsIgnoreCase(order.getStatus()) && !"CONFIRMED".equalsIgnoreCase(order.getStatus())) {
            throw new RuntimeException("Chỉ có thể hủy đơn hàng ở trạng thái Chờ thanh toán hoặc Chờ xác nhận");
        }

        order.setStatus("CANCELLED");
        
        // Rollback inventory
        for (OrderItem item : order.getListOrderItems()) {
            Product product = productRepository.findByIdWithLock(item.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
            product.setStock(product.getStock().add(item.getQuantity()));
            productRepository.save(product);
        }

        return orderRepository.save(order);
    }

    // ═══════════════════════════════════════════
    // HỦY ĐƠN HÀNG LÀM BỞI ADMIN
    // ═══════════════════════════════════════════
    @Transactional
    public Orders adminCancelOrder(int orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if ("CANCELLED".equalsIgnoreCase(order.getStatus())) {
            throw new RuntimeException("Đơn hàng đã bị hủy trước đó");
        }

        order.setStatus("CANCELLED");
        
        // Rollback inventory
        for (OrderItem item : order.getListOrderItems()) {
            Product product = productRepository.findByIdWithLock(item.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
            product.setStock(product.getStock().add(item.getQuantity()));
            productRepository.save(product);
        }

        return orderRepository.save(order);
    }
}