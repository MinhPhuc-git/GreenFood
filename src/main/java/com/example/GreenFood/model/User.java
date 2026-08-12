package com.example.GreenFood.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@Inheritance(strategy = InheritanceType.JOINED)
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Column(nullable = false, unique = true)
	private String account;
	
	@Column(nullable = false)
	private String pwd;
	
	@Column(nullable = false)
	private String status;
	
	private LocalDateTime createdAt;

	public User() {
	}

	public User(String account, String pwd, String status, LocalDateTime createdAt) {
		this.account = account;
		this.pwd = pwd;
		this.status = status;
		this.createdAt = createdAt;
	}

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }
	public String getAccount() { return account; }
	public void setAccount(String account) { this.account = account; }
	public String getPwd() { return pwd; }
	public void setPwd(String pwd) { this.pwd = pwd; }
	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
