package com.bhojanaloy.dao;

import com.bhojanaloy.config.DBConnection;
import com.bhojanaloy.model.User;
import com.bhojanaloy.util.PasswordUtil;

import java.sql.*;

public class UserDAO {

    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Authenticates a username/password pair. Returns the User on success, null otherwise. */
    public User authenticate(String username, String plainPassword) {
        User user = findByUsername(username);
        if (user == null || !user.isActive()) return null;
        if (PasswordUtil.verify(plainPassword, user.getSalt(), user.getPasswordHash())) {
            updateLastLogin(user.getUserId());
            return user;
        }
        return null;
    }

    public boolean createUser(String username, String plainPassword, String fullName, String role, Integer employeeId) {
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hash(plainPassword, salt);
        String sql = "INSERT INTO users (username, password_hash, salt, full_name, role, employee_id) VALUES (?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, hash);
            ps.setString(3, salt);
            ps.setString(4, fullName);
            ps.setString(5, role);
            if (employeeId != null) ps.setInt(6, employeeId); else ps.setNull(6, Types.INTEGER);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean changePassword(int userId, String newPlainPassword) {
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hash(newPlainPassword, salt);
        String sql = "UPDATE users SET password_hash=?, salt=? WHERE user_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setString(2, salt);
            ps.setInt(3, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void updateLastLogin(int userId) {
        String sql = "UPDATE users SET last_login = NOW() WHERE user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setSalt(rs.getString("salt"));
        u.setFullName(rs.getString("full_name"));
        u.setRole(rs.getString("role"));
        int empId = rs.getInt("employee_id");
        u.setEmployeeId(rs.wasNull() ? null : empId);
        u.setActive(rs.getBoolean("is_active"));
        Timestamp lastLogin = rs.getTimestamp("last_login");
        if (lastLogin != null) u.setLastLogin(lastLogin.toLocalDateTime());
        return u;
    }
}
