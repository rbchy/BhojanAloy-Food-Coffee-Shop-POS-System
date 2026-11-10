package com.bhojanaloy.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Central color palette & widget factory so every screen in Bhojan-Aloy
 * shares a consistent, warm "coffee shop" look and feel.
 */
public class UITheme {

    public static final Color COFFEE_DARK   = new Color(0x3B, 0x24, 0x1D);
    public static final Color COFFEE_MEDIUM = new Color(0x6F, 0x4E, 0x37);
    public static final Color COFFEE_LIGHT  = new Color(0xA9, 0x74, 0x59);
    public static final Color CREAM         = new Color(0xFA, 0xF3, 0xE8);
    public static final Color CREAM_DARKER  = new Color(0xF0, 0xE4, 0xD3);
    public static final Color ACCENT_ORANGE = new Color(0xE8, 0x7A, 0x41);
    public static final Color ACCENT_GREEN  = new Color(0x4C, 0xAF, 0x50);
    public static final Color ACCENT_RED    = new Color(0xD9, 0x43, 0x43);
    public static final Color TEXT_DARK     = new Color(0x2B, 0x1B, 0x14);
    public static final Color WHITE         = Color.WHITE;

    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_LABEL   = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_LABEL_B = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BUTTON  = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_MONO    = new Font("Consolas", Font.PLAIN, 13);

    private UITheme() {}

    public static JButton primaryButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, ACCENT_ORANGE, WHITE);
        return b;
    }

    public static JButton secondaryButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, COFFEE_MEDIUM, WHITE);
        return b;
    }

    public static JButton dangerButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, ACCENT_RED, WHITE);
        return b;
    }

    public static JButton successButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, ACCENT_GREEN, WHITE);
        return b;
    }

    private static void styleButton(JButton b, Color bg, Color fg) {
        b.setFont(FONT_BUTTON);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10, 22, 10, 22));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);
        b.setBorderPainted(false);
        addHoverEffect(b, bg);
    }

    /** Darkens a button's background on mouse-over and restores it on mouse-out, so every
     *  themed button in the app (menu buttons, category filters, dialog buttons, sidebar
     *  navigation, etc.) gives clear visual feedback that it's clickable.
     *
     *  Forces {@link BasicButtonUI} first: on macOS, the native Aqua look-and-feel largely
     *  ignores JButton.setBackground() (a well-known Swing quirk), so without this the
     *  hover color change would silently do nothing on a Mac even though the exact same
     *  code works fine on Windows/Linux. BasicButtonUI paints background color like every
     *  other platform does. */
    public static void addHoverEffect(JButton b, Color normalColor) {
        b.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBackground(normalColor);
        Color hoverColor = normalColor.darker();
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (b.isEnabled()) b.setBackground(hoverColor);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(normalColor);
            }
        });
    }

    public static JLabel heading(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_HEADING);
        l.setForeground(COFFEE_DARK);
        return l;
    }

    public static JLabel title(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_TITLE);
        l.setForeground(COFFEE_DARK);
        return l;
    }

    public static JPanel sectionPanel() {
        JPanel p = new JPanel();
        p.setBackground(WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CREAM_DARKER, 1),
                new EmptyBorder(16, 16, 16, 16)));
        return p;
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(28);
        table.setFont(FONT_LABEL);
        table.setSelectionBackground(ACCENT_ORANGE);
        table.setSelectionForeground(WHITE);
        table.setGridColor(CREAM_DARKER);
        table.getTableHeader().setFont(FONT_LABEL_B);
        table.getTableHeader().setBackground(COFFEE_MEDIUM);
        table.getTableHeader().setForeground(WHITE);
        table.setFillsViewportHeight(true);
    }

    public static void applyGlobalDefaults() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }
        UIManager.put("Panel.background", CREAM);
        UIManager.put("OptionPane.background", CREAM);
        UIManager.put("control", CREAM);
    }
}