package com.example.GreenFood.loyalty;

import com.example.GreenFood.model.Voucher;
import com.example.GreenFood.user.CustomerRepository;
import com.example.GreenFood.loyalty.VoucherRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VoucherService {
    private final VoucherRepository voucherRepository;
    private final CustomerRepository customerRepository;

    public VoucherService(VoucherRepository voucherRepository, CustomerRepository customerRepository) {
        this.voucherRepository = voucherRepository;
        this.customerRepository = customerRepository;
    }

    public record VoucherApplyResult(
            boolean valid,
            String message,
            String voucherCode,
            BigDecimal orderAmount,
            BigDecimal discountAmount,
            BigDecimal payableAmount) {
    }

    public List<Voucher> getAllVouchers() {
        return voucherRepository.findAll();
    }

    public Voucher getVoucher(int id) {
        return voucherRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy voucher"));
    }

    public Voucher createVoucher(Voucher voucher) {
        voucher.setId(0);
        attachCustomerReference(voucher);
        return voucherRepository.save(voucher);
    }

    public Voucher updateVoucher(int id, Voucher request) {
        Voucher voucher = getVoucher(id);
        voucher.setCustomer(request.getCustomer());
        attachCustomerReference(voucher);
        voucher.setVoucherCode(request.getVoucherCode());
        voucher.setDiscountType(request.getDiscountType());
        voucher.setDiscountValue(request.getDiscountValue());
        voucher.setMinOrderValue(request.getMinOrderValue());
        voucher.setExpiryDate(request.getExpiryDate());
        voucher.setIsActive(request.getIsActive());
        return voucherRepository.save(voucher);
    }

    public void deleteVoucher(int id) {
        voucherRepository.delete(getVoucher(id));
    }

    public VoucherApplyResult applyVoucher(String voucherCode, BigDecimal orderAmount) {
        if (voucherCode == null || voucherCode.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Yêu cầu mã voucher");
        }
        if (orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Giá trị đơn hàng không hợp lệ");
        }

        Voucher voucher = voucherRepository.findByVoucherCodeIgnoreCase(voucherCode.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mã voucher không tồn tại"));
        validateVoucher(voucher, orderAmount);

        BigDecimal discountAmount = calculateDiscount(voucher, orderAmount);
        BigDecimal payableAmount = orderAmount.subtract(discountAmount).max(BigDecimal.ZERO);
        return new VoucherApplyResult(true, "Voucher applied", voucher.getVoucherCode(), orderAmount, discountAmount, payableAmount);
    }

    private void validateVoucher(Voucher voucher, BigDecimal orderAmount) {
        if (!Boolean.TRUE.equals(voucher.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Voucher không hoạt động");
        }
        if (voucher.getExpiryDate() != null && voucher.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Voucher đã hết hạn");
        }
        if (voucher.getMinOrderValue() != null && orderAmount.compareTo(voucher.getMinOrderValue()) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Giá trị đơn hàng chưa đạt mức tối thiểu để dùng voucher");
        }
    }

    private BigDecimal calculateDiscount(Voucher voucher, BigDecimal orderAmount) {
        BigDecimal value = voucher.getDiscountValue() == null ? BigDecimal.ZERO : voucher.getDiscountValue();
        String type = voucher.getDiscountType() == null ? "" : voucher.getDiscountType().trim().toUpperCase();
        BigDecimal discount;
        if (type.equals("PERCENT") || type.equals("PERCENTAGE") || type.equals("%")) {
            discount = orderAmount.multiply(value).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            discount = value;
        }
        return discount.max(BigDecimal.ZERO).min(orderAmount);
    }

    private void attachCustomerReference(Voucher voucher) {
        if (voucher.getCustomer() != null) {
            voucher.setCustomer(customerRepository.getReferenceById(voucher.getCustomer().getId()));
        }
    }
}
