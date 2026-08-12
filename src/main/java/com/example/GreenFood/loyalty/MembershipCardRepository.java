package com.example.GreenFood.loyalty;

import com.example.GreenFood.model.Customer;
import com.example.GreenFood.model.MembershipCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MembershipCardRepository extends JpaRepository<MembershipCard, Integer> {
    Optional<MembershipCard> findByCustomer(Customer customer);
}
