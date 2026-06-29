package com.kinyozi.pos.model;

import java.math.BigDecimal;

public class Product {
    private int id;
    private String name;
    private String description;
    private String barcode;
    private BigDecimal sellingPrice;
    private BigDecimal costPrice;
    private int stockQuantity;
    private int reorderLevel;
    private boolean active;

    public Product() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }
    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }
    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isLowStock() { return stockQuantity <= reorderLevel; }

    @Override
    public String toString() { return name + " (Stock: " + stockQuantity + ")"; }
}