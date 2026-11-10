package com.bhojanaloy.dao;

import com.bhojanaloy.config.DBConnection;
import com.bhojanaloy.model.Delivery;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages DELIVERY-type orders being fulfilled by an in-house rider or a
 * third-party agency: assigning a rider/agency, and tracking pickup from
 * the shop through drop-off at the customer's address.
 */
public class DeliveryDAO {

    /** Active (not yet delivered/cancelled) deliveries, for the Delivery Management screen. */
    public List<Delivery> getActiveDeliveries() {
        List<Delivery> list = new ArrayList<>();
        String sql = "SELECT * FROM v_active_deliveries";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Assigns (or reassigns) a rider/agency to a delivery and marks it ASSIGNED. */
    public boolean assignRider(int deliveryId, String agencyName, String riderName, String riderPhone) {
        String sql = "UPDATE deliveries SET agency_name=?, rider_name=?, rider_phone=?, status='ASSIGNED', assigned_at=NOW() WHERE delivery_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, agencyName);
            ps.setString(2, riderName);
            ps.setString(3, riderPhone);
            ps.setInt(4, deliveryId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Marks a delivery as picked up from the shop by the rider/agency. */
    public boolean markPickedUp(int deliveryId) {
        return updateStatusWithTimestamp(deliveryId, "PICKED_UP", "picked_up_at");
    }

    /** Marks a delivery as successfully delivered to the customer's address. */
    public boolean markDelivered(int deliveryId) {
        return updateStatusWithTimestamp(deliveryId, "DELIVERED", "delivered_at");
    }

    public boolean cancelDelivery(int deliveryId, String reason) {
        String sql = "UPDATE deliveries SET status='CANCELLED', notes=? WHERE delivery_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, reason);
            ps.setInt(2, deliveryId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean updateStatusWithTimestamp(int deliveryId, String status, String timestampColumn) {
        String sql = "UPDATE deliveries SET status=?, " + timestampColumn + "=NOW() WHERE delivery_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, deliveryId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Delivery mapRow(ResultSet rs) throws SQLException {
        Delivery d = new Delivery();
        d.setDeliveryId(rs.getInt("delivery_id"));
        d.setOrderId(rs.getInt("order_id"));
        d.setOrderNumber(rs.getString("order_number"));
        d.setCustomerName(rs.getString("customer_name"));
        d.setCustomerPhone(rs.getString("customer_phone"));
        d.setCustomerAddress(rs.getString("customer_address"));
        d.setTotalAmount(rs.getBigDecimal("total_amount"));
        d.setAgencyName(rs.getString("agency_name"));
        d.setRiderName(rs.getString("rider_name"));
        d.setRiderPhone(rs.getString("rider_phone"));
        d.setStatus(rs.getString("status"));
        Timestamp a = rs.getTimestamp("assigned_at");
        if (a != null) d.setAssignedAt(a.toLocalDateTime());
        Timestamp p = rs.getTimestamp("picked_up_at");
        if (p != null) d.setPickedUpAt(p.toLocalDateTime());
        Timestamp del = rs.getTimestamp("delivered_at");
        if (del != null) d.setDeliveredAt(del.toLocalDateTime());
        d.setNotes(rs.getString("notes"));
        return d;
    }
}
