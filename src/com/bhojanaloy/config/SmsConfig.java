package com.bhojanaloy.config;

/**
 * Twilio account settings for REAL SMS receipt delivery (via Twilio's REST
 * API over plain HTTPS — no external SDK/jar needed, see
 * util/TwilioSmsSender.java). Set BHOJAN_TWILIO_ACCOUNT_SID, BHOJAN_TWILIO_AUTH_TOKEN
 * and BHOJAN_TWILIO_FROM_NUMBER in the environment to send real text messages; without them the app keeps
 * using a simulated send (fake reference, no SMS actually leaves) so nothing
 * breaks before you sign up.
 *
 * To go live: create a free/paid account at https://www.twilio.com, grab
 * your Account SID and Auth Token from the Twilio Console dashboard, and
 * either buy a Twilio phone number or use their trial number as
 * BHOJAN_TWILIO_FROM_NUMBER (must be in +countrycode format, e.g. +8801XXXXXXXXX).
 */
public class SmsConfig {

    // Real values come from the environment, so no Twilio secret is ever committed.
    public static final String TWILIO_ACCOUNT_SID = EmailConfig.env("BHOJAN_TWILIO_ACCOUNT_SID", "your-twilio-account-sid");
    public static final String TWILIO_AUTH_TOKEN = EmailConfig.env("BHOJAN_TWILIO_AUTH_TOKEN", "your-twilio-auth-token");
    public static final String TWILIO_FROM_NUMBER = EmailConfig.env("BHOJAN_TWILIO_FROM_NUMBER", "+10000000000");

    private SmsConfig() {}

    /** True once you've replaced the placeholder Account SID/Auth Token above with real values. */
    public static boolean isConfigured() {
        return !TWILIO_ACCOUNT_SID.equals("your-twilio-account-sid")
                && !TWILIO_AUTH_TOKEN.equals("your-twilio-auth-token")
                && !TWILIO_ACCOUNT_SID.isEmpty() && !TWILIO_AUTH_TOKEN.isEmpty();
    }
}