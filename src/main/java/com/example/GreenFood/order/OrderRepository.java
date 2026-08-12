package com.example.GreenFood.order;

import com.example.GreenFood.model.Customer;
import com.example.GreenFood.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Integer> {

    // Lấy tất cả đơn hàng của 1 customer, mới nhất trước
    List<Orders> findByCustomerOrderByOrderDateDesc(Customer customer);

    // Lấy đơn hàng theo customer + trạng thái
    List<Orders> findByCustomerAndStatusOrderByOrderDateDesc(Customer customer, String status);

    // Đếm đơn hàng theo trạng thái (dùng cho dashboard)
    @Query("SELECT COUNT(o) FROM Orders o WHERE o.status = :status")
    long countByStatus(@Param("status") String status);
}
