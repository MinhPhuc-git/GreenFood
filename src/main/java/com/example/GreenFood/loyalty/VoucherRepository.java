package com.example.GreenFood.loyalty;

import com.example.GreenFood.model.Voucher;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Integer> {
    Optional<Voucher> findByVoucherCodeIgnoreCase(String voucherCode);
}
