package com.bhojanaloy.web.dto;

import java.math.BigDecimal;
import java.util.List;

public class CheckoutRequest {
    private List<CartLineRequest> items;
    private BigDecimal discount;
    private String paymentMethod;   // CASH, CARD, MOBILE_BANKING
    private String orderType;       // DINE_IN, PICKUP, DELIVERY
    private String customerName;
    private String customerPhone;
    private String customerAddress;

    public List<CartLineRequest> getItems() { return items; }
    public void setItems(List<CartLineRequest> items) { this.items = items; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getCustomerAddress() { return customerAddress; }
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }
}
