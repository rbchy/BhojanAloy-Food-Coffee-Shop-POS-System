package com.bhojanaloy.util;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Loads item photos from disk for the POS/menu screens. Put real photos in
 * the project's images/ folder (see images/README_IMAGES.txt) and set each
 * MenuItem's "Image Path" (e.g. images/espresso.jpg) via the Menu
 * Management screen. If no photo is set/found, a large category emoji is
 * shown instead so every item still looks visually distinct without
 * requiring real photography up front.
 */
public class ImageUtil {

    private ImageUtil() {}

    /** Returns a scaled ImageIcon for the given path, or null if missing/unreadable. */
    public static ImageIcon loadScaledIcon(String imagePath, int width, int height) {
        if (imagePath == null || imagePath.trim().isEmpty()) return null;
        try {
            File file = new File(imagePath);
            if (!file.exists()) {
                // also try resolving relative to the app's working directory
                file = new File(System.getProperty("user.dir"), imagePath);
            }
            if (!file.exists()) return null;
            Image img = new ImageIcon(file.getAbsolutePath()).getImage();
            Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception e) {
            return null;
        }
    }

    /** A representative food emoji for a category/station name, used as a fallback "image". */
    public static String emojiFor(String categoryOrStationName) {
        if (categoryOrStationName == null) return "🍴";
        String s = categoryOrStationName.toLowerCase();
        if (s.contains("coffee")) return "☕";
        if (s.contains("bagel")) return "🥯";
        if (s.contains("donut") || s.contains("pastry") || s.contains("bakery")) return "🍩";
        if (s.contains("sandwich")) return "🥪";
        if (s.contains("beverage") || s.contains("juice") || s.contains("smoothie")) return "🥤";
        if (s.contains("tea")) return "🍵";
        if (s.contains("cake")) return "🍰";
        if (s.contains("pizza")) return "🍕";
        if (s.contains("burger")) return "🍔";
        return "🍴";
    }

    /** Builds a label showing either the real item photo (if available) or a large emoji fallback. */
    public static JLabel buildItemImageLabel(String imagePath, String categoryOrStationName, int size) {
        ImageIcon icon = loadScaledIcon(imagePath, size, size);
        JLabel label = new JLabel();
        label.setHorizontalAlignment(SwingConstants.CENTER);
        if (icon != null) {
            label.setIcon(icon);
        } else {
            label.setText(emojiFor(categoryOrStationName));
            label.setFont(new Font("Segoe UI Emoji", Font.PLAIN, (int) (size * 0.7)));
        }
        return label;
    }
}
