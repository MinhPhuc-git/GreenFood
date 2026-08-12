package com.example.GreenFood.loyalty;

public class LoyaltyAwardResponse {

    private int orderId;
    private int customerId;
    private int pointsEarned;
    private int totalLoyaltyPoints;
    private boolean alreadyProcessed;
    private String message;

    public LoyaltyAwardResponse() {
    }

    public static LoyaltyAwardResponse awarded(int orderId, int customerId, int pointsEarned, int totalLoyaltyPoints) {
        LoyaltyAwardResponse r = new LoyaltyAwardResponse();
        r.orderId = orderId;
        r.customerId = customerId;
        r.pointsEarned = pointsEarned;
        r.totalLoyaltyPoints = totalLoyaltyPoints;
        r.alreadyProcessed = false;
        r.message = "Bạn đã nhận được +" + pointsEarned + " điểm thưởng. Tổng điểm hiện tại: "
                + totalLoyaltyPoints + " điểm.";
        return r;
    }

    public static LoyaltyAwardResponse skipped(int orderId, int customerId, int totalLoyaltyPoints) {
        LoyaltyAwardResponse r = new LoyaltyAwardResponse();
        r.orderId = orderId;
        r.customerId = customerId;
        r.pointsEarned = 0;
        r.totalLoyaltyPoints = totalLoyaltyPoints;
        r.alreadyProcessed = true;
        r.message = "Đơn hàng đã được cộng điểm trước đó.";
        return r;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getPointsEarned() {
        return pointsEarned;
    }

    public void setPointsEarned(int pointsEarned) {
        this.pointsEarned = pointsEarned;
    }

    public int getTotalLoyaltyPoints() {
        return totalLoyaltyPoints;
    }

    public void setTotalLoyaltyPoints(int totalLoyaltyPoints) {
        this.totalLoyaltyPoints = totalLoyaltyPoints;
    }

    public boolean isAlreadyProcessed() {
        return alreadyProcessed;
    }

    public void setAlreadyProcessed(boolean alreadyProcessed) {
        this.alreadyProcessed = alreadyProcessed;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
