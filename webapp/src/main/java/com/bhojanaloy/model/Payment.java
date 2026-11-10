package com.bhojanaloy.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A single payment transaction against an order. For CASH this is recorded
 * as an immediate SUCCESS; for CARD / MOBILE_BANKING it represents the
 * result of the (simulated) gateway call made via PaymentDialog.
 */
public class Payment {
    private int paymentId;
    private int orderId;
    private String method;           // CASH, CARD, MOBILE_BANKING
    private String provider;         // Visa, Mastercard, bKash, Nagad, Rocket...
    private String referenceNumber;  // gateway/transaction reference
    private BigDecimal amount;
    private String status;           // SUCCESS, FAILED, PENDING
    private LocalDateTime processedAt;

    public Payment() {}

    public boolean isSuccessful() { return "SUCCESS".equalsIgnoreCase(status); }

    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    @Override
    public String toString() { return method + " (" + provider + ") - " + status; }
}
