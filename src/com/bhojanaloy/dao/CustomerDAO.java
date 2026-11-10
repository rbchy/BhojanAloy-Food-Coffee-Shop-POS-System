package com.bhojanaloy.dao;

import com.bhojanaloy.config.DBConnection;
import com.bhojanaloy.model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** CRUD + loyalty-point bookkeeping for registered customers. */
public class CustomerDAO {

    // The exact SQL error message from the most recent failed call, so the GUI can show
    // something more useful than "database error" (e.g. missing customers table because
    // migration_v4_loyalty.sql hasn't been run yet, or a duplicate phone number).
    private String lastError;

    public String getLastError() { return lastError; }

    public Customer findByPhone(String phone) {
        String sql = "SELECT * FROM customers WHERE phone = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            lastError = e.getMessage();
            e.printStackTrace();
        }
        return null;
    }

    /** Inserts a brand-new loyalty customer and returns their generated ID, or -1 on failure. */
    public int createCustomer(Customer c) {
        lastError = null;
        String sql = "INSERT INTO customers (full_name, phone, email, loyalty_points) VALUES (?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getFullName());
            ps.setString(2, c.getPhone());
            ps.setString(3, c.getEmail());
            ps.setInt(4, c.getLoyaltyPoints());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            lastError = e.getMessage();
            e.printStackTrace();
        }
        return -1;
    }

    /** Overwrites a customer's point balance (used after a redemption resets it to 0, or a normal +1 earn). */
    public boolean updateLoyaltyPoints(int customerId, int newPoints) {
        String sql = "UPDATE customers SET loyalty_points = ? WHERE customer_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, newPoints);
            ps.setInt(2, customerId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** All registered customers who have an email on file — used for "new menu item" announcements. */
    public List<Customer> getAllWithEmail() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM customers WHERE email IS NOT NULL AND email <> ''";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Customer> getAll() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY full_name";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setCustomerId(rs.getInt("customer_id"));
        c.setFullName(rs.getString("full_name"));
        c.setPhone(rs.getString("phone"));
        c.setEmail(rs.getString("email"));
        c.setLoyaltyPoints(rs.getInt("loyalty_points"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) c.setCreatedAt(ts.toLocalDateTime());
        return c;
    }
}