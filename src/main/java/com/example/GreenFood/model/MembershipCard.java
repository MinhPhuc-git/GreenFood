package com.example.GreenFood.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "membership_card")
public class MembershipCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne
    @JoinColumn(name = "customer_id", unique = true, nullable = false)
    private Customer customer;

    @Column(nullable = false)
    private String tier = "REGULAR"; // "REGULAR", "LOYAL"

    @Column(name = "total_sales", nullable = false)
    private BigDecimal totalSales = BigDecimal.ZERO;

    @Column(name = "issued_date")
    private LocalDateTime issuedDate;

    public MembershipCard() {
    }

    public MembershipCard(Customer customer, String tier, BigDecimal totalSales, LocalDateTime issuedDate) {
        this.customer = customer;
        this.tier = tier;
        this.totalSales = totalSales;
        this.issuedDate = issuedDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }
    public BigDecimal getTotalSales() { return totalSales; }
    public void setTotalSales(BigDecimal totalSales) { this.totalSales = totalSales; }
    public LocalDateTime getIssuedDate() { return issuedDate; }
    public void setIssuedDate(LocalDateTime issuedDate) { this.issuedDate = issuedDate; }
}
