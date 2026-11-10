package com.bhojanaloy.gui;

import com.bhojanaloy.model.Employee;
import com.bhojanaloy.service.PayrollService;
import com.bhojanaloy.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;

/**
 * Simulated bank transfer confirmation shown when paying an employee via
 * Direct Deposit. No live ACH/banking integration is wired up — see
 * PayrollService.simulateDirectDeposit for where to plug in a real one.
 */
public class DirectDepositDialog extends JDialog {

    private final PayrollService payrollService = new PayrollService();
    private final Employee employee;
    private final BigDecimal amount;
    private String reference; // set once the simulated transfer completes

    public DirectDepositDialog(Frame owner, Employee employee, BigDecimal amount) {
        super(owner, "Direct Deposit (Simulated)", true);
        this.employee = employee;
        this.amount = amount;
        setSize(400, 260);
        setLocationRelativeTo(owner);
        setResizable(false);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(UITheme.CREAM);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel title = UITheme.heading("Direct Deposit Transfer");
        JPanel details = new JPanel(new GridLayout(0, 2, 6, 6));
        details.setOpaque(false);
        details.add(new JLabel("Employee:")); details.add(new JLabel(employee.getFullName()));
        details.add(new JLabel("Bank:")); details.add(new JLabel(employee.getBankName() != null ? employee.getBankName() : "—"));
        details.add(new JLabel("Account:")); details.add(new JLabel(maskAccount(employee.getBankAccountNumber())));
        details.add(new JLabel("Amount:")); details.add(new JLabel("$" + amount));

        JLabel statusLabel = new JLabel(" ");
        statusLabel.setFont(UITheme.FONT_LABEL);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setStringPainted(true);
        progressBar.setString("Sending transfer...");

        JButton sendBtn = UITheme.primaryButton("Send Transfer");
        JButton cancelBtn = UITheme.secondaryButton("Cancel");
        cancelBtn.addActionListener(e -> { reference = null; dispose(); });

        sendBtn.addActionListener(e -> {
            sendBtn.setEnabled(false);
            cancelBtn.setEnabled(false);
            progressBar.setVisible(true);
            SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
                @Override protected String doInBackground() { return payrollService.simulateDirectDeposit(employee, amount); }
                @Override protected void done() {
                    try {
                        reference = get();
                        statusLabel.setForeground(UITheme.ACCENT_GREEN);
                        statusLabel.setText("Transfer sent! Ref: " + reference);
                        Timer t = new Timer(800, ev -> dispose());
                        t.setRepeats(false);
                        t.start();
                    } catch (Exception ex) {
                        statusLabel.setForeground(UITheme.ACCENT_RED);
                        statusLabel.setText("Transfer failed: " + ex.getMessage());
                        sendBtn.setEnabled(true);
                        cancelBtn.setEnabled(true);
                    }
                }
            };
            worker.execute();
        });
        progressBar.setVisible(false);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttonRow.setOpaque(false);
        buttonRow.add(cancelBtn);
        buttonRow.add(sendBtn);

        JPanel south = new JPanel();
        south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
        south.setOpaque(false);
        south.add(progressBar);
        south.add(statusLabel);
        south.add(Box.createVerticalStrut(8));
        south.add(buttonRow);

        root.add(title, BorderLayout.NORTH);
        root.add(details, BorderLayout.CENTER);
        root.add(south, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private String maskAccount(String account) {
        if (account == null || account.length() < 4) return "—";
        return "****" + account.substring(account.length() - 4);
    }

    /** The simulated transfer reference, or null if cancelled/failed. */
    public String getReference() {
        return reference;
    }
}
