package com.example.GreenFood.user;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.GreenFood.model.Customer;

import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Optional<Customer> findByAccount(String account);
    
    boolean existsByPhoneAndIdNot(String phone, int id);

    @Modifying
    @Transactional
    @Query(value = "INSERT IGNORE INTO customer (id, name, phone) VALUES (:id, :name, :phone)", nativeQuery = true)
    void insertCustomer(@Param("id") int id, @Param("name") String name, @Param("phone") String phone);
}
