package com.bhojanaloy.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private int orderId;
    private String orderNumber;
    private int employeeId;
    private String employeeName;
    private LocalDateTime orderDate;
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal discount = BigDecimal.ZERO;
    private BigDecimal taxAmount = BigDecimal.ZERO;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private String paymentMethod;  // CASH, CARD, MOBILE_BANKING
    private String orderType = "DINE_IN";     // DINE_IN, PICKUP, DELIVERY
    private String orderSource = "IN_STORE";  // IN_STORE, PHONE, ONLINE
    private String customerName;
    private String customerPhone;
    private String customerAddress;
    private Integer customerId;         // set when the customer registers/is recognized for the loyalty program
    private String loyaltyRewardItem;   // name of the item comped free by a 50-point reward, if any
    private String status;         // COMPLETED, CANCELLED, REFUNDED
    private List<OrderItem> items = new ArrayList<>();

    public Order() {}

    /**
     * subtotal = sum of line totals; tax = (subtotal - discount) x rate; total = taxable + tax.
     * A discount larger than the subtotal makes the order free, never negative (the POS screen's
     * live preview already did this; the saved order did not). Money is rounded to cents here, so
     * the receipt, the payment amount and the database row all show the same figures.
     */
    public void recalculateTotals(BigDecimal taxRate) {
        subtotal = BigDecimal.ZERO;
        for (OrderItem oi : items) {
            subtotal = subtotal.add(oi.getLineTotal());
        }
        subtotal = subtotal.setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal taxable = subtotal.subtract(discount == null ? BigDecimal.ZERO : discount).max(BigDecimal.ZERO);
        taxAmount = taxable.multiply(taxRate).setScale(2, java.math.RoundingMode.HALF_UP);
        totalAmount = taxable.add(taxAmount).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }

    public String getOrderSource() { return orderSource; }
    public void setOrderSource(String orderSource) { this.orderSource = orderSource; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getCustomerAddress() { return customerAddress; }
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }

    public boolean isDelivery() { return "DELIVERY".equalsIgnoreCase(orderType); }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public String getLoyaltyRewardItem() { return loyaltyRewardItem; }
    public void setLoyaltyRewardItem(String loyaltyRewardItem) { this.loyaltyRewardItem = loyaltyRewardItem; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    @Override
    public String toString() { return orderNumber + " - $" + totalAmount; }
}
