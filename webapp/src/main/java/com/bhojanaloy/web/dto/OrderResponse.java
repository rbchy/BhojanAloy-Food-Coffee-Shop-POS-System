package com.bhojanaloy.web.dto;

import com.bhojanaloy.model.Order;
import com.bhojanaloy.model.OrderItem;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class OrderResponse {
    private int orderId;
    private String orderNumber;
    private String employeeName;
    private LocalDateTime orderDate;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String orderType;
    private String status;
    private List<LineResponse> items;

    public static class LineResponse {
        public String itemName;
        public int quantity;
        public BigDecimal unitPrice;
        public BigDecimal lineTotal;
    }

    public static OrderResponse from(Order o) {
        OrderResponse r = new OrderResponse();
        r.orderId = o.getOrderId();
        r.orderNumber = o.getOrderNumber();
        r.employeeName = o.getEmployeeName();
        r.orderDate = o.getOrderDate();
        r.subtotal = o.getSubtotal();
        r.discount = o.getDiscount();
        r.taxAmount = o.getTaxAmount();
        r.totalAmount = o.getTotalAmount();
        r.paymentMethod = o.getPaymentMethod();
        r.orderType = o.getOrderType();
        r.status = o.getStatus();
        if (o.getItems() != null) {
            r.items = o.getItems().stream().map(OrderResponse::toLine).collect(Collectors.toList());
        }
        return r;
    }

    private static LineResponse toLine(OrderItem oi) {
        LineResponse l = new LineResponse();
        l.itemName = oi.getItemName();
        l.quantity = oi.getQuantity();
        l.unitPrice = oi.getUnitPrice();
        l.lineTotal = oi.getLineTotal();
        return l;
    }

    public int getOrderId() { return orderId; }
    public String getOrderNumber() { return orderNumber; }
    public String getEmployeeName() { return employeeName; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getDiscount() { return discount; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getOrderType() { return orderType; }
    public String getStatus() { return status; }
    public List<LineResponse> getItems() { return items; }
}
