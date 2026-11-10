package com.bhojanaloy.dao;

import com.bhojanaloy.config.DBConnection;
import com.bhojanaloy.model.Order;
import com.bhojanaloy.model.OrderItem;
import com.bhojanaloy.model.Payment;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OrderDAO {

    private final MenuItemDAO menuItemDAO = new MenuItemDAO();

    // The exact SQL error message from the most recent failed placeOrder() call, so the
    // GUI can show the caller something more useful than "checkout failed" (e.g. a
    // missing column because a migration script hasn't been run yet, or a stock issue).
    private String lastError;

    public String getLastError() { return lastError; }

    /** Convenience overload for CASH (or already-recorded) orders with no separate gateway payment row. */
    public int placeOrder(Order order) {
        return placeOrder(order, null);
    }

    /**
     * Persists a completed order + its line items inside a single transaction,
     * deducts stock for each item sold, logs an inventory transaction, records
     * the (optional) payment-gateway transaction, and — for DELIVERY orders —
     * creates an unassigned delivery record ready for dispatch.
     * Returns the generated order_id, or -1 on failure.
     */
    public int placeOrder(Order order, Payment payment) {
        lastError = null;
        String orderSql = "INSERT INTO orders (order_number, employee_id, customer_id, subtotal, discount, tax_amount, total_amount, " +
                           "payment_method, order_type, order_source, customer_name, customer_phone, customer_address, loyalty_reward_item, status) " +
                           "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        String itemSql = "INSERT INTO order_items (order_id, item_id, quantity, unit_price, line_total, station_name, prep_status) " +
                          "VALUES (?,?,?,?,?,?,?)";
        String invSql = "INSERT INTO inventory_transactions (item_id, txn_type, quantity, reference_id, notes) VALUES (?,?,?,?,?)";
        String paymentSql = "INSERT INTO payments (order_id, method, provider, reference_number, amount, status) VALUES (?,?,?,?,?,?)";
        String deliverySql = "INSERT INTO deliveries (order_id, status) VALUES (?, 'UNASSIGNED')";

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            int orderId;
            try (PreparedStatement ps = con.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, order.getOrderNumber());
                ps.setInt(2, order.getEmployeeId());
                if (order.getCustomerId() != null) ps.setInt(3, order.getCustomerId());
                else ps.setNull(3, Types.INTEGER);
                ps.setBigDecimal(4, order.getSubtotal());
                ps.setBigDecimal(5, order.getDiscount());
                ps.setBigDecimal(6, order.getTaxAmount());
                ps.setBigDecimal(7, order.getTotalAmount());
                ps.setString(8, order.getPaymentMethod());
                ps.setString(9, order.getOrderType());
                ps.setString(10, order.getOrderSource());
                ps.setString(11, order.getCustomerName());
                ps.setString(12, order.getCustomerPhone());
                ps.setString(13, order.getCustomerAddress());
                ps.setString(14, order.getLoyaltyRewardItem());
                ps.setString(15, order.getStatus());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Order insert failed, no ID generated.");
                    orderId = keys.getInt(1);
                }
            }

            try (PreparedStatement ps = con.prepareStatement(itemSql);
                 PreparedStatement invPs = con.prepareStatement(invSql)) {
                for (OrderItem oi : order.getItems()) {
                    ps.setInt(1, orderId);
                    ps.setInt(2, oi.getItemId());
                    ps.setInt(3, oi.getQuantity());
                    ps.setBigDecimal(4, oi.getUnitPrice());
                    ps.setBigDecimal(5, oi.getLineTotal());
                    ps.setString(6, oi.getStationName() != null ? oi.getStationName() : "General");
                    ps.setString(7, "PENDING");
                    ps.addBatch();

                    boolean deducted = menuItemDAO.deductStock(oi.getItemId(), oi.getQuantity(), con);
                    if (!deducted) throw new SQLException("Insufficient stock for item: " + oi.getItemName());

                    invPs.setInt(1, oi.getItemId());
                    invPs.setString(2, "SALE_OUT");
                    invPs.setBigDecimal(3, java.math.BigDecimal.valueOf(oi.getQuantity()));
                    invPs.setInt(4, orderId);
                    invPs.setString(5, "Sold via order " + order.getOrderNumber());
                    invPs.addBatch();
                }
                ps.executeBatch();
                invPs.executeBatch();
            }

            if (payment != null) {
                try (PreparedStatement ps = con.prepareStatement(paymentSql)) {
                    ps.setInt(1, orderId);
                    ps.setString(2, payment.getMethod());
                    ps.setString(3, payment.getProvider());
                    ps.setString(4, payment.getReferenceNumber());
                    ps.setBigDecimal(5, payment.getAmount());
                    ps.setString(6, payment.getStatus());
                    ps.executeUpdate();
                }
            }

            if (order.isDelivery()) {
                try (PreparedStatement ps = con.prepareStatement(deliverySql)) {
                    ps.setInt(1, orderId);
                    ps.executeUpdate();
                }
            }

            con.commit();
            return orderId;
        } catch (SQLException e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            lastError = e.getMessage();
            e.printStackTrace();
            return -1;
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); con.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    public List<Order> getOrdersBetween(LocalDateTime start, LocalDateTime end) {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.*, e.full_name FROM orders o JOIN employees e ON o.employee_id = e.employee_id " +
                     "WHERE o.order_date BETWEEN ? AND ? ORDER BY o.order_date DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(start));
            ps.setTimestamp(2, Timestamp.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Order> getRecentOrders(int limit) {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.*, e.full_name FROM orders o JOIN employees e ON o.employee_id = e.employee_id " +
                     "ORDER BY o.order_date DESC LIMIT ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Full order detail (with items) for printing a receipt after checkout. */
    public Order getOrderById(int orderId) {
        String sql = "SELECT o.*, e.full_name FROM orders o JOIN employees e ON o.employee_id = e.employee_id WHERE o.order_id = ?";
        Order order = null;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) order = mapRow(rs);
            }
            if (order != null) {
                String itemSql = "SELECT oi.*, mi.item_name FROM order_items oi JOIN menu_items mi ON oi.item_id = mi.item_id WHERE oi.order_id = ?";
                try (PreparedStatement ips = con.prepareStatement(itemSql)) {
                    ips.setInt(1, orderId);
                    try (ResultSet rs = ips.executeQuery()) {
                        List<OrderItem> items = new ArrayList<>();
                        while (rs.next()) {
                            OrderItem oi = new OrderItem(rs.getInt("item_id"), rs.getString("item_name"),
                                    rs.getInt("quantity"), rs.getBigDecimal("unit_price"), rs.getString("station_name"));
                            oi.setOrderItemId(rs.getInt("order_item_id"));
                            oi.setOrderId(orderId);
                            oi.setLineTotal(rs.getBigDecimal("line_total"));
                            oi.setPrepStatus(rs.getString("prep_status"));
                            items.add(oi);
                        }
                        order.setItems(items);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return order;
    }

    /**
     * Pending/in-progress order items for the Kitchen/Station Display, optionally
     * filtered to a single station (pass null for all stations).
     */
    public List<Map<String, Object>> getStationQueue(String stationFilter) {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "SELECT * FROM v_station_queue" + (stationFilter != null ? " WHERE station_name = ?" : "") +
                     " ORDER BY order_date";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (stationFilter != null) ps.setString(1, stationFilter);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int cols = meta.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= cols; i++) row.put(meta.getColumnLabel(i), rs.getObject(i));
                    results.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    /** Advances a single order line item's prep status (PENDING -> PREPARING -> READY -> SERVED). */
    public boolean updateItemPrepStatus(int orderItemId, String newStatus) {
        String sql = "UPDATE order_items SET prep_status = ? WHERE order_item_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, orderItemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static final java.time.format.DateTimeFormatter ORDER_NO_TIME =
            java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmssSSS");
    private static long lastOrderMillis;
    private static int orderSeq;

    /**
     * e.g. ORD-20261109-213605123-07 (25 characters; the column is VARCHAR(30) UNIQUE).
     * The old "ORD-date-random 4 digits" scheme had about a 1-in-2 chance of a duplicate once a
     * shop took ~120 orders in a day (birthday problem), which made that checkout fail.
     * Millisecond time plus a 2-digit sequence is unique within the app: if 100 numbers are
     * asked for in the same millisecond, the clock part is moved on by 1 ms instead of repeating.
     */
    public String generateOrderNumber() {
        long now = System.currentTimeMillis();
        long millis;
        int seq;
        synchronized (OrderDAO.class) {
            if (now > lastOrderMillis) {
                lastOrderMillis = now;
                orderSeq = 0;
            } else if (++orderSeq > 99) {
                lastOrderMillis++;
                orderSeq = 0;
            }
            millis = lastOrderMillis;
            seq = orderSeq;
        }
        LocalDateTime time = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(millis), java.time.ZoneId.systemDefault());
        return "ORD-" + time.format(ORDER_NO_TIME) + "-" + String.format("%02d", seq);
    }

    private Order mapRow(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setOrderId(rs.getInt("order_id"));
        o.setOrderNumber(rs.getString("order_number"));
        o.setEmployeeId(rs.getInt("employee_id"));
        o.setEmployeeName(rs.getString("full_name"));
        Timestamp ts = rs.getTimestamp("order_date");
        if (ts != null) o.setOrderDate(ts.toLocalDateTime());
        o.setSubtotal(rs.getBigDecimal("subtotal"));
        o.setDiscount(rs.getBigDecimal("discount"));
        o.setTaxAmount(rs.getBigDecimal("tax_amount"));
        o.setTotalAmount(rs.getBigDecimal("total_amount"));
        o.setPaymentMethod(rs.getString("payment_method"));
        o.setOrderType(rs.getString("order_type"));
        o.setOrderSource(rs.getString("order_source"));
        o.setCustomerName(rs.getString("customer_name"));
        o.setCustomerPhone(rs.getString("customer_phone"));
        o.setCustomerAddress(rs.getString("customer_address"));
        int custId = rs.getInt("customer_id");
        if (!rs.wasNull()) o.setCustomerId(custId);
        o.setLoyaltyRewardItem(rs.getString("loyalty_reward_item"));
        o.setStatus(rs.getString("status"));
        return o;
    }
}