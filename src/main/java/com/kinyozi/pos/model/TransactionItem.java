package com.kinyozi.pos.model;

import java.math.BigDecimal;

public class TransactionItem {
    private int id;
    private int transactionId;
    private String itemType; // SERVICE or PRODUCT
    private Integer serviceId;
    private Integer productId;
    private String itemName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private Integer barberId;
    private String barberName;

    public TransactionItem() {}

    public TransactionItem(String itemType, String itemName, int quantity, BigDecimal unitPrice) {
        this.itemType = itemType;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }
    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }
    public Integer getServiceId() { return serviceId; }
    public void setServiceId(Integer serviceId) { this.serviceId = serviceId; }
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public Integer getBarberId() { return barberId; }
    public void setBarberId(Integer barberId) { this.barberId = barberId; }
    public String getBarberName() { return barberName; }
    public void setBarberName(String barberName) { this.barberName = barberName; }
}