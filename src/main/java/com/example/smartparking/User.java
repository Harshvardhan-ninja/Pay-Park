package com.example.smartparking;

public class User {
    private String name;
    private String email;
    private String username;

    // Empty constructor needed for Firebase
    public User() {}

    public User(String name, String email, String username) {
        this.name = name;
        this.email = email;
        this.username = username;
    }

    // Add getters and setters
}