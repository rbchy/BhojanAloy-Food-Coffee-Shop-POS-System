package com.bhojanaloy.config;

/**
 * SMTP settings for REAL receipt/announcement emails (no external mail
 * library needed — see util/SmtpMailSender.java). Set BHOJAN_SMTP_USERNAME and
 * BHOJAN_SMTP_APP_PASSWORD in the environment to send real emails; without them
 * the app will keep using a simulated send (a fake delivery reference, no
 * email actually leaves the building) so nothing breaks before you configure it.
 *
 * For Gmail: turn on 2-Step Verification on the account, then create an
 * "App Password" (Google Account -> Security -> App passwords) and use that
 * 16-character password as BHOJAN_SMTP_APP_PASSWORD — NOT your normal Gmail password. Other
 * providers (Outlook, a business email host, etc.) work too as long as they
 * support SMTPS on port 465 with username/password (AUTH LOGIN) — just
 * change SMTP_HOST/SMTP_PORT to match your provider's settings.
 */
public class EmailConfig {

    public static final String SMTP_HOST = "smtp.gmail.com";
    public static final int SMTP_PORT = 465; // implicit TLS/SSL
    // Real values come from the environment, so no address or app password is ever committed.
    public static final String SMTP_USERNAME = env("BHOJAN_SMTP_USERNAME", "your-email@gmail.com");
    public static final String SMTP_APP_PASSWORD = env("BHOJAN_SMTP_APP_PASSWORD", "your-16-char-app-password");
    public static final String FROM_NAME = "Bhojan-Aloy";

    private EmailConfig() {}

    static String env(String name, String placeholder) {
        String v = System.getenv(name);
        return v != null && !v.trim().isEmpty() ? v.trim() : placeholder;
    }

    /** True once you've replaced both placeholder values above with real credentials. */
    public static boolean isConfigured() {
        return !SMTP_USERNAME.equals("your-email@gmail.com")
                && !SMTP_APP_PASSWORD.equals("your-16-char-app-password")
                && !SMTP_USERNAME.isEmpty() && !SMTP_APP_PASSWORD.isEmpty();
    }
}