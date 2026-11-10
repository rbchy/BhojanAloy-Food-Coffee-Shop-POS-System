package com.bhojanaloy.service;

import com.bhojanaloy.model.Payment;

import java.math.BigDecimal;
import java.security.SecureRandom;

/**
 * Simulated payment gateway for Card and Mobile Banking (bKash/Nagad/Rocket)
 * checkouts. There is no live merchant account wired up yet, so this class
 * stands in for a real gateway SDK call (e.g. SSLCommerz, Stripe, bKash
 * Checkout API) — it generates a realistic-looking transaction reference and
 * simulates network latency + an occasional decline, so the rest of the app
 * (receipts, reports, delivery flow) can be built and tested end-to-end.
 *
 * TO GO LIVE: replace the body of {@link #processPayment} with a real call
 * to your chosen gateway's SDK/REST API, using their returned transaction ID
 * as the reference number and their response code to set the Payment status.
 */
public class PaymentService {

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Simulates sending a payment request to the gateway.
     * @param method    CARD or MOBILE_BANKING
     * @param provider  e.g. "Visa", "Mastercard", "bKash", "Nagad", "Rocket"
     * @param amount    amount to charge
     * @param accountOrCardRef  last 4 digits of card, or mobile banking account number entered by the customer
     * @return a Payment with status SUCCESS or FAILED and a generated reference number
     */
    public Payment processPayment(String method, String provider, BigDecimal amount, String accountOrCardRef) {
        simulateNetworkDelay();

        Payment payment = new Payment();
        payment.setMethod(method);
        payment.setProvider(provider);
        payment.setAmount(amount);

        // Simulate a ~95% approval rate so the happy path is the common case in demos/testing.
        boolean approved = RANDOM.nextInt(100) < 95;
        payment.setStatus(approved ? "SUCCESS" : "FAILED");
        payment.setReferenceNumber(generateReferenceNumber(provider));
        return payment;
    }

    private void simulateNetworkDelay() {
        try {
            Thread.sleep(600 + RANDOM.nextInt(700)); // 0.6–1.3s, feels like a real gateway round-trip
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    private String generateReferenceNumber(String provider) {
        String prefix = provider == null ? "TXN" : provider.substring(0, Math.min(3, provider.length())).toUpperCase();
        StringBuilder sb = new StringBuilder(prefix);
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < 10; i++) sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        return sb.toString();
    }
}
