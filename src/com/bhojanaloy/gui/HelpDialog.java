package com.bhojanaloy.gui;

import com.bhojanaloy.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/** In-app help / user guide, with a dropdown to pick any of five supported languages. */
public class HelpDialog extends JDialog {

    private final JEditorPane editorPane = new JEditorPane();
    private final JComboBox<String> languageBox;
    private final Map<String, String> languages = new LinkedHashMap<>();

    public HelpDialog(Frame owner) {
        super(owner, "Bhojan-Aloy Help", false);
        setSize(700, 650);
        setLocationRelativeTo(owner);

        languages.put("English", HelpContent.ENGLISH_HTML);
        languages.put("বাংলা (Bangla)", HelpContent.BANGLA_HTML);
        languages.put("Español (Spanish)", HelpContent.SPANISH_HTML);
        languages.put("हिंदी (Hindi)", HelpContent.HINDI_HTML);
        languages.put("العربية (Arabic)", HelpContent.ARABIC_HTML);
        languageBox = new JComboBox<>(languages.keySet().toArray(new String[0]));

        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(UITheme.CREAM);
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel top = new JPanel(new BorderLayout(10, 0));
        top.setOpaque(false);
        JLabel title = UITheme.heading("Help / Language");

        JPanel languagePicker = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        languagePicker.setOpaque(false);
        languagePicker.add(new JLabel("Language:"));
        languageBox.setFont(UITheme.FONT_LABEL);
        languageBox.addActionListener(e -> showLanguage((String) languageBox.getSelectedItem()));
        languagePicker.add(languageBox);

        top.add(title, BorderLayout.WEST);
        top.add(languagePicker, BorderLayout.EAST);

        editorPane.setContentType("text/html");
        editorPane.setEditable(false);
        showLanguage("English");
        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JButton closeBtn = UITheme.secondaryButton("Close");
        closeBtn.addActionListener(e -> dispose());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        south.setOpaque(false);
        south.add(closeBtn);

        root.add(top, BorderLayout.NORTH);
        root.add(scrollPane, BorderLayout.CENTER);
        root.add(south, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void showLanguage(String languageName) {
        String html = languages.get(languageName);
        if (html == null) return;
        editorPane.setText(html);
        editorPane.setCaretPosition(0);
    }
}
