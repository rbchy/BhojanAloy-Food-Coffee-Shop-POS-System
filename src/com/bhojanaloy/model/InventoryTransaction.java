package com.bhojanaloy.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InventoryTransaction {
    private int txnId;
    private Integer ingredientId;
    private Integer itemId;
    private String txnType; // PURCHASE_IN, SALE_OUT, ADJUSTMENT, WASTE
    private BigDecimal quantity;
    private LocalDateTime txnDate;
    private Integer referenceId;
    private String notes;

    public InventoryTransaction() {}

    public int getTxnId() { return txnId; }
    public void setTxnId(int txnId) { this.txnId = txnId; }

    public Integer getIngredientId() { return ingredientId; }
    public void setIngredientId(Integer ingredientId) { this.ingredientId = ingredientId; }

    public Integer getItemId() { return itemId; }
    public void setItemId(Integer itemId) { this.itemId = itemId; }

    public String getTxnType() { return txnType; }
    public void setTxnType(String txnType) { this.txnType = txnType; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public LocalDateTime getTxnDate() { return txnDate; }
    public void setTxnDate(LocalDateTime txnDate) { this.txnDate = txnDate; }

    public Integer getReferenceId() { return referenceId; }
    public void setReferenceId(Integer referenceId) { this.referenceId = referenceId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    @Override
    public String toString() { return txnType + ": " + quantity; }
}
