package com.bhojanaloy.gui;

import com.bhojanaloy.dao.EmployeeDAO;
import com.bhojanaloy.dao.TimeClockDAO;
import com.bhojanaloy.model.Employee;
import com.bhojanaloy.model.TimeClock;
import com.bhojanaloy.model.User;
import com.bhojanaloy.util.SessionManager;
import com.bhojanaloy.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Clock-in / clock-out screen. Admins and Managers can clock any employee
 * in/out (handy since not every staff member has their own login); Cashiers
 * are limited to their own linked employee record. Also shows who's
 * currently clocked in across the whole shop.
 */
public class TimeClockPanel extends JPanel {

    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final TimeClockDAO timeClockDAO = new TimeClockDAO();

    private final JComboBox<Employee> employeeBox = new JComboBox<>();
    private final JLabel statusLabel = new JLabel(" ");
    private final JButton clockInBtn = UITheme.successButton("Clock In");
    private final JButton clockOutBtn = UITheme.dangerButton("Clock Out");

    private final DefaultTableModel historyModel = new DefaultTableModel(
            new Object[]{"Date", "Clock In", "Clock Out", "Total Hours"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final DefaultTableModel clockedInModel = new DefaultTableModel(
            new Object[]{"Employee", "Designation", "Clocked In Since"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };

    public TimeClockPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.CREAM);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        add(UITheme.heading("Employee Time Clock"), BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1, 2, 15, 0));
        center.setBackground(UITheme.CREAM);
        center.add(buildClockCard());
        center.add(buildClockedInCard());
        add(center, BorderLayout.CENTER);

        loadEmployees();
        refreshStatus();
        refreshClockedInList();
    }

    private JPanel buildClockCard() {
        JPanel card = UITheme.sectionPanel();
        card.setLayout(new BorderLayout(10, 10));

        User currentUser = SessionManager.getCurrentUser();
        boolean canSelectAnyEmployee = currentUser.isManagerOrAbove();

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        top.add(new JLabel("Employee:"));
        top.add(employeeBox);
        employeeBox.setEnabled(canSelectAnyEmployee);
        employeeBox.addActionListener(e -> refreshStatus());

        JPanel middle = new JPanel();
        middle.setLayout(new BoxLayout(middle, BoxLayout.Y_AXIS));
        middle.setOpaque(false);
        statusLabel.setFont(UITheme.FONT_HEADING);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonRow.setOpaque(false);
        clockInBtn.addActionListener(e -> doClockIn());
        clockOutBtn.addActionListener(e -> doClockOut());
        buttonRow.add(clockInBtn);
        buttonRow.add(clockOutBtn);

        middle.add(Box.createVerticalStrut(20));
        middle.add(statusLabel);
        middle.add(Box.createVerticalStrut(10));
        middle.add(buttonRow);

        JTable historyTable = new JTable(historyModel);
        UITheme.styleTable(historyTable);
        JScrollPane historyScroll = new JScrollPane(historyTable);
        historyScroll.setBorder(BorderFactory.createTitledBorder("Recent Shifts"));

        card.add(top, BorderLayout.NORTH);
        card.add(middle, BorderLayout.CENTER);
        card.add(historyScroll, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildClockedInCard() {
        JPanel card = UITheme.sectionPanel();
        card.setLayout(new BorderLayout(10, 10));
        card.add(UITheme.heading("Currently Clocked In"), BorderLayout.NORTH);

        JTable table = new JTable(clockedInModel);
        UITheme.styleTable(table);
        card.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton refreshBtn = UITheme.secondaryButton("Refresh");
        refreshBtn.addActionListener(e -> refreshClockedInList());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.setOpaque(false);
        south.add(refreshBtn);
        card.add(south, BorderLayout.SOUTH);
        return card;
    }

    private void loadEmployees() {
        employeeBox.removeAllItems();
        User currentUser = SessionManager.getCurrentUser();
        List<Employee> employees = employeeDAO.getAllActive();
        if (currentUser.isManagerOrAbove()) {
            for (Employee e : employees) employeeBox.addItem(e);
        } else if (currentUser.getEmployeeId() != null) {
            for (Employee e : employees) {
                if (e.getEmployeeId() == currentUser.getEmployeeId()) employeeBox.addItem(e);
            }
        }
    }

    private Employee getSelectedEmployee() {
        return (Employee) employeeBox.getSelectedItem();
    }

    private void refreshStatus() {
        Employee emp = getSelectedEmployee();
        if (emp == null) {
            statusLabel.setText("No employee selected.");
            clockInBtn.setEnabled(false);
            clockOutBtn.setEnabled(false);
            return;
        }
        TimeClock open = timeClockDAO.getOpenShift(emp.getEmployeeId());
        if (open != null) {
            statusLabel.setForeground(UITheme.ACCENT_GREEN);
            statusLabel.setText(emp.getFullName() + " — Clocked IN since " + open.getClockIn());
            clockInBtn.setEnabled(false);
            clockOutBtn.setEnabled(true);
        } else {
            statusLabel.setForeground(UITheme.ACCENT_RED);
            statusLabel.setText(emp.getFullName() + " — Not clocked in");
            clockInBtn.setEnabled(true);
            clockOutBtn.setEnabled(false);
        }
        refreshHistory(emp.getEmployeeId());
    }

    private void refreshHistory(int employeeId) {
        historyModel.setRowCount(0);
        for (TimeClock tc : timeClockDAO.getRecentHistory(employeeId, 15)) {
            historyModel.addRow(new Object[]{tc.getWorkDate(), tc.getClockIn(),
                    tc.getClockOut() != null ? tc.getClockOut() : "—",
                    tc.getTotalHours() != null ? tc.getTotalHours() : "—"});
        }
    }

    private void refreshClockedInList() {
        clockedInModel.setRowCount(0);
        for (TimeClock tc : timeClockDAO.getAllCurrentlyClockedIn()) {
            clockedInModel.addRow(new Object[]{tc.getEmployeeName(), "", tc.getClockIn()});
        }
    }

    private void doClockIn() {
        Employee emp = getSelectedEmployee();
        if (emp == null) return;
        if (timeClockDAO.clockIn(emp.getEmployeeId())) {
            JOptionPane.showMessageDialog(this, emp.getFullName() + " clocked in at " + java.time.LocalDateTime.now(),
                    "Clocked In", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Could not clock in — already clocked in?", "Error", JOptionPane.WARNING_MESSAGE);
        }
        refreshStatus();
        refreshClockedInList();
    }

    private void doClockOut() {
        Employee emp = getSelectedEmployee();
        if (emp == null) return;
        if (timeClockDAO.clockOut(emp.getEmployeeId())) {
            JOptionPane.showMessageDialog(this, emp.getFullName() + " clocked out at " + java.time.LocalDateTime.now(),
                    "Clocked Out", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Could not clock out — not currently clocked in?", "Error", JOptionPane.WARNING_MESSAGE);
        }
        refreshStatus();
        refreshClockedInList();
    }
}
