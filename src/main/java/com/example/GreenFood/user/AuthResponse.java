package com.example.GreenFood.user;

public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private String account;

    public AuthResponse(String token, String account) {
        this.token = token;
        this.account = account;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getAccount() { return account; }
    public void setAccount(String account) { this.account = account; }
}
