package com.bhojanaloy.gui;

import com.bhojanaloy.dao.ReportDAO;
import com.bhojanaloy.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Reporting dashboard: daily summary, item/category sales, employee performance, low stock. */
public class ReportsPanel extends JPanel {

    private final ReportDAO reportDAO = new ReportDAO();
    private final JLabel todayOrdersLabel = new JLabel("0");
    private final JLabel todaySalesLabel = new JLabel("$0.00");
    private final JLabel todayTaxLabel = new JLabel("$0.00");
    private final JTabbedPane tabs = new JTabbedPane();

    private DefaultTableModel dailyModel, itemModel, categoryModel, employeeModel, lowStockModel;
    private JTable currentExportTable;

    public ReportsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.CREAM);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        add(UITheme.heading("Reports & Analytics"), BorderLayout.NORTH);
        add(buildSummaryStrip(), BorderLayout.WEST);
        add(buildTabs(), BorderLayout.CENTER);
        add(buildExportBar(), BorderLayout.SOUTH);

        refreshAll();
    }

    private JPanel buildSummaryStrip() {
        JPanel strip = new JPanel();
        strip.setLayout(new BoxLayout(strip, BoxLayout.Y_AXIS));
        strip.setPreferredSize(new Dimension(220, 0));
        strip.setBackground(UITheme.WHITE);
        strip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.CREAM_DARKER, 1), new EmptyBorder(15, 15, 15, 15)));

        strip.add(UITheme.heading("Today"));
        strip.add(Box.createVerticalStrut(10));
        strip.add(summaryRow("Orders:", todayOrdersLabel));
        strip.add(summaryRow("Sales:", todaySalesLabel));
        strip.add(summaryRow("Tax Collected:", todayTaxLabel));
        strip.add(Box.createVerticalGlue());
        return strip;
    }

    private JPanel summaryRow(String label, JLabel valueLabel) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(UITheme.FONT_LABEL_B);
        valueLabel.setFont(UITheme.FONT_LABEL);
        valueLabel.setForeground(UITheme.ACCENT_ORANGE);
        row.add(l, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.EAST);
        row.setMaximumSize(new Dimension(200, 30));
        return row;
    }

    private JTabbedPane buildTabs() {
        dailyModel = new DefaultTableModel(new Object[]{"Date", "Orders", "Sales", "Discount", "Tax"}, 0);
        itemModel = new DefaultTableModel(new Object[]{"Item", "Category", "Qty Sold", "Revenue"}, 0);
        categoryModel = new DefaultTableModel(new Object[]{"Category", "Qty Sold", "Revenue"}, 0);
        employeeModel = new DefaultTableModel(new Object[]{"Employee", "Orders Handled", "Total Sales"}, 0);
        lowStockModel = new DefaultTableModel(new Object[]{"Item / Ingredient", "Stock", "Reorder Level"}, 0);

        tabs.addTab("Daily Sales", wrapTable(dailyModel));
        tabs.addTab("Item Sales", wrapTable(itemModel));
        tabs.addTab("Category Sales", wrapTable(categoryModel));
        tabs.addTab("Employee Performance", wrapTable(employeeModel));
        tabs.addTab("Low Stock Alerts", wrapTable(lowStockModel));
        return tabs;
    }

    private JScrollPane wrapTable(DefaultTableModel model) {
        JTable table = new JTable(model) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        UITheme.styleTable(table);
        return new JScrollPane(table);
    }

    private JPanel buildExportBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bar.setBackground(UITheme.CREAM);
        JButton refreshBtn = UITheme.secondaryButton("Refresh Reports");
        JButton exportBtn = UITheme.primaryButton("Export Current Tab (CSV)");
        refreshBtn.addActionListener(e -> refreshAll());
        exportBtn.addActionListener(e -> exportCurrentTab());
        bar.add(refreshBtn);
        bar.add(exportBtn);
        return bar;
    }

    private void refreshAll() {
        Map<String, Object> today = reportDAO.getTodaySummary();
        todayOrdersLabel.setText(String.valueOf(today.getOrDefault("total_orders", 0)));
        todaySalesLabel.setText("$" + today.getOrDefault("total_sales", "0.00"));
        todayTaxLabel.setText("$" + today.getOrDefault("total_tax", "0.00"));

        fillModel(dailyModel, reportDAO.getDailySummary(
                LocalDate.now().minusDays(30).toString(), LocalDate.now().toString()),
                "sale_date", "total_orders", "total_sales", "total_discount", "total_tax");

        fillModel(itemModel, reportDAO.getItemSales(),
                "item_name", "category_name", "total_qty_sold", "total_revenue");

        fillModel(categoryModel, reportDAO.getCategorySales(),
                "category_name", "qty_sold", "revenue");

        fillModel(employeeModel, reportDAO.getEmployeePerformance(),
                "full_name", "orders_handled", "total_sales");

        lowStockModel.setRowCount(0);
        for (Map<String, Object> row : reportDAO.getLowStockItems()) {
            lowStockModel.addRow(new Object[]{row.get("item_name"), row.get("stock_qty"), row.get("reorder_level")});
        }
        for (Map<String, Object> row : reportDAO.getLowStockIngredients()) {
            lowStockModel.addRow(new Object[]{row.get("ingredient_name"), row.get("stock_qty"), row.get("reorder_level")});
        }
    }

    private void fillModel(DefaultTableModel model, List<Map<String, Object>> rows, String... keys) {
        model.setRowCount(0);
        for (Map<String, Object> row : rows) {
            Object[] values = new Object[keys.length];
            for (int i = 0; i < keys.length; i++) values[i] = row.get(keys[i]);
            model.addRow(values);
        }
    }

    private void exportCurrentTab() {
        int idx = tabs.getSelectedIndex();
        DefaultTableModel model;
        String name;
        switch (idx) {
            case 0: model = dailyModel; name = "daily_sales"; break;
            case 1: model = itemModel; name = "item_sales"; break;
            case 2: model = categoryModel; name = "category_sales"; break;
            case 3: model = employeeModel; name = "employee_performance"; break;
            default: model = lowStockModel; name = "low_stock_alerts"; break;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File(name + "_" + LocalDate.now() + ".csv"));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        try (FileWriter writer = new FileWriter(chooser.getSelectedFile())) {
            for (int c = 0; c < model.getColumnCount(); c++) {
                writer.write(model.getColumnName(c));
                if (c < model.getColumnCount() - 1) writer.write(",");
            }
            writer.write("\n");
            for (int r = 0; r < model.getRowCount(); r++) {
                for (int c = 0; c < model.getColumnCount(); c++) {
                    Object val = model.getValueAt(r, c);
                    writer.write(val == null ? "" : val.toString().replace(",", ";"));
                    if (c < model.getColumnCount() - 1) writer.write(",");
                }
                writer.write("\n");
            }
            JOptionPane.showMessageDialog(this, "Report exported successfully.", "Export Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to export: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
