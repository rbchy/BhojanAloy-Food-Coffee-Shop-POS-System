package com.bhojanaloy.gui;

import com.bhojanaloy.model.Payment;
import com.bhojanaloy.service.PaymentService;
import com.bhojanaloy.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;

/**
 * Modal "payment gateway" screen shown during checkout for CARD or
 * MOBILE_BANKING orders. Since there's no live merchant account wired up,
 * PaymentService simulates the gateway round-trip — this dialog just gives
 * the cashier a realistic flow (choose provider, enter reference, wait for
 * confirmation, retry on decline) so the rest of the app can be built and
 * demoed end-to-end. Swap PaymentService's internals for a real gateway SDK
 * to go live.
 */
public class PaymentDialog extends JDialog {

    private final PaymentService paymentService = new PaymentService();
    private final String method; // CARD or MOBILE_BANKING
    private final BigDecimal amount;

    private final JComboBox<String> providerBox;
    private final JTextField refField = new JTextField();
    private final JLabel statusLabel = new JLabel(" ");
    private final JButton payButton = UITheme.primaryButton("Pay Now");
    private final JButton cancelButton = UITheme.secondaryButton("Cancel");
    private final JProgressBar progressBar = new JProgressBar();

    private Payment result; // null until a successful payment is confirmed, or dialog is cancelled

    public PaymentDialog(Frame owner, String method, BigDecimal amount) {
        super(owner, "Payment Gateway (Simulated)", true);
        this.method = method;
        this.amount = amount;
        this.providerBox = new JComboBox<>(method.equals("CARD")
                ? new String[]{"Visa", "Mastercard", "American Express"}
                : new String[]{"bKash", "Nagad", "Rocket"});

        setSize(420, 340);
        setLocationRelativeTo(owner);
        setResizable(false);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(UITheme.CREAM);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel title = UITheme.heading(method.equals("CARD") ? "Card Payment" : "Mobile Banking Payment");
        JLabel amountLabel = new JLabel("Amount to charge: $" + amount);
        amountLabel.setFont(UITheme.FONT_LABEL_B);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 10));
        form.setOpaque(false);
        form.add(new JLabel(method.equals("CARD") ? "Card Network:" : "Provider:"));
        form.add(providerBox);
        form.add(new JLabel(method.equals("CARD") ? "Card Last 4 Digits:" : "Mobile Account No.:"));
        form.add(refField);

        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setStringPainted(true);
        progressBar.setString("Contacting gateway...");

        statusLabel.setFont(UITheme.FONT_LABEL);

        payButton.addActionListener(e -> processPayment());
        cancelButton.addActionListener(e -> {
            result = null;
            dispose();
        });

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttonRow.setOpaque(false);
        buttonRow.add(cancelButton);
        buttonRow.add(payButton);

        JPanel north = new JPanel();
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.setOpaque(false);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        amountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        north.add(title);
        north.add(Box.createVerticalStrut(6));
        north.add(amountLabel);
        north.add(Box.createVerticalStrut(14));

        JPanel south = new JPanel();
        south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
        south.setOpaque(false);
        south.add(progressBar);
        south.add(Box.createVerticalStrut(8));
        south.add(statusLabel);
        south.add(Box.createVerticalStrut(10));
        south.add(buttonRow);

        root.add(north, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(south, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void processPayment() {
        String ref = refField.getText().trim();
        if (ref.isEmpty()) {
            statusLabel.setForeground(UITheme.ACCENT_RED);
            statusLabel.setText(method.equals("CARD") ? "Enter the card's last 4 digits." : "Enter the mobile account number.");
            return;
        }

        payButton.setEnabled(false);
        cancelButton.setEnabled(false);
        progressBar.setVisible(true);
        statusLabel.setForeground(UITheme.COFFEE_MEDIUM);
        statusLabel.setText("Processing payment, please wait...");

        String provider = (String) providerBox.getSelectedItem();

        SwingWorker<Payment, Void> worker = new SwingWorker<Payment, Void>() {
            @Override
            protected Payment doInBackground() {
                return paymentService.processPayment(method, provider, amount, ref);
            }

            @Override
            protected void done() {
                progressBar.setVisible(false);
                payButton.setEnabled(true);
                cancelButton.setEnabled(true);
                try {
                    Payment payment = get();
                    if (payment.isSuccessful()) {
                        result = payment;
                        statusLabel.setForeground(UITheme.ACCENT_GREEN);
                        statusLabel.setText("Payment approved! Ref: " + payment.getReferenceNumber());
                        payButton.setEnabled(false);
                        Timer closeTimer = new Timer(900, e -> dispose());
                        closeTimer.setRepeats(false);
                        closeTimer.start();
                    } else {
                        statusLabel.setForeground(UITheme.ACCENT_RED);
                        statusLabel.setText("Payment declined by gateway. Please try again.");
                    }
                } catch (Exception ex) {
                    statusLabel.setForeground(UITheme.ACCENT_RED);
                    statusLabel.setText("Gateway error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    /** Returns the successful Payment, or null if the customer cancelled / it was never completed. */
    public Payment getResult() {
        return result;
    }
}
