package com.bhojanaloy.gui;

import com.bhojanaloy.dao.EmployeeDAO;
import com.bhojanaloy.dao.HolidayDAO;
import com.bhojanaloy.dao.PayrollDAO;
import com.bhojanaloy.dao.TimeClockDAO;
import com.bhojanaloy.model.Employee;
import com.bhojanaloy.model.Holiday;
import com.bhojanaloy.model.PayrollRun;
import com.bhojanaloy.model.TimeClock;
import com.bhojanaloy.service.PayrollService;
import com.bhojanaloy.util.PayStubPrinter;
import com.bhojanaloy.util.UITheme;
import com.bhojanaloy.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Payroll processing: pick an employee + pay period, calculate their hours
 * breakdown (regular/overtime/weekend/holiday) from Time Clock records,
 * review gross/net pay, then pay them out via simulated Direct Deposit or
 * mark Cash/Check paid in person. Also manages the company holiday calendar
 * that feeds the holiday-pay multiplier, and shows payroll history.
 */
public class PayrollPanel extends JPanel {

    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final TimeClockDAO timeClockDAO = new TimeClockDAO();
    private final HolidayDAO holidayDAO = new HolidayDAO();
    private final PayrollDAO payrollDAO = new PayrollDAO();
    private final PayrollService payrollService = new PayrollService();

    private final JComboBox<Employee> employeeBox = new JComboBox<>();
    private final JSpinner startSpinner = new JSpinner(new SpinnerDateModel());
    private final JSpinner endSpinner = new JSpinner(new SpinnerDateModel());
    private final JComboBox<String> paymentMethodBox = new JComboBox<>(new String[]{"DIRECT_DEPOSIT", "CASH", "CHECK"});

    // Hours breakdown is editable so a manager can correct a missed clock-out or
    // add a manual adjustment before processing payment (see recalcFromFields()).
    private final JTextField regularField = new JTextField("0", 5);
    private final JTextField overtimeField = new JTextField("0", 5);
    private final JTextField weekendField = new JTextField("0", 5);
    private final JTextField holidayField = new JTextField("0", 5);
    private final JTextField deductionsField = new JTextField("0.00", 5);
    private final JLabel grossLabel = new JLabel("$0.00");
    private final JLabel netLabel = new JLabel("$0.00");

    private final DefaultTableModel historyModel = new DefaultTableModel(
            new Object[]{"Employee", "Period", "Reg", "OT", "Wknd", "Hol", "Gross", "Net", "Method", "Status"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable historyTable = new JTable(historyModel);
    private List<PayrollRun> historyList; // parallel to historyModel's rows, so Delete Selected knows the payroll_id

    private PayrollRun calculatedRun; // the most recently calculated (but not yet saved) run

    public PayrollPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.CREAM);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        add(UITheme.heading("Payroll Processing"), BorderLayout.NORTH);

        JPanel top = buildControlPanel();
        JPanel history = buildHistoryPanel();

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, history);
        split.setDividerLocation(280);
        split.setResizeWeight(0.4);
        add(split, BorderLayout.CENTER);

