package com.bhojanaloy.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Small, shared field-validation helpers used across the GUI forms (Menu
 * Items, Employees, Payroll edits, Checkout customer/loyalty fields) so
 * every screen rejects obviously-bad input the same way instead of each
 * dialog reinventing its own checks.
 */
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");
    // Accepts an optional leading + and 7-15 digits (covers local and international phone formats).
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{7,15}$");

    private ValidationUtil() {}

    public static boolean isNonEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }

    public static boolean isValidEmail(String s) {
        return s != null && EMAIL_PATTERN.matcher(s.trim()).matches();
    }

    public static boolean isValidPhone(String s) {
        return s != null && PHONE_PATTERN.matcher(s.trim().replaceAll("[\\s-]", "")).matches();
    }

    /** Parses a positive (> 0) decimal; returns null if blank/invalid/not positive. */
    public static BigDecimal parsePositiveDecimal(String s) {
        try {
            BigDecimal v = new BigDecimal(s.trim());
            return v.compareTo(BigDecimal.ZERO) > 0 ? v : null;
        } catch (Exception e) {
            return null;
        }
    }

    /** Parses a non-negative (>= 0) decimal; returns null if blank/invalid/negative. */
    public static BigDecimal parseNonNegativeDecimal(String s) {
        try {
            BigDecimal v = new BigDecimal(s.trim());
            return v.compareTo(BigDecimal.ZERO) >= 0 ? v : null;
        } catch (Exception e) {
            return null;
        }
    }

    /** Parses a non-negative integer; returns null if blank/invalid/negative. */
    public static Integer parseNonNegativeInt(String s) {
        try {
            int v = Integer.parseInt(s.trim());
            return v >= 0 ? v : null;
        } catch (Exception e) {
            return null;
        }
    }
}
