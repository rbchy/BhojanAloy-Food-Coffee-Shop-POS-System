package com.bhojanaloy.dao;

import com.bhojanaloy.config.DBConnection;
import com.bhojanaloy.model.RawIngredient;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RawIngredientDAO {

    public List<RawIngredient> getAll() {
        List<RawIngredient> list = new ArrayList<>();
        String sql = "SELECT * FROM raw_ingredients ORDER BY ingredient_name";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<RawIngredient> getLowStock() {
        List<RawIngredient> list = new ArrayList<>();
        String sql = "SELECT * FROM raw_ingredients WHERE stock_qty <= reorder_level ORDER BY ingredient_name";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean addIngredient(RawIngredient ri) {
        String sql = "INSERT INTO raw_ingredients (ingredient_name, unit, stock_qty, reorder_level, unit_cost, supplier_id) VALUES (?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ri.getIngredientName());
            ps.setString(2, ri.getUnit());
            ps.setBigDecimal(3, ri.getStockQty());
            ps.setBigDecimal(4, ri.getReorderLevel());
            ps.setBigDecimal(5, ri.getUnitCost());
            if (ri.getSupplierId() != null) ps.setInt(6, ri.getSupplierId()); else ps.setNull(6, Types.INTEGER);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean adjustStock(int ingredientId, java.math.BigDecimal deltaQty, String txnType, String notes) {
        String updateSql = "UPDATE raw_ingredients SET stock_qty = stock_qty + ? WHERE ingredient_id = ?";
        String logSql = "INSERT INTO inventory_transactions (ingredient_id, txn_type, quantity, notes) VALUES (?,?,?,?)";
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                ps.setBigDecimal(1, deltaQty);
                ps.setInt(2, ingredientId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(logSql)) {
                ps.setInt(1, ingredientId);
                ps.setString(2, txnType);
                ps.setBigDecimal(3, deltaQty);
                ps.setString(4, notes);
                ps.executeUpdate();
            }
            con.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private RawIngredient mapRow(ResultSet rs) throws SQLException {
        RawIngredient ri = new RawIngredient();
        ri.setIngredientId(rs.getInt("ingredient_id"));
        ri.setIngredientName(rs.getString("ingredient_name"));
        ri.setUnit(rs.getString("unit"));
        ri.setStockQty(rs.getBigDecimal("stock_qty"));
        ri.setReorderLevel(rs.getBigDecimal("reorder_level"));
        ri.setUnitCost(rs.getBigDecimal("unit_cost"));
        int supId = rs.getInt("supplier_id");
        ri.setSupplierId(rs.wasNull() ? null : supId);
        return ri;
    }
}
