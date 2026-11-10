package com.bhojanaloy.util;

import com.bhojanaloy.config.SmsConfig;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

/**
 * Sends a real SMS via Twilio's REST API over plain HTTPS, using only
 * {@code java.net.http.HttpClient} — no Twilio SDK jar needed. Requires a
 * Twilio account configured in {@link SmsConfig}; throws if the send fails.
 */
public class TwilioSmsSender {

    private TwilioSmsSender() {}

    public static String send(String toPhone, String body) throws Exception {
        String url = "https://api.twilio.com/2010-04-01/Accounts/" + SmsConfig.TWILIO_ACCOUNT_SID + "/Messages.json";
        String form = "To=" + URLEncoder.encode(toPhone, StandardCharsets.UTF_8)
                + "&From=" + URLEncoder.encode(SmsConfig.TWILIO_FROM_NUMBER, StandardCharsets.UTF_8)
                + "&Body=" + URLEncoder.encode(body, StandardCharsets.UTF_8);
        String auth = Base64.getEncoder().encodeToString(
                (SmsConfig.TWILIO_ACCOUNT_SID + ":" + SmsConfig.TWILIO_AUTH_TOKEN).getBytes(StandardCharsets.UTF_8));

        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Authorization", "Basic " + auth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return extractSid(response.body());
        }
        throw new RuntimeException("Twilio SMS failed (HTTP " + response.statusCode() + "): " + response.body());
    }

    /** Crude extraction of the "sid" field from Twilio's JSON response, without needing a JSON library. */
    private static String extractSid(String jsonBody) {
        String key = "\"sid\":\"";
        int idx = jsonBody.indexOf(key);
        if (idx < 0) return "SENT";
        int start = idx + key.length();
        int end = jsonBody.indexOf('"', start);
        return end > start ? jsonBody.substring(start, end) : "SENT";
    }
}