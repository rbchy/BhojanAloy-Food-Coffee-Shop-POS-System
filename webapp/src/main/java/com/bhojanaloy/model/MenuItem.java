package com.bhojanaloy.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MenuItem {
    private int itemId;
    private int categoryId;
    private String categoryName;   // convenience join field
    private String stationName;    // convenience join field (from category.station_name)
    private String itemName;
    private String description;
    private BigDecimal price;
    private BigDecimal costPrice;
    private int stockQty;
    private int reorderLevel;
    private boolean available;
    private String imagePath;
    private LocalDateTime createdAt;

    public MenuItem() {}

    public MenuItem(int itemId, int categoryId, String itemName, BigDecimal price, int stockQty) {
        this.itemId = itemId;
        this.categoryId = categoryId;
        this.itemName = itemName;
        this.price = price;
        this.stockQty = stockQty;
    }

    public boolean isLowStock() { return stockQty <= reorderLevel; }

    public BigDecimal getProfitMargin() {
        if (price == null || costPrice == null) return BigDecimal.ZERO;
        return price.subtract(costPrice);
    }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }

    public int getStockQty() { return stockQty; }
    public void setStockQty(int stockQty) { this.stockQty = stockQty; }

    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() { return itemName + " - $" + price; }
}
