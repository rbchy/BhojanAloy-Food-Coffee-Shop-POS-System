package com.bhojanaloy.gui;

import com.bhojanaloy.dao.EmployeeDAO;
import com.bhojanaloy.dao.UserDAO;
import com.bhojanaloy.model.Employee;
import com.bhojanaloy.util.UITheme;
import com.bhojanaloy.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Staff management + pay setup (hourly rate, direct deposit/cash) + login account creation. */
public class EmployeePanel extends JPanel {

    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final UserDAO userDAO = new UserDAO();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Name", "Designation", "Phone", "Email", "Hourly Rate", "Pay Method", "Hire Date"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable table = new JTable(model);
    private List<Employee> current;

    public EmployeePanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.CREAM);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        add(UITheme.heading("Employee Management"), BorderLayout.NORTH);
        UITheme.styleTable(table);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildButtonBar(), BorderLayout.SOUTH);

        refresh();
    }

    private JPanel buildButtonBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bar.setBackground(UITheme.CREAM);
        JButton addBtn = UITheme.primaryButton("Add Employee");
        JButton editBtn = UITheme.secondaryButton("Edit Selected (Pay & Bank Info)");
        JButton createLoginBtn = UITheme.secondaryButton("Create Login for Selected");
        JButton deactivateBtn = UITheme.dangerButton("Deactivate Selected");
        JButton refreshBtn = UITheme.secondaryButton("Refresh");

        addBtn.addActionListener(e -> showEmployeeDialog(null));
        editBtn.addActionListener(e -> {
            Employee sel = getSelected();
            if (sel != null) showEmployeeDialog(sel);
            else JOptionPane.showMessageDialog(this, "Select an employee first.", "No Selection", JOptionPane.WARNING_MESSAGE);
        });
        createLoginBtn.addActionListener(e -> showCreateLoginDialog());
        deactivateBtn.addActionListener(e -> {
            Employee sel = getSelected();
            if (sel != null && JOptionPane.showConfirmDialog(this, "Deactivate " + sel.getFullName() + "?",
                    "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                employeeDAO.deleteEmployee(sel.getEmployeeId());
                refresh();
            }
        });
        refreshBtn.addActionListener(e -> refresh());

        bar.add(addBtn);
        bar.add(editBtn);
        bar.add(createLoginBtn);
        bar.add(deactivateBtn);
        bar.add(refreshBtn);
        return bar;
    }

    private Employee getSelected() {
        int row = table.getSelectedRow();
        if (row < 0 || current == null || row >= current.size()) return null;
        return current.get(row);
    }

    private void refresh() {
        current = employeeDAO.getAllActive();
        model.setRowCount(0);
        for (Employee e : current) {
            model.addRow(new Object[]{e.getEmployeeId(), e.getFullName(), e.getDesignation(),
                    e.getPhone(), e.getEmail(), "$" + e.getHourlyRate() + "/hr", e.getPaymentPreference(), e.getHireDate()});
        }
    }

    private void showEmployeeDialog(Employee existing) {
        JTextField nameField = new JTextField(existing != null ? existing.getFullName() : "");
        JTextField designationField = new JTextField(existing != null ? existing.getDesignation() : "");
        JTextField phoneField = new JTextField(existing != null ? existing.getPhone() : "");
        JTextField emailField = new JTextField(existing != null ? existing.getEmail() : "");
        JTextField salaryField = new JTextField(existing != null && existing.getSalary() != null ? existing.getSalary().toString() : "0.00");
        JTextField hourlyRateField = new JTextField(existing != null && existing.getHourlyRate() != null ? existing.getHourlyRate().toString() : "0.00");
        JComboBox<String> paymentPrefBox = new JComboBox<>(new String[]{"CASH", "DIRECT_DEPOSIT", "CHECK"});
        if (existing != null && existing.getPaymentPreference() != null) paymentPrefBox.setSelectedItem(existing.getPaymentPreference());
        JTextField bankNameField = new JTextField(existing != null && existing.getBankName() != null ? existing.getBankName() : "");
        JTextField bankAccountField = new JTextField(existing != null && existing.getBankAccountNumber() != null ? existing.getBankAccountNumber() : "");
        JTextField bankRoutingField = new JTextField(existing != null && existing.getBankRoutingNumber() != null ? existing.getBankRoutingNumber() : "");

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        formPanel.add(new JLabel("Full Name:")); formPanel.add(nameField);
        formPanel.add(new JLabel("Designation:")); formPanel.add(designationField);
        formPanel.add(new JLabel("Phone:")); formPanel.add(phoneField);
        formPanel.add(new JLabel("Email:")); formPanel.add(emailField);
        formPanel.add(new JLabel("Monthly Salary (reference only):")); formPanel.add(salaryField);
        formPanel.add(new JLabel("Hourly Rate (for payroll):")); formPanel.add(hourlyRateField);
        formPanel.add(new JLabel("Payment Preference:")); formPanel.add(paymentPrefBox);
        formPanel.add(new JLabel("Bank Name:")); formPanel.add(bankNameField);
        formPanel.add(new JLabel("Bank Account Number:")); formPanel.add(bankAccountField);
        formPanel.add(new JLabel("Bank Routing Number:")); formPanel.add(bankRoutingField);

        int result = JOptionPane.showConfirmDialog(this, formPanel,
                existing == null ? "Add Employee" : "Edit Employee — " + existing.getFullName(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        if (!ValidationUtil.isNonEmpty(nameField.getText())) {
            JOptionPane.showMessageDialog(this, "Full name is required.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String phoneText = phoneField.getText().trim();
        if (!phoneText.isEmpty() && !ValidationUtil.isValidPhone(phoneText)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid phone number (7-15 digits), or leave it blank.",
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String emailText = emailField.getText().trim();
        if (!emailText.isEmpty() && !ValidationUtil.isValidEmail(emailText)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address, or leave it blank.",
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        BigDecimal salary = ValidationUtil.parseNonNegativeDecimal(salaryField.getText());
        if (salary == null) {
            JOptionPane.showMessageDialog(this, "Monthly salary must be a non-negative number.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        BigDecimal hourlyRate = ValidationUtil.parseNonNegativeDecimal(hourlyRateField.getText());
        if (hourlyRate == null) {
            JOptionPane.showMessageDialog(this, "Hourly rate must be a non-negative number.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String paymentPref = (String) paymentPrefBox.getSelectedItem();
        String bankAccountText = bankAccountField.getText().trim();
        if ("DIRECT_DEPOSIT".equals(paymentPref) && bankAccountText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Direct Deposit requires a bank account number (or choose Cash/Check instead).",
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Employee emp = existing != null ? existing : new Employee();
        emp.setFullName(nameField.getText().trim());
        emp.setDesignation(designationField.getText().trim());
        emp.setPhone(phoneText);
        emp.setEmail(emailText);
        emp.setSalary(salary);
        emp.setHourlyRate(hourlyRate);
        emp.setPaymentPreference(paymentPref);
        emp.setBankName(bankNameField.getText().trim().isEmpty() ? null : bankNameField.getText().trim());
        emp.setBankAccountNumber(bankAccountText.isEmpty() ? null : bankAccountText);
        emp.setBankRoutingNumber(bankRoutingField.getText().trim().isEmpty() ? null : bankRoutingField.getText().trim());
        if (existing == null) {
            emp.setHireDate(LocalDate.now());
            emp.setActive(true);
        }

        boolean success = existing != null ? employeeDAO.updateEmployee(emp) : employeeDAO.addEmployee(emp);
        if (!success) {
            JOptionPane.showMessageDialog(this, "Failed to save employee. Check DB connection.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        refresh();
    }

    private void showCreateLoginDialog() {
        Employee sel = getSelected();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Select an employee first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JTextField usernameField = new JTextField(sel.getFullName().toLowerCase().replace(" ", "."));
        JPasswordField passwordField = new JPasswordField("changeme123");
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"CASHIER", "MANAGER", "ADMIN"});

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        formPanel.add(new JLabel("Username:")); formPanel.add(usernameField);
        formPanel.add(new JLabel("Temp Password:")); formPanel.add(passwordField);
        formPanel.add(new JLabel("Role:")); formPanel.add(roleBox);

        int result = JOptionPane.showConfirmDialog(this, formPanel, "Create Login for " + sel.getFullName(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        boolean created = userDAO.createUser(usernameField.getText().trim(),
                new String(passwordField.getPassword()), sel.getFullName(),
                (String) roleBox.getSelectedItem(), sel.getEmployeeId());

        JOptionPane.showMessageDialog(this, created ? "Login created successfully." : "Failed to create login (username may already exist).",
                created ? "Success" : "Error", created ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
    }
}
