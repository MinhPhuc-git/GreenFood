package com.example.GreenFood.security;

import com.example.GreenFood.model.User;
import com.example.GreenFood.admin.AdminRepository;
import com.example.GreenFood.user.CustomerRepository;
import com.example.GreenFood.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByAccount(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with account: " + username));

        boolean isAdmin = adminRepository.existsById(user.getId());
        String role = isAdmin ? "ROLE_ADMIN" : "ROLE_USER";

        return new org.springframework.security.core.userdetails.User(
                user.getAccount(),
                user.getPwd(),
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }
}
