package com.bhojanaloy.gui;

import com.bhojanaloy.dao.SupplierDAO;
import com.bhojanaloy.model.Supplier;
import com.bhojanaloy.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/** Supplier directory for raw ingredient sourcing. */
public class SupplierPanel extends JPanel {

    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Supplier", "Contact Person", "Phone", "Email", "Address"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable table = new JTable(model);
    private List<Supplier> current;

    public SupplierPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.CREAM);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        add(UITheme.heading("Supplier Directory"), BorderLayout.NORTH);
        UITheme.styleTable(table);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildButtonBar(), BorderLayout.SOUTH);

        refresh();
    }

    private JPanel buildButtonBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bar.setBackground(UITheme.CREAM);
        JButton addBtn = UITheme.primaryButton("Add Supplier");
        JButton deleteBtn = UITheme.dangerButton("Delete Selected");
        JButton refreshBtn = UITheme.secondaryButton("Refresh");

        addBtn.addActionListener(e -> showAddDialog());
        deleteBtn.addActionListener(e -> {
            Supplier sel = getSelected();
            if (sel != null && JOptionPane.showConfirmDialog(this, "Delete " + sel.getSupplierName() + "?",
                    "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                supplierDAO.deleteSupplier(sel.getSupplierId());
                refresh();
            }
        });
        refreshBtn.addActionListener(e -> refresh());

        bar.add(addBtn);
        bar.add(deleteBtn);
        bar.add(refreshBtn);
        return bar;
    }

    private Supplier getSelected() {
        int row = table.getSelectedRow();
        if (row < 0 || current == null || row >= current.size()) return null;
        return current.get(row);
    }

    private void refresh() {
        current = supplierDAO.getAll();
        model.setRowCount(0);
        for (Supplier s : current) {
            model.addRow(new Object[]{s.getSupplierId(), s.getSupplierName(), s.getContactPerson(),
                    s.getPhone(), s.getEmail(), s.getAddress()});
        }
    }

    private void showAddDialog() {
        JTextField nameField = new JTextField();
        JTextField contactField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField addressField = new JTextField();

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        formPanel.add(new JLabel("Supplier Name:")); formPanel.add(nameField);
        formPanel.add(new JLabel("Contact Person:")); formPanel.add(contactField);
        formPanel.add(new JLabel("Phone:")); formPanel.add(phoneField);
        formPanel.add(new JLabel("Email:")); formPanel.add(emailField);
        formPanel.add(new JLabel("Address:")); formPanel.add(addressField);

        int result = JOptionPane.showConfirmDialog(this, formPanel, "Add Supplier",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        Supplier s = new Supplier();
        s.setSupplierName(nameField.getText().trim());
        s.setContactPerson(contactField.getText().trim());
        s.setPhone(phoneField.getText().trim());
        s.setEmail(emailField.getText().trim());
        s.setAddress(addressField.getText().trim());
        supplierDAO.addSupplier(s);
        refresh();
    }
}
