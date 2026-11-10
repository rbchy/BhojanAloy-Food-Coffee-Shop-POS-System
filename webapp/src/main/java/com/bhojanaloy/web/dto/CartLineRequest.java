package com.bhojanaloy.web.dto;

public class CartLineRequest {
    private int itemId;
    private int quantity;

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
