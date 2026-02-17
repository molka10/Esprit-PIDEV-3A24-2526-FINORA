package com.example.finora_user.entities;

import java.time.LocalDate;

public class User {

    private int id;
    private String username;
    private String email;
    private String motDePasse;
    private String role;

    // New fields
    private String phone;
    private String address;
    private LocalDate dateOfBirth;

    // Default constructor
    public User() {}

    // Constructor without ID (for insert)
    public User(String username, String email, String motDePasse, String role) {
        this.username = username;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
    }

    // Full constructor (optional but professional)
    public User(int id, String username, String email, String motDePasse,
                String role, String phone, String address, LocalDate dateOfBirth) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
        this.phone = phone;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
    }

    // ---------------- GETTERS & SETTERS ----------------

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    // ---------------- toString ----------------

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                '}';
    }
}
