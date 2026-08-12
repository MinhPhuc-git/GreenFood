package com.example.GreenFood.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "`Order`")
public class Orders {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(name = "delivery_fee")
	private BigDecimal deliveryFee;
	@Column(name = "discount_amount")
	private BigDecimal discountAmount;
	@Column(name = "total_amount")
	private BigDecimal totalAmount;
	@Column(name = "order_date")
	private LocalDateTime orderDate;
	@Column(name = "delivery_time")
	private LocalDateTime deliveryTime;
	@Column(name = "delivery_address")
	private String deliveryAddress;
	@Column(name = "status")
	private String status;
	@Column(name = "payment_method")
	private String paymentMethod;

	@Column(name = "reward_processed", nullable = false)
	private boolean rewardProcessed = false;
	
	@ManyToOne
	@JoinColumn(name = "customer_id", nullable = false)
	private Customer customer;

	@ManyToOne
	@JoinColumn(name = "voucher_id")
	private Voucher voucher;
	
	@JsonManagedReference
    @OneToMany (mappedBy = "orders", cascade = CascadeType.ALL)
	private List<OrderItem> listOrderItems;

	public Orders() {
	}

	public Orders(BigDecimal deliveryFee, BigDecimal discountAmount, BigDecimal totalAmount, LocalDateTime orderDate, LocalDateTime deliveryTime, String deliveryAddress, String status, String paymentMethod, Customer customer, Voucher voucher, List<OrderItem> listOrderItems) {
		this.deliveryFee = deliveryFee;
		this.discountAmount = discountAmount;
		this.totalAmount = totalAmount;
		this.orderDate = orderDate;
		this.deliveryTime = deliveryTime;
		this.deliveryAddress = deliveryAddress;
		this.status = status;
		this.paymentMethod = paymentMethod;
		this.customer = customer;
		this.voucher = voucher;
		this.listOrderItems = listOrderItems;
	}

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }
	public BigDecimal getDeliveryFee() { return deliveryFee; }
	public void setDeliveryFee(BigDecimal deliveryFee) { this.deliveryFee = deliveryFee; }
	public BigDecimal getDiscountAmount() { return discountAmount; }
	public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
	public BigDecimal getTotalAmount() { return totalAmount; }
	public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
	public LocalDateTime getOrderDate() { return orderDate; }
	public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
	public LocalDateTime getDeliveryTime() { return deliveryTime; }
	public void setDeliveryTime(LocalDateTime deliveryTime) { this.deliveryTime = deliveryTime; }
	public String getDeliveryAddress() { return deliveryAddress; }
	public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
	public String getPaymentMethod() { return paymentMethod; }
	public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
	public boolean isRewardProcessed() { return rewardProcessed; }
	public void setRewardProcessed(boolean rewardProcessed) { this.rewardProcessed = rewardProcessed; }
	public Customer getCustomer() { return customer; }
	public void setCustomer(Customer customer) { this.customer = customer; }
	public Voucher getVoucher() { return voucher; }
	public void setVoucher(Voucher voucher) { this.voucher = voucher; }
	public List<OrderItem> getListOrderItems() { return listOrderItems; }
	public void setListOrderItems(List<OrderItem> listOrderItems) { this.listOrderItems = listOrderItems; }
}
