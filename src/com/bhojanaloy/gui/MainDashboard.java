package com.bhojanaloy.gui;

import com.bhojanaloy.util.SessionManager;
import com.bhojanaloy.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Main application shell: sidebar navigation + CardLayout content area. */
public class MainDashboard extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    public MainDashboard() {
        super("Bhojan-Aloy | " + SessionManager.getCurrentUser().getFullName() +
                " (" + SessionManager.getCurrentUser().getRole() + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildTopBar(), BorderLayout.NORTH);

        contentPanel.setBackground(UITheme.CREAM);
        contentPanel.add(new POSPanel(), "POS");
        contentPanel.add(new StationDisplayPanel(), "STATIONS");
        contentPanel.add(new MenuManagementPanel(), "MENU");
        contentPanel.add(new InventoryPanel(), "INVENTORY");
        contentPanel.add(new RecipePanel(), "RECIPES");
        contentPanel.add(new DeliveryPanel(), "DELIVERY");
        contentPanel.add(new TimeClockPanel(), "TIMECLOCK");
        contentPanel.add(new EmployeePanel(), "EMPLOYEES");
        contentPanel.add(new PayrollPanel(), "PAYROLL");
        contentPanel.add(new ReportsPanel(), "REPORTS");
        contentPanel.add(new SupplierPanel(), "SUPPLIERS");

        root.add(contentPanel, BorderLayout.CENTER);
        setContentPane(root);
        cardLayout.show(contentPanel, "POS");
    }

    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(UITheme.COFFEE_DARK);
        top.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel title = new JLabel("BHOJAN-ALOY  ☕");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(UITheme.WHITE);

        JLabel user = new JLabel(SessionManager.getCurrentUser().getFullName() +
                "  |  " + SessionManager.getCurrentUser().getRole());
        user.setFont(UITheme.FONT_LABEL);
        user.setForeground(UITheme.CREAM);

        JButton helpBtn = new JButton("❓ Help");
        helpBtn.setFocusPainted(false);
        helpBtn.setBackground(UITheme.ACCENT_ORANGE);
        helpBtn.setForeground(UITheme.WHITE);
        helpBtn.setBorderPainted(false);
        helpBtn.addActionListener(e -> new HelpDialog(this).setVisible(true));

        JButton logout = new JButton("Log Out");
        logout.setFocusPainted(false);
        logout.setBackground(UITheme.ACCENT_RED);
        logout.setForeground(UITheme.WHITE);
        logout.setBorderPainted(false);
        logout.addActionListener(e -> {
            SessionManager.logout();
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        right.setOpaque(false);
        right.add(user);
        right.add(helpBtn);
        right.add(logout);

        top.add(title, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);
        return top;
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.COFFEE_MEDIUM);
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setBorder(new EmptyBorder(20, 0, 20, 0));

        boolean isCashier = "CASHIER".equalsIgnoreCase(SessionManager.getCurrentUser().getRole());

        sidebar.add(navButton("🛒 Point of Sale", "POS"));
        sidebar.add(navButton("🧑‍🍳 Station Display", "STATIONS"));
        sidebar.add(navButton("🍽 Menu Items", "MENU"));
        sidebar.add(navButton("📖 Recipes", "RECIPES"));
        sidebar.add(navButton("📦 Inventory", "INVENTORY"));
        sidebar.add(navButton("🛵 Delivery", "DELIVERY"));
        sidebar.add(navButton("⏱ Time Clock", "TIMECLOCK"));
        if (!isCashier) {
            sidebar.add(navButton("👥 Employees", "EMPLOYEES"));
            sidebar.add(navButton("💰 Payroll", "PAYROLL"));
            sidebar.add(navButton("🧺 Suppliers", "SUPPLIERS"));
            sidebar.add(navButton("📊 Reports", "REPORTS"));
        }
        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private JButton navButton(String label, String cardName) {
        JButton btn = new JButton(label);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(210, 46));
        btn.setPreferredSize(new Dimension(210, 46));
        btn.setFont(UITheme.FONT_LABEL_B);
        btn.setForeground(UITheme.WHITE);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> cardLayout.show(contentPanel, cardName));
        // Uses the same MouseListener-based hover as the rest of the app (see
        // UITheme.addHoverEffect) instead of a rollover ChangeListener — on macOS the
        // Aqua look-and-feel largely ignores JButton background changes driven by the
        // button model's rollover state, so the old approach silently did nothing there.
        UITheme.addHoverEffect(btn, UITheme.COFFEE_MEDIUM);
        return btn;
    }
}