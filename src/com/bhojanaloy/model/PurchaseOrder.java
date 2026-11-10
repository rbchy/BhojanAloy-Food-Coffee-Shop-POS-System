package com.bhojanaloy.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PurchaseOrder {
    private int poId;
    private int supplierId;
    private String supplierName;
    private LocalDateTime orderDate;
    private String status; // PENDING, RECEIVED, CANCELLED
    private BigDecimal totalAmount;

    public PurchaseOrder() {}

    public int getPoId() { return poId; }
    public void setPoId(int poId) { this.poId = poId; }

    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    @Override
    public String toString() { return "PO#" + poId + " - " + supplierName + " (" + status + ")"; }
}
