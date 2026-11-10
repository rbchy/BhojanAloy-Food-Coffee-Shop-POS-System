package com.bhojanaloy.model;

import java.time.LocalDateTime;

/** A registered loyalty-program customer: 1 point per order, 50 points = 1 free item. */
public class Customer {
    private int customerId;
    private String fullName;
    private String phone;
    private String email;
    private int loyaltyPoints;
    private LocalDateTime createdAt;

    public Customer() {}

    public Customer(String fullName, String phone, String email) {
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
    }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getLoyaltyPoints() { return loyaltyPoints; }
    public void setLoyaltyPoints(int loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() { return fullName + " (" + phone + ") — " + loyaltyPoints + " pts"; }
}
