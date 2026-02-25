package com.example.finora.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Investment {

    private int investmentId;
    private String name;
    private String category;
    private String location;
    private BigDecimal estimatedValue;
    private String riskLevel;
    private String imageUrl;
    private String description;
    private LocalDateTime createdAt;

    public Investment() {}

    // Constructor WITHOUT createdAt
    public Investment(String name,
                      String category,
                      String location,
                      BigDecimal estimatedValue,
                      String riskLevel,
                      String imageUrl,
                      String description) {
        this.name = name;
        this.category = category;
        this.location = location;
        this.estimatedValue = estimatedValue;
        this.riskLevel = riskLevel;
        this.imageUrl = imageUrl;
        this.description = description;
    }

    public Investment(int investmentId, String name, String category, String location,
                      BigDecimal estimatedValue, String riskLevel,
                      String imageUrl, String description, LocalDateTime createdAt) {
        this.investmentId = investmentId;
        this.name = name;
        this.category = category;
        this.location = location;
        this.estimatedValue = estimatedValue;
        this.riskLevel = riskLevel;
        this.imageUrl = imageUrl;
        this.description = description;
        this.createdAt = createdAt;
    }

    // Getters & Setters
    public int getInvestmentId() { return investmentId; }
    public void setInvestmentId(int investmentId) { this.investmentId = investmentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public BigDecimal getEstimatedValue() { return estimatedValue; }
    public void setEstimatedValue(BigDecimal estimatedValue) { this.estimatedValue = estimatedValue; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
