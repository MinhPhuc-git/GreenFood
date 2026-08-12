package com.example.GreenFood.user;

import com.example.GreenFood.user.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import com.example.GreenFood.security.JwtTokenProvider;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider tokenProvider;

    public AuthController(AuthService authService, JwtTokenProvider tokenProvider) {
        this.authService = authService;
        this.tokenProvider = tokenProvider;
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        try {
            // Xác thực thông qua AuthService (xử lý cả plain text lẫn BCrypt)
            Map<String, Object> result = new java.util.HashMap<>(authService.login(
                    body.get("account"),
                    body.get("pwd")
            ));

            // Tạo JWT token
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            body.get("account"), null, Collections.emptyList());
            String jwt = tokenProvider.generateToken(auth);
            result.put("token", jwt);

            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        try {
            Map<String, Object> result = authService.register(
                    body.get("account"),
                    body.get("pwd"),
                    body.get("confirmPwd"),
                    body.get("name"),
                    body.get("phone")
            );
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}