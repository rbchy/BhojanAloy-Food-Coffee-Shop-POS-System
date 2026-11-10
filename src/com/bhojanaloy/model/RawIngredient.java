package com.bhojanaloy.model;

import java.math.BigDecimal;

public class RawIngredient {
    private int ingredientId;
    private String ingredientName;
    private String unit;
    private BigDecimal stockQty;
    private BigDecimal reorderLevel;
    private BigDecimal unitCost;
    private Integer supplierId;

    public RawIngredient() {}

    public boolean isLowStock() {
        return stockQty != null && reorderLevel != null && stockQty.compareTo(reorderLevel) <= 0;
    }

    public int getIngredientId() { return ingredientId; }
    public void setIngredientId(int ingredientId) { this.ingredientId = ingredientId; }

    public String getIngredientName() { return ingredientName; }
    public void setIngredientName(String ingredientName) { this.ingredientName = ingredientName; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public BigDecimal getStockQty() { return stockQty; }
    public void setStockQty(BigDecimal stockQty) { this.stockQty = stockQty; }

    public BigDecimal getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(BigDecimal reorderLevel) { this.reorderLevel = reorderLevel; }

    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }

    public Integer getSupplierId() { return supplierId; }
    public void setSupplierId(Integer supplierId) { this.supplierId = supplierId; }

    @Override
    public String toString() { return ingredientName + " (" + stockQty + " " + unit + ")"; }
}
