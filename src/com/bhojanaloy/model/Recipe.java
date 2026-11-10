package com.bhojanaloy.model;

import java.util.ArrayList;
import java.util.List;

public class Recipe {
    private int recipeId;
    private int itemId;
    private String itemName;
    private String itemImagePath; // from menu_items.image_path, for the Recipes screen's product photo
    private String recipeName;
    private int prepTimeMin;
    private String difficulty;   // Easy / Medium / Hard
    private String instructions;    // English (default/fallback)
    private String instructionsBn;  // বাংলা
    private String instructionsEs;  // Español
    private String instructionsHi;  // हिंदी
    private String instructionsAr;  // العربية
    private int yieldQty;
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    public Recipe() {}

    public int getRecipeId() { return recipeId; }
    public void setRecipeId(int recipeId) { this.recipeId = recipeId; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getItemImagePath() { return itemImagePath; }
    public void setItemImagePath(String itemImagePath) { this.itemImagePath = itemImagePath; }

    public String getRecipeName() { return recipeName; }
    public void setRecipeName(String recipeName) { this.recipeName = recipeName; }

    public int getPrepTimeMin() { return prepTimeMin; }
    public void setPrepTimeMin(int prepTimeMin) { this.prepTimeMin = prepTimeMin; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public String getInstructionsBn() { return instructionsBn; }
    public void setInstructionsBn(String instructionsBn) { this.instructionsBn = instructionsBn; }

    public String getInstructionsEs() { return instructionsEs; }
    public void setInstructionsEs(String instructionsEs) { this.instructionsEs = instructionsEs; }

    public String getInstructionsHi() { return instructionsHi; }
    public void setInstructionsHi(String instructionsHi) { this.instructionsHi = instructionsHi; }

    public String getInstructionsAr() { return instructionsAr; }
    public void setInstructionsAr(String instructionsAr) { this.instructionsAr = instructionsAr; }

    /** Returns the instructions for the given language code (en/bn/es/hi/ar),
     *  falling back to English whenever that language hasn't been filled in. */
    public String getInstructionsFor(String languageCode) {
        String text;
        switch (languageCode) {
            case "bn": text = instructionsBn; break;
            case "es": text = instructionsEs; break;
            case "hi": text = instructionsHi; break;
            case "ar": text = instructionsAr; break;
            default: text = instructions;
        }
        return (text != null && !text.trim().isEmpty()) ? text : instructions;
    }

    public int getYieldQty() { return yieldQty; }
    public void setYieldQty(int yieldQty) { this.yieldQty = yieldQty; }

    public List<RecipeIngredient> getIngredients() { return ingredients; }
    public void setIngredients(List<RecipeIngredient> ingredients) { this.ingredients = ingredients; }

    @Override
    public String toString() { return recipeName + " (" + prepTimeMin + " min, " + difficulty + ")"; }
}