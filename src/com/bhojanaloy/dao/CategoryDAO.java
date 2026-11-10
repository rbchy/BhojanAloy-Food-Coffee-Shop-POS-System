package com.bhojanaloy.dao;

import com.bhojanaloy.config.DBConnection;
import com.bhojanaloy.model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    public List<Category> getAllActive() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM categories WHERE is_active = TRUE ORDER BY display_order";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean addCategory(Category c) {
        String sql = "INSERT INTO categories (category_name, description, display_order, station_name, is_active) VALUES (?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getCategoryName());
            ps.setString(2, c.getDescription());
            ps.setInt(3, c.getDisplayOrder());
            ps.setString(4, c.getStationName() != null ? c.getStationName() : "General");
            ps.setBoolean(5, c.isActive());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateCategory(Category c) {
        String sql = "UPDATE categories SET category_name=?, description=?, display_order=?, station_name=?, is_active=? WHERE category_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getCategoryName());
            ps.setString(2, c.getDescription());
            ps.setInt(3, c.getDisplayOrder());
            ps.setString(4, c.getStationName() != null ? c.getStationName() : "General");
            ps.setBoolean(5, c.isActive());
            ps.setInt(6, c.getCategoryId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Distinct list of station names currently in use, for populating dropdowns/tabs. */
    public java.util.List<String> getDistinctStations() {
        java.util.List<String> stations = new ArrayList<>();
        String sql = "SELECT DISTINCT station_name FROM categories WHERE is_active = TRUE ORDER BY station_name";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) stations.add(rs.getString("station_name"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stations;
    }

    public boolean deleteCategory(int categoryId) {
        String sql = "UPDATE categories SET is_active = FALSE WHERE category_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Category mapRow(ResultSet rs) throws SQLException {
        return new Category(
                rs.getInt("category_id"),
                rs.getString("category_name"),
                rs.getString("description"),
                rs.getInt("display_order"),
                rs.getString("station_name"),
                rs.getBoolean("is_active"));
    }
}
