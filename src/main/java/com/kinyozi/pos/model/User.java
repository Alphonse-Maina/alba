package com.kinyozi.pos.model;

import java.time.LocalDateTime;

public class User {
    private int id;
    private String username;
    private String passwordHash;
    private String fullName;
    private String role;
    private String phone;
    private String email;
    private boolean active;
    private LocalDateTime createdAt;

    public User() {}

    public User(int id, String username, String fullName, String role, String phone, String email, boolean active) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.phone = phone;
        this.email = email;
        this.active = active;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isAdmin() { return "ADMIN".equals(role); }
    public boolean isManager() { return "ADMIN".equals(role) || "MANAGER".equals(role); }

    @Override
    public String toString() { return fullName + " (" + role + ")"; }
}