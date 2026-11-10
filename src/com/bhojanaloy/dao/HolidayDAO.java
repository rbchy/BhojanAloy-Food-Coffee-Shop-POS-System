package com.bhojanaloy.dao;

import com.bhojanaloy.config.DBConnection;
import com.bhojanaloy.model.Holiday;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Company holiday calendar — drives the holiday pay multiplier in PayrollService. */
public class HolidayDAO {

    public List<Holiday> getAll() {
        List<Holiday> list = new ArrayList<>();
        String sql = "SELECT * FROM holidays ORDER BY holiday_date";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** All holiday dates within [start, end], as a Set for quick lookup during payroll calculation. */
    public Set<LocalDate> getHolidayDatesInRange(LocalDate start, LocalDate end) {
        Set<LocalDate> dates = new HashSet<>();
        String sql = "SELECT holiday_date FROM holidays WHERE holiday_date BETWEEN ? AND ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) dates.add(rs.getDate("holiday_date").toLocalDate());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dates;
    }

    public boolean addHoliday(LocalDate date, String name) {
        String sql = "INSERT INTO holidays (holiday_date, holiday_name) VALUES (?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            ps.setString(2, name);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteHoliday(int holidayId) {
        String sql = "DELETE FROM holidays WHERE holiday_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, holidayId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Holiday mapRow(ResultSet rs) throws SQLException {
        Holiday h = new Holiday();
        h.setHolidayId(rs.getInt("holiday_id"));
        h.setHolidayDate(rs.getDate("holiday_date").toLocalDate());
        h.setHolidayName(rs.getString("holiday_name"));
        return h;
    }
}
