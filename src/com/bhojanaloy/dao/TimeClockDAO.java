package com.bhojanaloy.dao;

import com.bhojanaloy.config.DBConnection;
import com.bhojanaloy.model.TimeClock;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Employee clock-in / clock-out tracking. */
public class TimeClockDAO {

    /** Returns the employee's currently open shift (clock_out IS NULL), or null if not clocked in. */
    public TimeClock getOpenShift(int employeeId) {
        String sql = "SELECT tc.*, e.full_name FROM time_clock tc JOIN employees e ON tc.employee_id = e.employee_id " +
                     "WHERE tc.employee_id = ? AND tc.clock_out IS NULL ORDER BY tc.clock_in DESC LIMIT 1";
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

    public boolean clockIn(int employeeId) {
        if (getOpenShift(employeeId) != null) return false; // already clocked in
        String sql = "INSERT INTO time_clock (employee_id, work_date, clock_in) VALUES (?, CURDATE(), NOW())";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Clocks out the employee's open shift and computes total_hours. */
    public boolean clockOut(int employeeId) {
        TimeClock open = getOpenShift(employeeId);
        if (open == null) return false;

        LocalDateTime now = LocalDateTime.now();
        BigDecimal hours = BigDecimal.valueOf(Duration.between(open.getClockIn(), now).toMinutes())
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

        String sql = "UPDATE time_clock SET clock_out = ?, total_hours = ? WHERE clock_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(now));
            ps.setBigDecimal(2, hours);
            ps.setInt(3, open.getClockId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<TimeClock> getAllCurrentlyClockedIn() {
        List<TimeClock> list = new ArrayList<>();
        String sql = "SELECT tc.*, e.full_name FROM time_clock tc JOIN employees e ON tc.employee_id = e.employee_id " +
                     "WHERE tc.clock_out IS NULL ORDER BY tc.clock_in";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Completed (clocked-out) shifts for one employee within a date range — used by payroll processing. */
    public List<TimeClock> getShiftsForPeriod(int employeeId, java.time.LocalDate start, java.time.LocalDate end) {
        List<TimeClock> list = new ArrayList<>();
        String sql = "SELECT tc.*, e.full_name FROM time_clock tc JOIN employees e ON tc.employee_id = e.employee_id " +
                     "WHERE tc.employee_id = ? AND tc.work_date BETWEEN ? AND ? AND tc.clock_out IS NOT NULL " +
                     "ORDER BY tc.work_date";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setDate(2, Date.valueOf(start));
            ps.setDate(3, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<TimeClock> getRecentHistory(int employeeId, int limit) {
        List<TimeClock> list = new ArrayList<>();
        String sql = "SELECT tc.*, e.full_name FROM time_clock tc JOIN employees e ON tc.employee_id = e.employee_id " +
                     "WHERE tc.employee_id = ? ORDER BY tc.clock_in DESC LIMIT ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private TimeClock mapRow(ResultSet rs) throws SQLException {
        TimeClock tc = new TimeClock();
        tc.setClockId(rs.getInt("clock_id"));
        tc.setEmployeeId(rs.getInt("employee_id"));
        tc.setEmployeeName(rs.getString("full_name"));
        Date wd = rs.getDate("work_date");
        if (wd != null) tc.setWorkDate(wd.toLocalDate());
        Timestamp ci = rs.getTimestamp("clock_in");
        if (ci != null) tc.setClockIn(ci.toLocalDateTime());
        Timestamp co = rs.getTimestamp("clock_out");
        if (co != null) tc.setClockOut(co.toLocalDateTime());
        tc.setTotalHours(rs.getBigDecimal("total_hours"));
        tc.setNotes(rs.getString("notes"));
        return tc;
    }
}
