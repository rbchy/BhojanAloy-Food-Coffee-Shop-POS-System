package com.bhojanaloy.model;

import java.math.BigDecimal;

public class OrderItem {
    private int orderItemId;
    private int orderId;
    private int itemId;
    private String itemName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
    private String stationName = "General";  // which prep counter handles this line item
    private String prepStatus = "PENDING";   // PENDING, PREPARING, READY, SERVED

    public OrderItem() {}

    public OrderItem(int itemId, String itemName, int quantity, BigDecimal unitPrice) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public OrderItem(int itemId, String itemName, int quantity, BigDecimal unitPrice, String stationName) {
        this(itemId, itemName, quantity, unitPrice);
        this.stationName = stationName;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        if (unitPrice != null) this.lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public int getOrderItemId() { return orderItemId; }
    public void setOrderItemId(int orderItemId) { this.orderItemId = orderItemId; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public int getQuantity() { return quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }

    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }

    public String getPrepStatus() { return prepStatus; }
    public void setPrepStatus(String prepStatus) { this.prepStatus = prepStatus; }

    @Override
    public String toString() { return itemName + " x" + quantity + " = $" + lineTotal; }
}
