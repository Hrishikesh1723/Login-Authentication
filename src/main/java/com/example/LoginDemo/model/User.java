package com.example.LoginDemo.model;

public class User {
    private Long id;
    private String email;
    private String username;
    private String role;
    private Integer counter = 0;
    private String currentToken;

    // Default constructor
    public User() {}

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Integer getCounter() { return counter; }
    public void setCounter(Integer counter) { this.counter = counter; }

    public String getCurrentToken() { return currentToken; }
    public void setCurrentToken(String currentToken) { this.currentToken = currentToken; }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", counter=" + counter +
                '}';
    }
}
