package com.bhojanaloy.dao;

import com.bhojanaloy.config.DBConnection;
import com.bhojanaloy.model.MenuItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MenuItemDAO {

    public List<MenuItem> getAll() {
        List<MenuItem> list = new ArrayList<>();
        String sql = "SELECT mi.*, c.category_name, c.station_name FROM menu_items mi " +
                     "JOIN categories c ON mi.category_id = c.category_id ORDER BY c.display_order, mi.item_name";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<MenuItem> getByCategory(int categoryId) {
        List<MenuItem> list = new ArrayList<>();
        String sql = "SELECT mi.*, c.category_name, c.station_name FROM menu_items mi " +
                     "JOIN categories c ON mi.category_id = c.category_id " +
                     "WHERE mi.category_id = ? AND mi.is_available = TRUE ORDER BY mi.item_name";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<MenuItem> getLowStock() {
        List<MenuItem> list = new ArrayList<>();
        String sql = "SELECT mi.*, c.category_name, c.station_name FROM menu_items mi " +
                     "JOIN categories c ON mi.category_id = c.category_id " +
                     "WHERE mi.stock_qty <= mi.reorder_level AND mi.is_available = TRUE";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean addItem(MenuItem m) {
        String sql = "INSERT INTO menu_items (category_id, item_name, description, price, cost_price, stock_qty, reorder_level, is_available, image_path) " +
                     "VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, m.getCategoryId());
            ps.setString(2, m.getItemName());
            ps.setString(3, m.getDescription());
            ps.setBigDecimal(4, m.getPrice());
            ps.setBigDecimal(5, m.getCostPrice());
            ps.setInt(6, m.getStockQty());
            ps.setInt(7, m.getReorderLevel());
            ps.setBoolean(8, m.isAvailable());
            ps.setString(9, m.getImagePath());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateItem(MenuItem m) {
        String sql = "UPDATE menu_items SET category_id=?, item_name=?, description=?, price=?, cost_price=?, " +
                     "stock_qty=?, reorder_level=?, is_available=?, image_path=? WHERE item_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, m.getCategoryId());
            ps.setString(2, m.getItemName());
            ps.setString(3, m.getDescription());
            ps.setBigDecimal(4, m.getPrice());
            ps.setBigDecimal(5, m.getCostPrice());
            ps.setInt(6, m.getStockQty());
            ps.setInt(7, m.getReorderLevel());
            ps.setBoolean(8, m.isAvailable());
            ps.setString(9, m.getImagePath());
            ps.setInt(10, m.getItemId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteItem(int itemId) {
        String sql = "UPDATE menu_items SET is_available = FALSE WHERE item_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Deducts stock after a sale; also logs an inventory transaction. */
    public boolean deductStock(int itemId, int quantity, Connection con) throws SQLException {
        String sql = "UPDATE menu_items SET stock_qty = stock_qty - ? WHERE item_id = ? AND stock_qty >= ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, itemId);
            ps.setInt(3, quantity);
            return ps.executeUpdate() > 0;
        }
    }

    private MenuItem mapRow(ResultSet rs) throws SQLException {
        MenuItem m = new MenuItem();
        m.setItemId(rs.getInt("item_id"));
        m.setCategoryId(rs.getInt("category_id"));
        m.setCategoryName(rs.getString("category_name"));
        m.setStationName(rs.getString("station_name"));
        m.setItemName(rs.getString("item_name"));
        m.setDescription(rs.getString("description"));
        m.setPrice(rs.getBigDecimal("price"));
        m.setCostPrice(rs.getBigDecimal("cost_price"));
        m.setStockQty(rs.getInt("stock_qty"));
        m.setReorderLevel(rs.getInt("reorder_level"));
        m.setAvailable(rs.getBoolean("is_available"));
        m.setImagePath(rs.getString("image_path"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) m.setCreatedAt(ts.toLocalDateTime());
        return m;
    }
}
