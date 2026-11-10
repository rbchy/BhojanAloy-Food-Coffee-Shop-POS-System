package com.bhojanaloy.web.dto;

import com.bhojanaloy.model.MenuItem;
import java.math.BigDecimal;

public class MenuItemResponse {
    private int itemId;
    private String itemName;
    private String description;
    private String categoryName;
    private BigDecimal price;
    private int stockQty;
    private boolean available;

    public static MenuItemResponse from(MenuItem m) {
        MenuItemResponse r = new MenuItemResponse();
        r.itemId = m.getItemId();
        r.itemName = m.getItemName();
        r.description = m.getDescription();
        r.categoryName = m.getCategoryName();
        r.price = m.getPrice();
        r.stockQty = m.getStockQty();
        r.available = m.isAvailable();
        return r;
    }

    public int getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public String getDescription() { return description; }
    public String getCategoryName() { return categoryName; }
    public BigDecimal getPrice() { return price; }
    public int getStockQty() { return stockQty; }
    public boolean isAvailable() { return available; }
}
