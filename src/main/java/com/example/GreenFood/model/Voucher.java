package com.example.GreenFood.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "voucher")
public class Voucher {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@ManyToOne
	@JoinColumn(name = "customer_id")
	private Customer customer;

	@Column(nullable = false, unique = true)
	private String voucherCode;
	private String discountType;
	private BigDecimal discountValue;
	private BigDecimal minOrderValue;
	private LocalDateTime expiryDate;
	private Boolean isActive;

	public Voucher() {
	}

	public Voucher(Customer customer, String voucherCode, String discountType, BigDecimal discountValue, BigDecimal minOrderValue, LocalDateTime expiryDate, Boolean isActive) {
		this.customer = customer;
		this.voucherCode = voucherCode;
		this.discountType = discountType;
		this.discountValue = discountValue;
		this.minOrderValue = minOrderValue;
		this.expiryDate = expiryDate;
		this.isActive = isActive;
	}

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }
	public Customer getCustomer() { return customer; }
	public void setCustomer(Customer customer) { this.customer = customer; }
	public String getVoucherCode() { return voucherCode; }
	public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }
	public String getDiscountType() { return discountType; }
	public void setDiscountType(String discountType) { this.discountType = discountType; }
	public BigDecimal getDiscountValue() { return discountValue; }
	public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
	public BigDecimal getMinOrderValue() { return minOrderValue; }
	public void setMinOrderValue(BigDecimal minOrderValue) { this.minOrderValue = minOrderValue; }
	public LocalDateTime getExpiryDate() { return expiryDate; }
	public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }
	public Boolean getIsActive() { return isActive; }
	public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
