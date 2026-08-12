package com.example.GreenFood.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VNPayConfig {

    @Value("${vnpay.tmn-code}")
    public String tmnCode;

    @Value("${vnpay.hash-secret}")
    public String hashSecret;

    @Value("${vnpay.url}")
    public String paymentUrl;

    @Value("${vnpay.return-url}")
    public String returnUrl;
}