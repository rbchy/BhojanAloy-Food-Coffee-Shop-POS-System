package com.bhojanaloy.dao;

import com.bhojanaloy.config.DBConnection;
import com.bhojanaloy.model.Payment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {

    /** Inserts a standalone payment record (used when NOT going through OrderDAO.placeOrder's transaction). */
    public boolean recordPayment(Payment payment) {
        String sql = "INSERT INTO payments (order_id, method, provider, reference_number, amount, status) VALUES (?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, payment.getOrderId());
            ps.setString(2, payment.getMethod());
            ps.setString(3, payment.getProvider());
            ps.setString(4, payment.getReferenceNumber());
            ps.setBigDecimal(5, payment.getAmount());
            ps.setString(6, payment.getStatus());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Payment> getPaymentsForOrder(int orderId) {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE order_id = ? ORDER BY processed_at DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Payment mapRow(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setPaymentId(rs.getInt("payment_id"));
        p.setOrderId(rs.getInt("order_id"));
        p.setMethod(rs.getString("method"));
        p.setProvider(rs.getString("provider"));
        p.setReferenceNumber(rs.getString("reference_number"));
        p.setAmount(rs.getBigDecimal("amount"));
        p.setStatus(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("processed_at");
        if (ts != null) p.setProcessedAt(ts.toLocalDateTime());
        return p;
    }
}
