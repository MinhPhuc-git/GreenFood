package com.example.GreenFood.model;

import jakarta.persistence.*;

@Entity
@Table (name = "admin")
@PrimaryKeyJoinColumn(name = "id")
public class Admin extends User{
	public Admin() {
	}

	public Admin(String account, String pwd, String status, java.time.LocalDateTime createdAt) {
		super(account, pwd, status, createdAt);
	}
}
