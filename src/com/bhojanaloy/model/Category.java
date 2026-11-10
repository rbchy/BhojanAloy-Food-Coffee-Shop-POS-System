package com.bhojanaloy.model;

public class Category {
    private int categoryId;
    private String categoryName;
    private String description;
    private int displayOrder;
    private String stationName; // which prep counter/station handles this category
    private boolean active;

    public Category() {}

    public Category(int categoryId, String categoryName, String description, int displayOrder, boolean active) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.description = description;
        this.displayOrder = displayOrder;
        this.active = active;
    }

    public Category(int categoryId, String categoryName, String description, int displayOrder, String stationName, boolean active) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.description = description;
        this.displayOrder = displayOrder;
        this.stationName = stationName;
        this.active = active;
    }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }

    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() { return categoryName; }
}
