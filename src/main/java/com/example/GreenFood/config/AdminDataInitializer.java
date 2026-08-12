package com.example.GreenFood.config;

import com.example.GreenFood.model.Admin;
import com.example.GreenFood.admin.AdminRepository;
import com.example.GreenFood.user.CustomerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class AdminDataInitializer {

    @Bean
    public CommandLineRunner initAdminData(AdminRepository adminRepository, CustomerRepository customerRepository) {
        return args -> {
            List<Admin> admins = adminRepository.findAll();
            for (Admin admin : admins) {
                if (!customerRepository.existsById(admin.getId())) {
                    try {
                        customerRepository.insertCustomer(admin.getId(), "Admin", "");
                    } catch (Exception ignored) {
                        // Da ton tai hoac vi pham constraint - bo qua
                    }
                }
            }
        };
    }
}
