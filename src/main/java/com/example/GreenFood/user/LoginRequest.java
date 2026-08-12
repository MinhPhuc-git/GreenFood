package com.example.GreenFood.user;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    @NotBlank(message = "Tài khoản không được để trống")
    private String account;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;

    public String getAccount() { return account; }
    public void setAccount(String account) { this.account = account; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
