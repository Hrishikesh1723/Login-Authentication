package com.example.LoginDemo.model;

import java.time.LocalDateTime;

public class UserSession {
    private Long id;
    private User user;
    private String browserId;
    private boolean active = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public UserSession() {}

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getBrowserId() { return browserId; }
    public void setBrowserId(String browserId) { this.browserId = browserId; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "UserSession{" +
                "id=" + id +
                ", user=" + user +
                ", browserId='" + browserId + '\'' +
                ", active=" + active +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}