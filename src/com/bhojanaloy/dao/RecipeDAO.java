package com.bhojanaloy.dao;

import com.bhojanaloy.config.DBConnection;
import com.bhojanaloy.model.Recipe;
import com.bhojanaloy.model.RecipeIngredient;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecipeDAO {

    public List<Recipe> getAll() {
        List<Recipe> list = new ArrayList<>();
        String sql = "SELECT r.*, mi.item_name, mi.image_path FROM recipes r " +
                     "JOIN menu_items mi ON r.item_id = mi.item_id ORDER BY mi.item_name";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Recipe recipe = mapRow(rs);
                recipe.setIngredients(getIngredientsForRecipe(recipe.getRecipeId(), con));
                list.add(recipe);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Recipe getByItemId(int itemId) {
        String sql = "SELECT r.*, mi.item_name, mi.image_path FROM recipes r " +
                     "JOIN menu_items mi ON r.item_id = mi.item_id WHERE r.item_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Recipe recipe = mapRow(rs);
                    recipe.setIngredients(getIngredientsForRecipe(recipe.getRecipeId(), con));
                    return recipe;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<RecipeIngredient> getIngredientsForRecipe(int recipeId, Connection con) throws SQLException {
        List<RecipeIngredient> list = new ArrayList<>();
        String sql = "SELECT ri.*, raw.ingredient_name, raw.unit FROM recipe_ingredients ri " +
                     "JOIN raw_ingredients raw ON ri.ingredient_id = raw.ingredient_id WHERE ri.recipe_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, recipeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RecipeIngredient ri = new RecipeIngredient();
                    ri.setRecipeIngId(rs.getInt("recipe_ing_id"));
                    ri.setRecipeId(rs.getInt("recipe_id"));
                    ri.setIngredientId(rs.getInt("ingredient_id"));
                    ri.setIngredientName(rs.getString("ingredient_name"));
                    ri.setUnit(rs.getString("unit"));
                    ri.setQuantityNeeded(rs.getBigDecimal("quantity_needed"));
                    list.add(ri);
                }
            }
        }
        return list;
    }

    public boolean addRecipe(Recipe r) {
        String sql = "INSERT INTO recipes (item_id, recipe_name, prep_time_min, difficulty, instructions, " +
                     "instructions_bn, instructions_es, instructions_hi, instructions_ar, yield_qty) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bindRecipeFields(ps, r);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateRecipe(Recipe r) {
        String sql = "UPDATE recipes SET item_id=?, recipe_name=?, prep_time_min=?, difficulty=?, instructions=?, " +
                     "instructions_bn=?, instructions_es=?, instructions_hi=?, instructions_ar=?, yield_qty=? WHERE recipe_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bindRecipeFields(ps, r);
            ps.setInt(11, r.getRecipeId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteRecipe(int recipeId) {
        // recipe_ingredients rows reference this recipe, so clear those first.
        String delIngredients = "DELETE FROM recipe_ingredients WHERE recipe_id = ?";
        String delRecipe = "DELETE FROM recipes WHERE recipe_id = ?";
        try (Connection con = DBConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(delIngredients)) {
                ps.setInt(1, recipeId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(delRecipe)) {
                ps.setInt(1, recipeId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void bindRecipeFields(PreparedStatement ps, Recipe r) throws SQLException {
        ps.setInt(1, r.getItemId());
        ps.setString(2, r.getRecipeName());
        ps.setInt(3, r.getPrepTimeMin());
        ps.setString(4, r.getDifficulty());
        ps.setString(5, r.getInstructions());
        ps.setString(6, r.getInstructionsBn());
        ps.setString(7, r.getInstructionsEs());
        ps.setString(8, r.getInstructionsHi());
        ps.setString(9, r.getInstructionsAr());
        ps.setInt(10, r.getYieldQty());
    }

    private Recipe mapRow(ResultSet rs) throws SQLException {
        Recipe r = new Recipe();
        r.setRecipeId(rs.getInt("recipe_id"));
        r.setItemId(rs.getInt("item_id"));
        r.setItemName(rs.getString("item_name"));
        r.setItemImagePath(rs.getString("image_path"));
        r.setRecipeName(rs.getString("recipe_name"));
        r.setPrepTimeMin(rs.getInt("prep_time_min"));
        r.setDifficulty(rs.getString("difficulty"));
        r.setInstructions(rs.getString("instructions"));
        r.setInstructionsBn(rs.getString("instructions_bn"));
        r.setInstructionsEs(rs.getString("instructions_es"));
        r.setInstructionsHi(rs.getString("instructions_hi"));
        r.setInstructionsAr(rs.getString("instructions_ar"));
        r.setYieldQty(rs.getInt("yield_qty"));
        return r;
    }
}