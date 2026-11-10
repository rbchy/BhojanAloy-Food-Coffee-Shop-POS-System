package com.bhojanaloy.gui;

import com.bhojanaloy.model.Order;
import com.bhojanaloy.service.ReceiptDeliveryService;
import com.bhojanaloy.util.BillFormatter;
import com.bhojanaloy.util.ReceiptPrinter;
import com.bhojanaloy.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Shows the finished item-wise bill on screen first, so the cashier (or
 * customer) can review it before deciding how to deliver it: print it on
 * paper, or send it electronically (Email / SMS) — whichever the customer
 * prefers. This is shown right after a successful checkout in POSPanel.
 */
public class BillPreviewDialog extends JDialog {

    private final ReceiptDeliveryService deliveryService = new ReceiptDeliveryService();
    private final Order order;

    public BillPreviewDialog(Frame owner, Order order) {
        super(owner, "Bill Preview — " + order.getOrderNumber(), true);
        this.order = order;
        setSize(480, 620);
        setLocationRelativeTo(owner);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(UITheme.CREAM);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel title = UITheme.heading("Bill / Receipt Preview");

        JTextArea billArea = new JTextArea(BillFormatter.format(order));
        billArea.setEditable(false);
        billArea.setFont(UITheme.FONT_MONO);
        billArea.setMargin(new Insets(12, 12, 12, 12));
        billArea.setBackground(UITheme.WHITE);
        JScrollPane scrollPane = new JScrollPane(billArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.CREAM_DARKER));

        JButton printBtn = UITheme.primaryButton("🖨 Print");
        printBtn.addActionListener(e -> ReceiptPrinter.printReceipt(order, this));

        JButton sendBtn = UITheme.secondaryButton("✉ Send to Customer");
        sendBtn.addActionListener(e -> showSendDialog());

        JButton closeBtn = UITheme.dangerButton("Close");
        closeBtn.addActionListener(e -> dispose());

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonRow.setOpaque(false);
        buttonRow.add(printBtn);
        buttonRow.add(sendBtn);
        buttonRow.add(closeBtn);

        root.add(title, BorderLayout.NORTH);
        root.add(scrollPane, BorderLayout.CENTER);
        root.add(buttonRow, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void showSendDialog() {
        JComboBox<String> channelBox = new JComboBox<>(new String[]{"SMS", "EMAIL"});
        JTextField destinationField = new JTextField(order.getCustomerPhone() != null ? order.getCustomerPhone() : "");

        channelBox.addActionListener(e -> {
            if ("SMS".equals(channelBox.getSelectedItem()) && order.getCustomerPhone() != null) {
                destinationField.setText(order.getCustomerPhone());
            } else if ("EMAIL".equals(channelBox.getSelectedItem())) {
                destinationField.setText("");
            }
        });

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Send via:")); form.add(channelBox);
        form.add(new JLabel("Phone / Email:")); form.add(destinationField);

        int result = JOptionPane.showConfirmDialog(this, form, "Send Receipt to Customer",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String channel = (String) channelBox.getSelectedItem();
        String destination = destinationField.getText().trim();
        if (destination.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a phone number or email address.", "Missing Info", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog progress = new JDialog(this, "Sending...", true);
        JLabel progressLabel = new JLabel("  Sending receipt via " + channel + "...  ", SwingConstants.CENTER);
        progressLabel.setBorder(new EmptyBorder(20, 20, 20, 20));
        progress.add(progressLabel);
        progress.pack();
        progress.setLocationRelativeTo(this);

        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override protected String doInBackground() throws Exception {
                return deliveryService.send(channel, destination, BillFormatter.format(order));
            }
            @Override protected void done() {
                progress.dispose();
                try {
                    String ref = get();
                    JOptionPane.showMessageDialog(BillPreviewDialog.this,
                            "Receipt sent via " + channel + " to " + destination + "\nDelivery ref: " + ref,
                            "Sent", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(BillPreviewDialog.this,
                            "Failed to send: " + ex.getMessage(), "Send Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
        progress.setVisible(true);
    }
}