package com.example.GreenFood.loyalty;

import com.example.GreenFood.model.Customer;
import com.example.GreenFood.model.RewardPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RewardPointRepository extends JpaRepository<RewardPoint, Integer> {

    // Tổng điểm còn hiệu lực của customer
    @Query("SELECT COALESCE(SUM(r.points), 0) FROM RewardPoint r WHERE r.customer = :customer AND r.expiryDate > CURRENT_TIMESTAMP")
    int sumValidPointsByCustomer(Customer customer);
}