package com.bhojanaloy.gui;

import com.bhojanaloy.dao.DeliveryDAO;
import com.bhojanaloy.model.Delivery;
import com.bhojanaloy.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Delivery management: every DELIVERY-type order lands here as UNASSIGNED.
 * Staff assign it to an in-house rider or a third-party agency (e.g. a food
 * delivery service), then track it through PICKED_UP (collected from the
 * shop) to DELIVERED (dropped off at the customer's address). Customers who
 * self-collect use Pickup order type instead and never appear here.
 */
public class DeliveryPanel extends JPanel {

    private final DeliveryDAO deliveryDAO = new DeliveryDAO();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Order #", "Customer", "Phone", "Address", "Total", "Agency/Rider", "Rider Phone", "Status"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable table = new JTable(model);
    private List<Delivery> current;

    public DeliveryPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.CREAM);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        add(UITheme.heading("Delivery Management"), BorderLayout.NORTH);
        UITheme.styleTable(table);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildButtonBar(), BorderLayout.SOUTH);

        refresh();
    }

    private JPanel buildButtonBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bar.setBackground(UITheme.CREAM);
        JButton assignBtn = UITheme.primaryButton("Assign Rider/Agency");
        JButton pickedUpBtn = UITheme.secondaryButton("Mark Picked Up");
        JButton deliveredBtn = UITheme.successButton("Mark Delivered");
        JButton cancelBtn = UITheme.dangerButton("Cancel Delivery");
        JButton refreshBtn = UITheme.secondaryButton("Refresh");

        assignBtn.addActionListener(e -> showAssignDialog());
        pickedUpBtn.addActionListener(e -> withSelected(d -> {
            deliveryDAO.markPickedUp(d.getDeliveryId());
            refresh();
        }));
        deliveredBtn.addActionListener(e -> withSelected(d -> {
            deliveryDAO.markDelivered(d.getDeliveryId());
            refresh();
        }));
        cancelBtn.addActionListener(e -> withSelected(d -> {
            String reason = JOptionPane.showInputDialog(this, "Reason for cancellation:");
            if (reason != null) {
                deliveryDAO.cancelDelivery(d.getDeliveryId(), reason);
                refresh();
            }
        }));
        refreshBtn.addActionListener(e -> refresh());

        bar.add(assignBtn);
        bar.add(pickedUpBtn);
        bar.add(deliveredBtn);
        bar.add(cancelBtn);
        bar.add(refreshBtn);
        return bar;
    }

    private interface DeliveryAction { void run(Delivery d); }

    private void withSelected(DeliveryAction action) {
        Delivery d = getSelected();
        if (d == null) {
            JOptionPane.showMessageDialog(this, "Select a delivery first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        action.run(d);
    }

    private Delivery getSelected() {
        int row = table.getSelectedRow();
        if (row < 0 || current == null || row >= current.size()) return null;
        return current.get(row);
    }

    private void refresh() {
        current = deliveryDAO.getActiveDeliveries();
        model.setRowCount(0);
        for (Delivery d : current) {
            String agencyRider = (d.getAgencyName() != null ? d.getAgencyName() : "") +
                    (d.getRiderName() != null ? " / " + d.getRiderName() : "");
            model.addRow(new Object[]{d.getOrderNumber(), d.getCustomerName(), d.getCustomerPhone(),
                    d.getCustomerAddress(), "$" + d.getTotalAmount(), agencyRider, d.getRiderPhone(), d.getStatus()});
        }
    }

    private void showAssignDialog() {
        Delivery d = getSelected();
        if (d == null) {
            JOptionPane.showMessageDialog(this, "Select a delivery first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JComboBox<String> agencyBox = new JComboBox<>(new String[]{
                "In-house Rider", "Pathao Courier", "Foodpanda Rider", "Steadfast Courier", "Other Agency"});
        JTextField riderNameField = new JTextField();
        JTextField riderPhoneField = new JTextField();

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        formPanel.add(new JLabel("Order:")); formPanel.add(new JLabel(d.getOrderNumber()));
        formPanel.add(new JLabel("Customer:")); formPanel.add(new JLabel(d.getCustomerName() + " (" + d.getCustomerPhone() + ")"));
        formPanel.add(new JLabel("Address:")); formPanel.add(new JLabel(d.getCustomerAddress()));
        formPanel.add(new JLabel("Agency:")); formPanel.add(agencyBox);
        formPanel.add(new JLabel("Rider Name:")); formPanel.add(riderNameField);
        formPanel.add(new JLabel("Rider Phone:")); formPanel.add(riderPhoneField);

        int result = JOptionPane.showConfirmDialog(this, formPanel, "Assign Rider/Agency — the rider collects this order copy from the shop and delivers it to the customer's address",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        deliveryDAO.assignRider(d.getDeliveryId(), (String) agencyBox.getSelectedItem(),
                riderNameField.getText().trim(), riderPhoneField.getText().trim());
        refresh();
    }
}
