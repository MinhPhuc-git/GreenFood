package com.example.GreenFood.loyalty;

import com.example.GreenFood.model.Customer;
import com.example.GreenFood.model.Voucher;
import com.example.GreenFood.loyalty.VoucherService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api")
public class VoucherController {
    private final VoucherService voucherService;

    public VoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    public record VoucherRequest(
            Integer customerId,
            String voucherCode,
            String discountType,
            BigDecimal discountValue,
            BigDecimal minOrderValue,
            LocalDateTime expiryDate,
            Boolean isActive) {
    }

    public record VoucherResponse(
            int id,
            Integer customerId,
            String voucherCode,
            String discountType,
            BigDecimal discountValue,
            BigDecimal minOrderValue,
            LocalDateTime expiryDate,
            Boolean isActive) {
    }

    public record ApplyVoucherRequest(String voucherCode, BigDecimal orderAmount) {
    }

    @PostMapping("/vouchers/apply")
    public VoucherService.VoucherApplyResult applyVoucher(@RequestBody ApplyVoucherRequest request) {
        return voucherService.applyVoucher(request.voucherCode(), request.orderAmount());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/vouchers")
    public List<VoucherResponse> getVouchers() {
        return voucherService.getAllVouchers().stream().map(this::toVoucherResponse).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/vouchers/{id}")
    public VoucherResponse getVoucher(@PathVariable int id) {
        return toVoucherResponse(voucherService.getVoucher(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/vouchers")
    @ResponseStatus(HttpStatus.CREATED)
    public VoucherResponse createVoucher(@RequestBody VoucherRequest request) {
        return toVoucherResponse(voucherService.createVoucher(toVoucher(request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/vouchers/{id}")
    public VoucherResponse updateVoucher(@PathVariable int id, @RequestBody VoucherRequest request) {
        return toVoucherResponse(voucherService.updateVoucher(id, toVoucher(request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/vouchers/{id}")
    public ResponseEntity<Void> deleteVoucher(@PathVariable int id) {
        voucherService.deleteVoucher(id);
        return ResponseEntity.noContent().build();
    }

    private Voucher toVoucher(VoucherRequest request) {
        Voucher voucher = new Voucher();
        if (request.customerId() != null) {
            Customer customer = new Customer();
            customer.setId(request.customerId());
            voucher.setCustomer(customer);
        }
        voucher.setVoucherCode(request.voucherCode());
        voucher.setDiscountType(request.discountType());
        voucher.setDiscountValue(request.discountValue());
        voucher.setMinOrderValue(request.minOrderValue());
        voucher.setExpiryDate(request.expiryDate());
        voucher.setIsActive(request.isActive());
        return voucher;
    }

    private VoucherResponse toVoucherResponse(Voucher voucher) {
        Customer customer = voucher.getCustomer();
        return new VoucherResponse(
                voucher.getId(),
                customer == null ? null : customer.getId(),
                voucher.getVoucherCode(),
                voucher.getDiscountType(),
                voucher.getDiscountValue(),
                voucher.getMinOrderValue(),
                voucher.getExpiryDate(),
                voucher.getIsActive());
    }
}
