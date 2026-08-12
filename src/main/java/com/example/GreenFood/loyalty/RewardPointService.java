package com.example.GreenFood.loyalty;

import com.example.GreenFood.model.Customer;
import com.example.GreenFood.model.MembershipCard;
import com.example.GreenFood.model.Orders;
import com.example.GreenFood.loyalty.MembershipCardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class RewardPointService {

    private final MembershipCardRepository membershipCardRepository;
    private final LoyaltyPointService loyaltyPointService;

    private static final BigDecimal LOYAL_TIER_THRESHOLD = new BigDecimal("5000000");

    public RewardPointService(MembershipCardRepository membershipCardRepository,
                              LoyaltyPointService loyaltyPointService) {
        this.membershipCardRepository = membershipCardRepository;
        this.loyaltyPointService = loyaltyPointService;
    }

    /**
     * Gọi sau khi đơn thanh toán / hoàn thành: cộng điểm tích lũy + cập nhật hạng thành viên.
     */
    @Transactional
    public void processOrderCompletion(Orders order) {
        loyaltyPointService.tryAwardPointsForOrder(order.getId());
        updateMembershipTier(order);
    }

    @Transactional
    public void updateMembershipTier(Orders order) {
        Customer customer = order.getCustomer();
        BigDecimal totalAmount = order.getTotalAmount();

        MembershipCard card = membershipCardRepository.findByCustomer(customer).orElseGet(() -> {
            MembershipCard newCard = new MembershipCard(customer, "REGULAR", BigDecimal.ZERO, LocalDateTime.now());
            return membershipCardRepository.save(newCard);
        });

        if (totalAmount != null) {
            card.setTotalSales(card.getTotalSales().add(totalAmount));
        }

        if ("REGULAR".equals(card.getTier()) && card.getTotalSales().compareTo(LOYAL_TIER_THRESHOLD) >= 0) {
            card.setTier("LOYAL");
        }

        membershipCardRepository.save(card);
    }

    /** Điểm tích lũy hiển thị (từ cột customer.loyalty_points). */
    public int getValidPoints(Customer customer) {
        return loyaltyPointService.getCustomerPoints(customer.getId()).getLoyaltyPoints();
    }
}
