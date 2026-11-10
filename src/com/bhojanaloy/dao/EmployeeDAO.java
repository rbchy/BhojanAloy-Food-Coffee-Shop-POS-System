package com.bhojanaloy.dao;

import com.bhojanaloy.config.DBConnection;
import com.bhojanaloy.model.Employee;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {

    public List<Employee> getAllActive() {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT * FROM employees WHERE is_active = TRUE ORDER BY full_name";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Employee getById(int employeeId) {
        String sql = "SELECT * FROM employees WHERE employee_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addEmployee(Employee e) {
        String sql = "INSERT INTO employees (full_name, designation, phone, email, salary, hourly_rate, " +
                     "payment_preference, bank_name, bank_account_number, bank_routing_number, hire_date, is_active) " +
                     "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, e.getFullName());
            ps.setString(2, e.getDesignation());
            ps.setString(3, e.getPhone());
            ps.setString(4, e.getEmail());
            ps.setBigDecimal(5, e.getSalary());
            ps.setBigDecimal(6, e.getHourlyRate());
            ps.setString(7, e.getPaymentPreference());
            ps.setString(8, e.getBankName());
            ps.setString(9, e.getBankAccountNumber());
            ps.setString(10, e.getBankRoutingNumber());
            ps.setDate(11, e.getHireDate() != null ? Date.valueOf(e.getHireDate()) : null);
            ps.setBoolean(12, e.isActive());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean updateEmployee(Employee e) {
        String sql = "UPDATE employees SET full_name=?, designation=?, phone=?, email=?, salary=?, hourly_rate=?, " +
                     "payment_preference=?, bank_name=?, bank_account_number=?, bank_routing_number=?, hire_date=?, is_active=? " +
                     "WHERE employee_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, e.getFullName());
            ps.setString(2, e.getDesignation());
            ps.setString(3, e.getPhone());
            ps.setString(4, e.getEmail());
            ps.setBigDecimal(5, e.getSalary());
            ps.setBigDecimal(6, e.getHourlyRate());
            ps.setString(7, e.getPaymentPreference());
            ps.setString(8, e.getBankName());
            ps.setString(9, e.getBankAccountNumber());
            ps.setString(10, e.getBankRoutingNumber());
            ps.setDate(11, e.getHireDate() != null ? Date.valueOf(e.getHireDate()) : null);
            ps.setBoolean(12, e.isActive());
            ps.setInt(13, e.getEmployeeId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean deleteEmployee(int employeeId) {
        String sql = "UPDATE employees SET is_active = FALSE WHERE employee_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private Employee mapRow(ResultSet rs) throws SQLException {
        Employee e = new Employee();
        e.setEmployeeId(rs.getInt("employee_id"));
        e.setFullName(rs.getString("full_name"));
        e.setDesignation(rs.getString("designation"));
        e.setPhone(rs.getString("phone"));
        e.setEmail(rs.getString("email"));
        e.setSalary(rs.getBigDecimal("salary"));
        e.setHourlyRate(rs.getBigDecimal("hourly_rate"));
        e.setPaymentPreference(rs.getString("payment_preference"));
        e.setBankName(rs.getString("bank_name"));
        e.setBankAccountNumber(rs.getString("bank_account_number"));
        e.setBankRoutingNumber(rs.getString("bank_routing_number"));
        Date hd = rs.getDate("hire_date");
        if (hd != null) e.setHireDate(hd.toLocalDate());
        e.setActive(rs.getBoolean("is_active"));
        return e;
    }
}
