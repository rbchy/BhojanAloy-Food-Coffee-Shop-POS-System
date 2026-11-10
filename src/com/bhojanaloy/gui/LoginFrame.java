package com.bhojanaloy.gui;

import com.bhojanaloy.config.DBConnection;
import com.bhojanaloy.dao.UserDAO;
import com.bhojanaloy.model.User;
import com.bhojanaloy.util.ImageUtil;
import com.bhojanaloy.util.SessionManager;
import com.bhojanaloy.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/** First screen shown: authenticates against the users table. */
public class LoginFrame extends JFrame {

    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JLabel statusLabel = new JLabel(" ");
    private final UserDAO userDAO = new UserDAO();

    public LoginFrame() {
        super("Bhojan-Aloy | Login");
        UITheme.applyGlobalDefaults();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 600);
        setLocationRelativeTo(null);
        setResizable(false);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.CREAM);
        root.setBorder(new EmptyBorder(20, 40, 20, 40));

        // Everything (badge, tagline, username/password, buttons) lives in one
        // centered column, with stretchy glue above and below so the whole
        // block sits in the vertical middle of the window, not pinned to the top.
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UITheme.CREAM);
        content.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Circular badge-style illustration (images/login_banner.png). No real
        // photo is bundled (see images/README_IMAGES.txt) — falls back to the
        // plain text logo below if the file is ever missing or moved.
        ImageIcon banner = ImageUtil.loadScaledIcon("images/login_banner.png", 190, 190);
        if (banner != null) {
            JLabel bannerLabel = new JLabel(banner);
            bannerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            content.add(bannerLabel);
            content.add(Box.createVerticalStrut(14));
        } else {
            JLabel logo = new JLabel("☕  BHOJAN-ALOY", SwingConstants.CENTER);
            logo.setFont(new Font("Segoe UI", Font.BOLD, 24));
            logo.setForeground(UITheme.COFFEE_DARK);
            logo.setAlignmentX(Component.CENTER_ALIGNMENT);
            content.add(logo);
            content.add(Box.createVerticalStrut(6));

            JLabel tagline = new JLabel("Food & Coffee Shop POS System", SwingConstants.CENTER);
            tagline.setFont(UITheme.FONT_LABEL);
            tagline.setForeground(UITheme.COFFEE_MEDIUM);
            tagline.setAlignmentX(Component.CENTER_ALIGNMENT);
            content.add(tagline);
            content.add(Box.createVerticalStrut(18));
        }

        JLabel userLabel = new JLabel("Username", SwingConstants.CENTER);
        userLabel.setFont(UITheme.FONT_LABEL_B);
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        usernameField.setFont(UITheme.FONT_LABEL);
        usernameField.setMaximumSize(new Dimension(230, 30));
        usernameField.setHorizontalAlignment(JTextField.CENTER);
        usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel passLabel = new JLabel("Password", SwingConstants.CENTER);
        passLabel.setFont(UITheme.FONT_LABEL_B);
        passLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordField.setFont(UITheme.FONT_LABEL);
        passwordField.setMaximumSize(new Dimension(230, 30));
        passwordField.setHorizontalAlignment(JTextField.CENTER);
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton loginBtn = UITheme.primaryButton("LOG IN");
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(230, 38));
        loginBtn.addActionListener(this::attemptLogin);
        passwordField.addActionListener(this::attemptLogin);

        statusLabel.setForeground(UITheme.ACCENT_RED);
        statusLabel.setFont(UITheme.FONT_LABEL);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel hint = new JLabel("Default admin login: admin / admin123");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        hint.setForeground(UITheme.COFFEE_LIGHT);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton helpBtn = new JButton("❓ Help / সাহায্য");
        helpBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        helpBtn.setFocusPainted(false);
        helpBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        helpBtn.addActionListener(e -> new HelpDialog(this).setVisible(true));

        content.add(userLabel);
        content.add(Box.createVerticalStrut(4));
        content.add(usernameField);
        content.add(Box.createVerticalStrut(16));
        content.add(passLabel);
        content.add(Box.createVerticalStrut(4));
        content.add(passwordField);
        content.add(Box.createVerticalStrut(22));
        content.add(loginBtn);
        content.add(Box.createVerticalStrut(10));
        content.add(statusLabel);
        content.add(Box.createVerticalStrut(16));
        content.add(hint);
        content.add(Box.createVerticalStrut(10));
        content.add(helpBtn);

        JPanel centeringWrapper = new JPanel();
        centeringWrapper.setLayout(new BoxLayout(centeringWrapper, BoxLayout.Y_AXIS));
        centeringWrapper.setBackground(UITheme.CREAM);
        centeringWrapper.add(Box.createVerticalGlue());
        centeringWrapper.add(content);
        centeringWrapper.add(Box.createVerticalGlue());

        root.add(centeringWrapper, BorderLayout.CENTER);
        setContentPane(root);
    }

    private void attemptLogin(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter username and password.");
            return;
        }
        if (!DBConnection.testConnection()) {
            statusLabel.setText("Cannot reach database. Check MySQL connection settings.");
            return;
        }

        User user;
        try {
            user = userDAO.authenticate(username, password);
        } catch (Exception ex) {
            // Defensive catch-all: a bad/placeholder salt, DB hiccup, etc.
            // should show a message, not crash the Swing event thread.
            ex.printStackTrace();
            statusLabel.setText("Login failed due to an internal error. See console log.");
            return;
        }

        if (user == null) {
            statusLabel.setText("Invalid username or password.");
            return;
        }

        SessionManager.login(user);
        dispose();
        SwingUtilities.invokeLater(() -> new MainDashboard().setVisible(true));
    }
}