package com.bhojanaloy;

import com.bhojanaloy.gui.LoginFrame;
import com.bhojanaloy.util.UITheme;

import javax.swing.*;

/**
 * BHOJAN-ALOY — Food & Coffee Shop POS System
 * Entry point: launches the login screen. Run this class from Eclipse
 * (Run As > Java Application) after importing the database. MySQL credentials
 * come from DB_URL / DB_USER / DB_PASSWORD or db.properties (see DBConnection).
 */
public class Main {
    public static void main(String[] args) {
        UITheme.applyGlobalDefaults();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
