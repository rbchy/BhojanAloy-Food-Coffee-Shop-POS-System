package com.bhojanaloy.model;

import java.math.BigDecimal;

public class RecipeIngredient {
    private int recipeIngId;
    private int recipeId;
    private int ingredientId;
    private String ingredientName;
    private String unit;
    private BigDecimal quantityNeeded;

    public RecipeIngredient() {}

    public int getRecipeIngId() { return recipeIngId; }
    public void setRecipeIngId(int recipeIngId) { this.recipeIngId = recipeIngId; }

    public int getRecipeId() { return recipeId; }
    public void setRecipeId(int recipeId) { this.recipeId = recipeId; }

    public int getIngredientId() { return ingredientId; }
    public void setIngredientId(int ingredientId) { this.ingredientId = ingredientId; }

    public String getIngredientName() { return ingredientName; }
    public void setIngredientName(String ingredientName) { this.ingredientName = ingredientName; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public BigDecimal getQuantityNeeded() { return quantityNeeded; }
    public void setQuantityNeeded(BigDecimal quantityNeeded) { this.quantityNeeded = quantityNeeded; }

    @Override
    public String toString() { return ingredientName + ": " + quantityNeeded + " " + unit; }
}
