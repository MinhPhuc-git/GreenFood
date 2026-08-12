package com.example.GreenFood.loyalty;

import com.example.GreenFood.loyalty.CustomerPointsResponse;
import com.example.GreenFood.loyalty.LoyaltyAwardResponse;
import com.example.GreenFood.model.Customer;
import com.example.GreenFood.model.Orders;
import com.example.GreenFood.user.CustomerRepository;
import com.example.GreenFood.order.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.Set;

@Service
public class LoyaltyPointService {

    private static final BigDecimal VND_PER_POINT = new BigDecimal("1000");

    private static final Set<String> PAID_STATUSES = Set.of("PAID", "COMPLETED", "SUCCESS");

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;

    public LoyaltyPointService(OrderRepository orderRepository, CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
    }

    public static int calculateEarnedPoints(BigDecimal paymentAmount) {
        if (paymentAmount == null || paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        return paymentAmount.divide(VND_PER_POINT, 0, RoundingMode.DOWN).intValue();
    }

    public static boolean isEligiblePaidStatus(String status) {
        return status != null && PAID_STATUSES.contains(status.trim().toUpperCase());
    }

    public CustomerPointsResponse getCustomerPoints(int customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
        return new CustomerPointsResponse(
                customer.getId(),
                customer.getName(),
                customer.getLoyaltyPoints()
        );
    }

    /**
     * Cộng điểm khi đơn ở trạng thái thanh toán thành công (PAID / COMPLETED / SUCCESS).
     * Không cộng trùng nhờ cờ reward_processed.
     */
    @Transactional
    public Optional<LoyaltyAwardResponse> tryAwardPointsForOrder(int orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        Customer customer = order.getCustomer();
        if (customer == null) {
            throw new RuntimeException("Đơn hàng không gắn với khách hàng");
        }

        if (order.isRewardProcessed()) {
            return Optional.of(LoyaltyAwardResponse.skipped(
                    order.getId(), customer.getId(), customer.getLoyaltyPoints()));
        }

        if (!isEligiblePaidStatus(order.getStatus())) {
            return Optional.empty();
        }

        int earned = calculateEarnedPoints(order.getTotalAmount());
        if (earned <= 0) {
            order.setRewardProcessed(true);
            orderRepository.save(order);
            return Optional.of(LoyaltyAwardResponse.skipped(
                    order.getId(), customer.getId(), customer.getLoyaltyPoints()));
        }

        Customer lockedCustomer = customerRepository.findById(customer.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        lockedCustomer.setLoyaltyPoints(lockedCustomer.getLoyaltyPoints() + earned);
        customerRepository.save(lockedCustomer);

        order.setRewardProcessed(true);
        orderRepository.save(order);

        return Optional.of(LoyaltyAwardResponse.awarded(
                order.getId(),
                lockedCustomer.getId(),
                earned,
                lockedCustomer.getLoyaltyPoints()));
    }
}
