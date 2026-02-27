package com.example.finora.entities;

public class Wishlist {

    private int id;
    private int userId;
    private String name;
    private double price;

    public Wishlist(int id, int userId, String name, double price) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.price = price;
    }

    public Wishlist(int userId, String name, double price) {
        this.userId = userId;
        this.name = name;
        this.price = price;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getName() { return name; }
    public double getPrice() { return price; }
}