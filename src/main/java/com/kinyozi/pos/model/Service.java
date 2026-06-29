package com.kinyozi.pos.model;

import java.math.BigDecimal;

public class Service {
    private int id;
    private int categoryId;
    private String categoryName;
    private String name;
    private String description;
    private BigDecimal price;
    private int durationMinutes;
    private boolean active;

    public Service() {}

    public Service(int id, String name, BigDecimal price, int durationMinutes) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.durationMinutes = durationMinutes;
        this.active = true;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() { return name + " - KES " + price; }
}