package com.bhojanaloy.dao;

import com.bhojanaloy.config.DBConnection;
import com.bhojanaloy.model.PayrollRun;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Persists processed payroll runs (hours breakdown, pay, payout method/status). */
public class PayrollDAO {

    public int insertPayrollRun(PayrollRun run) {
        String sql = "INSERT INTO payroll_runs (employee_id, period_start, period_end, regular_hours, overtime_hours, " +
                     "weekend_hours, holiday_hours, hourly_rate, gross_pay, deductions, net_pay, payment_method, " +
                     "payment_reference, status, paid_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, run.getEmployeeId());
            ps.setDate(2, Date.valueOf(run.getPeriodStart()));
            ps.setDate(3, Date.valueOf(run.getPeriodEnd()));
            ps.setBigDecimal(4, run.getRegularHours());
            ps.setBigDecimal(5, run.getOvertimeHours());
            ps.setBigDecimal(6, run.getWeekendHours());
            ps.setBigDecimal(7, run.getHolidayHours());
            ps.setBigDecimal(8, run.getHourlyRate());
            ps.setBigDecimal(9, run.getGrossPay());
            ps.setBigDecimal(10, run.getDeductions());
            ps.setBigDecimal(11, run.getNetPay());
            ps.setString(12, run.getPaymentMethod());
            ps.setString(13, run.getPaymentReference());
            ps.setString(14, run.getStatus());
            if ("PAID".equals(run.getStatus())) {
                ps.setTimestamp(15, Timestamp.valueOf(java.time.LocalDateTime.now()));
            } else {
                ps.setNull(15, Types.TIMESTAMP);
            }
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Looks for a payroll run already saved for this exact employee + pay period
     * (regardless of status), so the GUI can offer to overwrite it instead of
     * blindly inserting a duplicate row when a manager recalculates/reprocesses
     * after fixing a mistake. Returns the most recent match, or null if none.
     */
    public PayrollRun findExistingRun(int employeeId, LocalDate periodStart, LocalDate periodEnd) {
        String sql = "SELECT * FROM payroll_runs WHERE employee_id = ? AND period_start = ? AND period_end = ? " +
                     "ORDER BY payroll_id DESC LIMIT 1";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setDate(2, Date.valueOf(periodStart));
            ps.setDate(3, Date.valueOf(periodEnd));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapPlainRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Overwrites an already-saved payroll run in place (used instead of insertPayrollRun to avoid duplicates). */
    public boolean updatePayrollRun(PayrollRun run) {
        String sql = "UPDATE payroll_runs SET regular_hours=?, overtime_hours=?, weekend_hours=?, holiday_hours=?, " +
                     "hourly_rate=?, gross_pay=?, deductions=?, net_pay=?, payment_method=?, payment_reference=?, " +
                     "status=?, paid_at=? WHERE payroll_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, run.getRegularHours());
            ps.setBigDecimal(2, run.getOvertimeHours());
            ps.setBigDecimal(3, run.getWeekendHours());
            ps.setBigDecimal(4, run.getHolidayHours());
            ps.setBigDecimal(5, run.getHourlyRate());
            ps.setBigDecimal(6, run.getGrossPay());
            ps.setBigDecimal(7, run.getDeductions());
            ps.setBigDecimal(8, run.getNetPay());
            ps.setString(9, run.getPaymentMethod());
            ps.setString(10, run.getPaymentReference());
            ps.setString(11, run.getStatus());
            if ("PAID".equals(run.getStatus())) {
                ps.setTimestamp(12, Timestamp.valueOf(java.time.LocalDateTime.now()));
            } else {
                ps.setNull(12, Types.TIMESTAMP);
            }
            ps.setInt(13, run.getPayrollId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Removes a payroll history row entirely — e.g. to clean up an accidental duplicate. */
    public boolean deletePayrollRun(int payrollId) {
        String sql = "DELETE FROM payroll_runs WHERE payroll_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, payrollId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean markPaid(int payrollId, String paymentMethod, String paymentReference) {
        String sql = "UPDATE payroll_runs SET status='PAID', payment_method=?, payment_reference=?, paid_at=NOW() WHERE payroll_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, paymentMethod);
            ps.setString(2, paymentReference);
            ps.setInt(3, payrollId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<PayrollRun> getHistory() {
        List<PayrollRun> list = new ArrayList<>();
        String sql = "SELECT * FROM v_payroll_history";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<PayrollRun> getHistoryForEmployee(int employeeId) {
        List<PayrollRun> list = new ArrayList<>();
        String sql = "SELECT * FROM v_payroll_history WHERE employee_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Maps a row read directly from payroll_runs (no employees join, so no employeeName). */
    private PayrollRun mapPlainRow(ResultSet rs) throws SQLException {
        PayrollRun run = new PayrollRun();
        run.setPayrollId(rs.getInt("payroll_id"));
        run.setEmployeeId(rs.getInt("employee_id"));
        run.setPeriodStart(rs.getDate("period_start").toLocalDate());
        run.setPeriodEnd(rs.getDate("period_end").toLocalDate());
        run.setRegularHours(rs.getBigDecimal("regular_hours"));
        run.setOvertimeHours(rs.getBigDecimal("overtime_hours"));
        run.setWeekendHours(rs.getBigDecimal("weekend_hours"));
        run.setHolidayHours(rs.getBigDecimal("holiday_hours"));
        run.setHourlyRate(rs.getBigDecimal("hourly_rate"));
        run.setGrossPay(rs.getBigDecimal("gross_pay"));
        run.setDeductions(rs.getBigDecimal("deductions"));
        run.setNetPay(rs.getBigDecimal("net_pay"));
        run.setPaymentMethod(rs.getString("payment_method"));
        run.setPaymentReference(rs.getString("payment_reference"));
        run.setStatus(rs.getString("status"));
        Timestamp gen = rs.getTimestamp("generated_at");
        if (gen != null) run.setGeneratedAt(gen.toLocalDateTime());
        Timestamp paid = rs.getTimestamp("paid_at");
        if (paid != null) run.setPaidAt(paid.toLocalDateTime());
        return run;
    }

    private PayrollRun mapRow(ResultSet rs) throws SQLException {
        PayrollRun run = new PayrollRun();
        run.setPayrollId(rs.getInt("payroll_id"));
        run.setEmployeeId(rs.getInt("employee_id"));
        run.setEmployeeName(rs.getString("full_name"));
        run.setPeriodStart(rs.getDate("period_start").toLocalDate());
        run.setPeriodEnd(rs.getDate("period_end").toLocalDate());
        run.setRegularHours(rs.getBigDecimal("regular_hours"));
        run.setOvertimeHours(rs.getBigDecimal("overtime_hours"));
        run.setWeekendHours(rs.getBigDecimal("weekend_hours"));
        run.setHolidayHours(rs.getBigDecimal("holiday_hours"));
        run.setHourlyRate(rs.getBigDecimal("hourly_rate"));
        run.setGrossPay(rs.getBigDecimal("gross_pay"));
        run.setDeductions(rs.getBigDecimal("deductions"));
        run.setNetPay(rs.getBigDecimal("net_pay"));
        run.setPaymentMethod(rs.getString("payment_method"));
        run.setPaymentReference(rs.getString("payment_reference"));
        run.setStatus(rs.getString("status"));
        Timestamp gen = rs.getTimestamp("generated_at");
        if (gen != null) run.setGeneratedAt(gen.toLocalDateTime());
        Timestamp paid = rs.getTimestamp("paid_at");
        if (paid != null) run.setPaidAt(paid.toLocalDateTime());
        return run;
    }
}