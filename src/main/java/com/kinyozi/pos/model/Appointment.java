package com.kinyozi.pos.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

public class Appointment {
    private int id;
    private Integer customerId;
    private String customerName;
    private String customerPhone;
    private Integer barberId;
    private String barberName;
    private Integer serviceId;
    private String serviceName;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String status; // SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED, NO_SHOW
    private String notes;
    private LocalDateTime createdAt;

    public Appointment() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public Integer getBarberId() { return barberId; }
    public void setBarberId(Integer barberId) { this.barberId = barberId; }
    public String getBarberName() { return barberName; }
    public void setBarberName(String barberName) { this.barberName = barberName; }
    public Integer getServiceId() { return serviceId; }
    public void setServiceId(Integer serviceId) { this.serviceId = serviceId; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate appointmentDate) { this.appointmentDate = appointmentDate; }
    public LocalTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(LocalTime appointmentTime) { this.appointmentTime = appointmentTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getStatusDisplayText() {
        return switch (status) {
            case "SCHEDULED" -> "📅 Scheduled";
            case "IN_PROGRESS" -> "⚡ In Progress";
            case "COMPLETED" -> "✅ Completed";
            case "CANCELLED" -> "❌ Cancelled";
            case "NO_SHOW" -> "⚠️ No Show";
            default -> status;
        };
    }
}