package com.example.GreenFood.admin;

import com.example.GreenFood.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Integer> {

    @Modifying
    @Transactional
    @Query(value = "INSERT IGNORE INTO customer (id, name, phone) SELECT id, 'Admin', CONCAT('00', id) FROM admin", nativeQuery = true)
    void initializeAdminsAsCustomers();
}
