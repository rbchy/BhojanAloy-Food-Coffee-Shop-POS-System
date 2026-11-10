package com.bhojanaloy.service;

import com.bhojanaloy.config.EmailConfig;
import com.bhojanaloy.config.SmsConfig;
import com.bhojanaloy.util.SmtpMailSender;
import com.bhojanaloy.util.TwilioSmsSender;

import java.security.SecureRandom;

/**
 * Sends the customer's bill/receipt electronically instead of (or in
 * addition to) printing it — by Email or SMS, depending on what the
 * customer prefers.
 *
 * This now actually sends for real once you configure it:
 *  - Email: fill in your SMTP account in config/EmailConfig.java (works with
 *    Gmail + an App Password, or any provider with SMTPS on port 465). Sent
 *    via a small built-in SMTP client (util/SmtpMailSender.java) — no
 *    external mail library required.
 *  - SMS: fill in your Twilio account in config/SmsConfig.java. Sent via
 *    Twilio's plain HTTPS REST API (util/TwilioSmsSender.java) — no Twilio
 *    SDK jar required.
 * Until you fill those in, both channels fall back to a simulated send
 * (short delay + a fake "-SIM-" reference) so the app still works out of
 * the box with nothing configured.
 */
public class ReceiptDeliveryService {

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * @param channel     "EMAIL" or "SMS"
     * @param destination the email address or phone number to send to
     * @return a delivery reference (real if configured, otherwise simulated)
     * @throws Exception if a real send was attempted and failed (bad credentials,
     *                    no internet, provider rejected the message, etc.)
     */
    public String send(String channel, String destination, String billText) throws Exception {
        if ("EMAIL".equalsIgnoreCase(channel)) {
            if (EmailConfig.isConfigured()) {
                SmtpMailSender.send(destination, "Your Bhojan-Aloy Receipt", billText);
                return "MAIL-SENT-" + System.currentTimeMillis();
            }
            return simulate("MAIL");
        } else {
            if (SmsConfig.isConfigured()) {
                return TwilioSmsSender.send(destination, billText);
            }
            return simulate("SMS");
        }
    }

    private String simulate(String prefix) {
        try {
            Thread.sleep(500 + RANDOM.nextInt(600));
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        StringBuilder sb = new StringBuilder(prefix + "-SIM-");
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < 8; i++) sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        return sb.toString();
    }
}