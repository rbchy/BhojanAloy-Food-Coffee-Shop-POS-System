package com.bhojanaloy.dao;

import com.bhojanaloy.config.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Powers the Reports panel: daily sales, item-wise sales, category sales,
 * employee performance, and low stock alerts. Each method returns a list of
 * LinkedHashMap rows so it can be rendered generically in a JTable or exported.
 */
public class ReportDAO {

    public List<Map<String, Object>> getDailySummary(String startDate, String endDate) {
        String sql = "SELECT * FROM v_daily_summary WHERE sale_date BETWEEN ? AND ? ORDER BY sale_date DESC";
        return runQuery(sql, startDate, endDate);
    }

    public List<Map<String, Object>> getItemSales() {
        String sql = "SELECT * FROM v_item_sales ORDER BY total_revenue DESC";
        return runQuery(sql);
    }

    public List<Map<String, Object>> getCategorySales() {
        String sql = "SELECT category_name, SUM(total_qty_sold) AS qty_sold, SUM(total_revenue) AS revenue " +
                     "FROM v_item_sales GROUP BY category_name ORDER BY revenue DESC";
        return runQuery(sql);
    }

    public List<Map<String, Object>> getEmployeePerformance() {
        String sql = "SELECT * FROM v_employee_performance ORDER BY total_sales DESC";
        return runQuery(sql);
    }

    public List<Map<String, Object>> getLowStockItems() {
        String sql = "SELECT * FROM v_low_stock_items";
        return runQuery(sql);
    }

    public List<Map<String, Object>> getLowStockIngredients() {
        String sql = "SELECT * FROM v_low_stock_ingredients";
        return runQuery(sql);
    }

    public Map<String, Object> getTodaySummary() {
        String sql = "SELECT COUNT(order_id) AS total_orders, COALESCE(SUM(total_amount),0) AS total_sales, " +
                     "COALESCE(SUM(discount),0) AS total_discount, COALESCE(SUM(tax_amount),0) AS total_tax " +
                     "FROM orders WHERE DATE(order_date) = CURDATE() AND status = 'COMPLETED'";
        List<Map<String, Object>> rows = runQuery(sql);
        return rows.isEmpty() ? new LinkedHashMap<>() : rows.get(0);
    }

    private List<Map<String, Object>> runQuery(String sql, String... params) {
        List<Map<String, Object>> results = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setString(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int cols = meta.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= cols; i++) {
                        row.put(meta.getColumnLabel(i), rs.getObject(i));
                    }
                    results.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }
}
