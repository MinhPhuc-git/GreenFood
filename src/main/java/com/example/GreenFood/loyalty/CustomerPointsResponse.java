package com.example.GreenFood.loyalty;

public class CustomerPointsResponse {

    private int customerId;
    private String customerName;
    private int loyaltyPoints;

    public CustomerPointsResponse() {
    }

    public CustomerPointsResponse(int customerId, String customerName, int loyaltyPoints) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.loyaltyPoints = loyaltyPoints;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public int getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public void setLoyaltyPoints(int loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
    }
}