        loadEmployees();
        setDefaultPeriod();
        refreshHistory();
    }

    private JPanel buildControlPanel() {
        JPanel panel = UITheme.sectionPanel();
        panel.setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(2, 4, 10, 8));
        form.setOpaque(false);
        form.add(new JLabel("Employee:")); form.add(employeeBox);
        form.add(new JLabel("Period Start:")); form.add(startSpinner);
        form.add(new JLabel("Period End:")); form.add(endSpinner);
        form.add(new JLabel("Payment Method:")); form.add(paymentMethodBox);

        startSpinner.setEditor(new JSpinner.DateEditor(startSpinner, "yyyy-MM-dd"));
        endSpinner.setEditor(new JSpinner.DateEditor(endSpinner, "yyyy-MM-dd"));

        JPanel breakdown = new JPanel(new GridLayout(3, 6, 6, 4));
        breakdown.setOpaque(false);
        breakdown.setBorder(BorderFactory.createTitledBorder("Hours Breakdown (editable — click Recalculate after changes)"));
        breakdown.add(new JLabel("Regular:")); breakdown.add(regularField);
        breakdown.add(new JLabel("Overtime:")); breakdown.add(overtimeField);
        breakdown.add(new JLabel("Weekend:")); breakdown.add(weekendField);
        breakdown.add(new JLabel("Holiday:")); breakdown.add(holidayField);
        breakdown.add(new JLabel("Deductions ($):")); breakdown.add(deductionsField);
        breakdown.add(new JLabel(""));
        JLabel grossCaption = new JLabel("Gross Pay:"); grossCaption.setFont(UITheme.FONT_LABEL_B);
        grossLabel.setFont(UITheme.FONT_LABEL_B);
        breakdown.add(grossCaption); breakdown.add(grossLabel);
        JLabel netCaption = new JLabel("Net Pay:"); netCaption.setFont(UITheme.FONT_HEADING);
        netLabel.setFont(UITheme.FONT_HEADING);
        netLabel.setForeground(UITheme.ACCENT_ORANGE);
        breakdown.add(netCaption); breakdown.add(netLabel);

        JButton calcBtn = UITheme.secondaryButton("Calculate Hours & Pay");
        calcBtn.addActionListener(e -> calculate());
        JButton recalcBtn = UITheme.secondaryButton("Recalculate");
        recalcBtn.addActionListener(e -> recalcFromFields());
        JButton payBtn = UITheme.successButton("Process Payment");
        payBtn.addActionListener(e -> processPayment());
        JButton holidaysBtn = UITheme.secondaryButton("Manage Holidays");
        holidaysBtn.addActionListener(e -> showHolidaysDialog());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        buttons.setOpaque(false);
        buttons.add(calcBtn);
        buttons.add(recalcBtn);
        buttons.add(payBtn);
        buttons.add(holidaysBtn);

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(form, BorderLayout.NORTH);
        north.add(breakdown, BorderLayout.CENTER);

        panel.add(north, BorderLayout.NORTH);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(UITheme.CREAM);
        panel.add(UITheme.heading("Payroll History"), BorderLayout.NORTH);
        UITheme.styleTable(historyTable);
        panel.add(new JScrollPane(historyTable), BorderLayout.CENTER);

        JButton refreshBtn = UITheme.secondaryButton("Refresh History");
        refreshBtn.addActionListener(e -> refreshHistory());
        JButton deleteBtn = UITheme.dangerButton("Delete Selected");
        deleteBtn.addActionListener(e -> deleteSelectedHistoryRow());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.setOpaque(false);
        south.add(refreshBtn);
        south.add(deleteBtn);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    private void deleteSelectedHistoryRow() {
        int row = historyTable.getSelectedRow();
        if (row < 0 || historyList == null || row >= historyList.size()) {
            JOptionPane.showMessageDialog(this, "Select a row in Payroll History first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        PayrollRun run = historyList.get(row);
        int choice = JOptionPane.showConfirmDialog(this,
                "Delete the payroll record for " + run.getEmployeeName() + " (" + run.getPeriodStart() + " to " +
                        run.getPeriodEnd() + ", net pay $" + run.getNetPay() + ")?\n\nThis cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;

        if (payrollDAO.deletePayrollRun(run.getPayrollId())) {
            refreshHistory();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to delete. Check database connection.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadEmployees() {
        employeeBox.removeAllItems();
        for (Employee e : employeeDAO.getAllActive()) employeeBox.addItem(e);
    }

    private void setDefaultPeriod() {
        Calendar cal = Calendar.getInstance();
        endSpinner.setValue(cal.getTime());
        cal.add(Calendar.DAY_OF_MONTH, -6);
        startSpinner.setValue(cal.getTime());
    }

    private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void calculate() {
        Employee employee = (Employee) employeeBox.getSelectedItem();
        if (employee == null) {
            JOptionPane.showMessageDialog(this, "Select an employee.", "No Employee", JOptionPane.WARNING_MESSAGE);
            return;
        }
        LocalDate start = toLocalDate((Date) startSpinner.getValue());
        LocalDate end = toLocalDate((Date) endSpinner.getValue());
        if (end.isBefore(start)) {
            JOptionPane.showMessageDialog(this, "Period end must be on or after period start.", "Invalid Period", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<TimeClock> shifts = timeClockDAO.getShiftsForPeriod(employee.getEmployeeId(), start, end);
        Set<LocalDate> holidays = holidayDAO.getHolidayDatesInRange(start, end);

        calculatedRun = payrollService.calculate(employee, start, end, shifts, holidays);
        paymentMethodBox.setSelectedItem(employee.getPaymentPreference());

        regularField.setText(calculatedRun.getRegularHours().toString());
        overtimeField.setText(calculatedRun.getOvertimeHours().toString());
        weekendField.setText(calculatedRun.getWeekendHours().toString());
        holidayField.setText(calculatedRun.getHolidayHours().toString());
        deductionsField.setText("0.00");
        grossLabel.setText("$" + calculatedRun.getGrossPay());
        netLabel.setText("$" + calculatedRun.getNetPay());

        if (shifts.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    employee.getFullName() + " has no completed (clocked-out) shifts in this period.",
                    "No Shifts Found", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Recomputes gross/net pay from whatever is currently in the (editable)
     * hours-breakdown fields and the deductions field — lets a manager fix a
     * missed clock-out, add a manual bonus/adjustment hour, or apply a
     * deduction before processing payment, without re-running Calculate.
     * Returns true if the fields were valid and the run was updated.
     */
    private boolean recalcFromFields() {
        if (calculatedRun == null) {
            JOptionPane.showMessageDialog(this, "Click 'Calculate Hours & Pay' first.", "Nothing to Recalculate", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        BigDecimal regular = ValidationUtil.parseNonNegativeDecimal(regularField.getText());
        BigDecimal overtime = ValidationUtil.parseNonNegativeDecimal(overtimeField.getText());
        BigDecimal weekend = ValidationUtil.parseNonNegativeDecimal(weekendField.getText());
        BigDecimal holiday = ValidationUtil.parseNonNegativeDecimal(holidayField.getText());
        BigDecimal deductions = ValidationUtil.parseNonNegativeDecimal(deductionsField.getText());
        if (regular == null || overtime == null || weekend == null || holiday == null || deductions == null) {
            JOptionPane.showMessageDialog(this, "Hours and deductions must all be non-negative numbers.",
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        BigDecimal rate = calculatedRun.getHourlyRate() != null ? calculatedRun.getHourlyRate() : BigDecimal.ZERO;
        BigDecimal gross = regular.multiply(rate)
                .add(overtime.multiply(rate).multiply(PayrollService.OVERTIME_MULTIPLIER))
                .add(weekend.multiply(rate).multiply(PayrollService.WEEKEND_MULTIPLIER))
                .add(holiday.multiply(rate).multiply(PayrollService.HOLIDAY_MULTIPLIER))
                .setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal net = gross.subtract(deductions).setScale(2, java.math.RoundingMode.HALF_UP);

        calculatedRun.setRegularHours(regular);
        calculatedRun.setOvertimeHours(overtime);
        calculatedRun.setWeekendHours(weekend);
        calculatedRun.setHolidayHours(holiday);
        calculatedRun.setDeductions(deductions);
        calculatedRun.setGrossPay(gross);
        calculatedRun.setNetPay(net);

        grossLabel.setText("$" + gross);
        netLabel.setText("$" + net);
        return true;
    }

    private void processPayment() {
        if (calculatedRun == null) {
            JOptionPane.showMessageDialog(this, "Calculate hours & pay first.", "Nothing to Pay", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Make sure any manual edits to the hours/deductions fields are captured
        // before we save — recalcFromFields() also validates them.
        if (!recalcFromFields()) return;
        Employee employee = (Employee) employeeBox.getSelectedItem();
        String method = (String) paymentMethodBox.getSelectedItem();
        calculatedRun.setPaymentMethod(method);

        // A payroll run for this exact employee + period may already have been
        // processed (e.g. the manager is correcting hours and reprocessing) —
        // offer to overwrite it instead of silently creating a duplicate row.
        if (calculatedRun.getPayrollId() <= 0) {
            PayrollRun existing = payrollDAO.findExistingRun(employee.getEmployeeId(),
                    calculatedRun.getPeriodStart(), calculatedRun.getPeriodEnd());
            if (existing != null) {
                int overwriteChoice = JOptionPane.showConfirmDialog(this,
                        "A payroll run for " + employee.getFullName() + " for " + calculatedRun.getPeriodStart() +
                                " to " + calculatedRun.getPeriodEnd() + " already exists (status: " + existing.getStatus() +
                                ", net pay $" + existing.getNetPay() + ").\n\n" +
                                "Overwrite it with the corrected figures? Choosing No cancels so you can avoid a duplicate.",
                        "Existing Payroll Run Found", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (overwriteChoice != JOptionPane.YES_OPTION) return;
                calculatedRun.setPayrollId(existing.getPayrollId());
            }
        }

        if (method.equals("DIRECT_DEPOSIT")) {
            if (employee.getBankAccountNumber() == null || employee.getBankAccountNumber().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        employee.getFullName() + " has no bank account on file. Add one in Employees, or pick Cash/Check.",
                        "Missing Bank Info", JOptionPane.WARNING_MESSAGE);
                return;
            }
            DirectDepositDialog dialog = new DirectDepositDialog((Frame) SwingUtilities.getWindowAncestor(this),
                    employee, calculatedRun.getNetPay());
            dialog.setVisible(true);
            String reference = dialog.getReference();
            if (reference == null) return; // cancelled/failed
            calculatedRun.setPaymentReference(reference);
        } else {
            calculatedRun.setPaymentReference(null);
        }

        calculatedRun.setStatus("PAID");
        boolean overwriting = calculatedRun.getPayrollId() > 0;
        boolean saved;
        if (overwriting) {
            saved = payrollDAO.updatePayrollRun(calculatedRun);
        } else {
            int newId = payrollDAO.insertPayrollRun(calculatedRun);
            saved = newId > 0;
            if (saved) calculatedRun.setPayrollId(newId);
        }

        if (saved) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Payroll processed for " + employee.getFullName() + "!\nNet Pay: $" + calculatedRun.getNetPay() +
                            "\n\nPrint pay stub now?",
                    "Payment Complete", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) {
                PayStubPrinter.printPayStub(calculatedRun, this);
            }
            calculatedRun = null;
            refreshHistory();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save payroll run. Check database connection.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshHistory() {
        historyList = payrollDAO.getHistory();
        historyModel.setRowCount(0);
        for (PayrollRun run : historyList) {
            historyModel.addRow(new Object[]{run.getEmployeeName(),
                    run.getPeriodStart() + " to " + run.getPeriodEnd(),
                    run.getRegularHours(), run.getOvertimeHours(), run.getWeekendHours(), run.getHolidayHours(),
                    "$" + run.getGrossPay(), "$" + run.getNetPay(), run.getPaymentMethod(), run.getStatus()});
        }
    }

    private void showHolidaysDialog() {
        List<Holiday> holidays = holidayDAO.getAll();
        DefaultListModel<Holiday> listModel = new DefaultListModel<>();
        for (Holiday h : holidays) listModel.addElement(h);
        JList<Holiday> list = new JList<>(listModel);

        JTextField dateField = new JTextField(10);
        JTextField nameField = new JTextField(15);
        JButton addBtn = UITheme.primaryButton("Add");
        JButton removeBtn = UITheme.dangerButton("Remove Selected");

        JPanel addRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addRow.add(new JLabel("Date (yyyy-MM-dd):"));
        addRow.add(dateField);
        addRow.add(new JLabel("Name:"));
        addRow.add(nameField);
        addRow.add(addBtn);

        addBtn.addActionListener(e -> {
            try {
                LocalDate date = LocalDate.parse(dateField.getText().trim());
                holidayDAO.addHoliday(date, nameField.getText().trim());
                listModel.addElement(new Holiday(date, nameField.getText().trim()));
                dateField.setText("");
                nameField.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Enter a valid date like 2026-12-25.", "Invalid Date", JOptionPane.WARNING_MESSAGE);
            }
        });
        removeBtn.addActionListener(e -> {
            Holiday sel = list.getSelectedValue();
            if (sel != null) {
                holidayDAO.deleteHoliday(sel.getHolidayId());
                listModel.removeElement(sel);
            }
        });

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.add(new JScrollPane(list), BorderLayout.CENTER);
        JPanel south = new JPanel(new BorderLayout());
        south.add(addRow, BorderLayout.NORTH);
        south.add(removeBtn, BorderLayout.SOUTH);
        panel.add(south, BorderLayout.SOUTH);
        panel.setPreferredSize(new Dimension(420, 350));

        JOptionPane.showMessageDialog(this, panel, "Manage Company Holidays", JOptionPane.PLAIN_MESSAGE);
    }
}