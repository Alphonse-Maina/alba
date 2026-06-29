package com.kinyozi.pos.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Transaction {
    private int id;
    private String receiptNumber;
    private Integer customerId;
    private String customerName;
    private Integer servedBy;
    private String barberName;
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal taxAmount = BigDecimal.ZERO;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private BigDecimal amountPaid = BigDecimal.ZERO;
    private BigDecimal changeGiven = BigDecimal.ZERO;
    private String paymentMethod = "CASH";
    private String mpesaCode;
    private String status = "COMPLETED";
    private String notes;
    private LocalDateTime createdAt;
    private List<TransactionItem> items = new ArrayList<>();

    public Transaction() {}

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }
    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public Integer getServedBy() { return servedBy; }
    public void setServedBy(Integer servedBy) { this.servedBy = servedBy; }
    public String getBarberName() { return barberName; }
    public void setBarberName(String barberName) { this.barberName = barberName; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }
    public BigDecimal getChangeGiven() { return changeGiven; }
    public void setChangeGiven(BigDecimal changeGiven) { this.changeGiven = changeGiven; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getMpesaCode() { return mpesaCode; }
    public void setMpesaCode(String mpesaCode) { this.mpesaCode = mpesaCode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<TransactionItem> getItems() { return items; }
    public void setItems(List<TransactionItem> items) { this.items = items; }
    public void addItem(TransactionItem item) { this.items.add(item); }
}