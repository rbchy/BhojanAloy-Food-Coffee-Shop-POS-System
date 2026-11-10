package com.bhojanaloy.gui;

import com.bhojanaloy.dao.RawIngredientDAO;
import com.bhojanaloy.dao.SupplierDAO;
import com.bhojanaloy.model.RawIngredient;
import com.bhojanaloy.model.Supplier;
import com.bhojanaloy.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/** Raw ingredient stock tracking with low-stock highlighting and manual adjustments. */
public class InventoryPanel extends JPanel {

    private final RawIngredientDAO ingredientDAO = new RawIngredientDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Ingredient", "Unit", "Stock Qty", "Reorder Lvl", "Unit Cost", "Status"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable table = new JTable(model);
    private List<RawIngredient> current;

    public InventoryPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.CREAM);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        add(UITheme.heading("Raw Ingredient Inventory"), BorderLayout.NORTH);
        UITheme.styleTable(table);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildButtonBar(), BorderLayout.SOUTH);

        refresh();
    }

    private JPanel buildButtonBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bar.setBackground(UITheme.CREAM);
        JButton addBtn = UITheme.primaryButton("Add Ingredient");
        JButton adjustBtn = UITheme.secondaryButton("Adjust Stock");
        JButton refreshBtn = UITheme.secondaryButton("Refresh");
        JButton lowStockBtn = UITheme.dangerButton("Show Low Stock Only");

        addBtn.addActionListener(e -> showAddDialog());
        adjustBtn.addActionListener(e -> showAdjustDialog());
        refreshBtn.addActionListener(e -> refresh());
        lowStockBtn.addActionListener(e -> {
            current = ingredientDAO.getLowStock();
            populateTable(current);
        });

        bar.add(addBtn);
        bar.add(adjustBtn);
        bar.add(lowStockBtn);
        bar.add(refreshBtn);
        return bar;
    }

    private void refresh() {
        current = ingredientDAO.getAll();
        populateTable(current);
    }

    private void populateTable(List<RawIngredient> list) {
        model.setRowCount(0);
        for (RawIngredient ri : list) {
            model.addRow(new Object[]{ri.getIngredientId(), ri.getIngredientName(), ri.getUnit(),
                    ri.getStockQty(), ri.getReorderLevel(), ri.getUnitCost(),
                    ri.isLowStock() ? "⚠ LOW STOCK" : "OK"});
        }
    }

    private void showAddDialog() {
        List<Supplier> suppliers = supplierDAO.getAll();
        JComboBox<Supplier> supplierBox = new JComboBox<>(suppliers.toArray(new Supplier[0]));
        JTextField nameField = new JTextField();
        JTextField unitField = new JTextField("g");
        JTextField stockField = new JTextField("0");
        JTextField reorderField = new JTextField("100");
        JTextField costField = new JTextField("0.00");

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        formPanel.add(new JLabel("Name:")); formPanel.add(nameField);
        formPanel.add(new JLabel("Unit (g/ml/pcs):")); formPanel.add(unitField);
        formPanel.add(new JLabel("Initial Stock:")); formPanel.add(stockField);
        formPanel.add(new JLabel("Reorder Level:")); formPanel.add(reorderField);
        formPanel.add(new JLabel("Unit Cost:")); formPanel.add(costField);
        formPanel.add(new JLabel("Supplier:")); formPanel.add(supplierBox);

        int result = JOptionPane.showConfirmDialog(this, formPanel, "Add Raw Ingredient",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            RawIngredient ri = new RawIngredient();
            ri.setIngredientName(nameField.getText().trim());
            ri.setUnit(unitField.getText().trim());
            ri.setStockQty(new BigDecimal(stockField.getText().trim()));
            ri.setReorderLevel(new BigDecimal(reorderField.getText().trim()));
            ri.setUnitCost(new BigDecimal(costField.getText().trim()));
            Supplier sel = (Supplier) supplierBox.getSelectedItem();
            if (sel != null) ri.setSupplierId(sel.getSupplierId());
            ingredientDAO.addIngredient(ri);
            refresh();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void showAdjustDialog() {
        int row = table.getSelectedRow();
        if (row < 0 || current == null || row >= current.size()) {
            JOptionPane.showMessageDialog(this, "Select an ingredient first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        RawIngredient ri = current.get(row);
        JTextField deltaField = new JTextField("0");
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"PURCHASE_IN", "ADJUSTMENT", "WASTE"});
        JTextField notesField = new JTextField();

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        formPanel.add(new JLabel("Ingredient:")); formPanel.add(new JLabel(ri.getIngredientName()));
        formPanel.add(new JLabel("Quantity change (+/-):")); formPanel.add(deltaField);
        formPanel.add(new JLabel("Transaction Type:")); formPanel.add(typeBox);
        formPanel.add(new JLabel("Notes:")); formPanel.add(notesField);

        int result = JOptionPane.showConfirmDialog(this, formPanel, "Adjust Stock",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            BigDecimal delta = new BigDecimal(deltaField.getText().trim());
            String type = (String) typeBox.getSelectedItem();
            if ("WASTE".equals(type) && delta.compareTo(BigDecimal.ZERO) > 0) delta = delta.negate();
            ingredientDAO.adjustStock(ri.getIngredientId(), delta, type, notesField.getText().trim());
            refresh();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
        }
    }
}
