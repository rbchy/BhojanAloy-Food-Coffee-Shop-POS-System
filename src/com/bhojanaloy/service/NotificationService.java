package com.bhojanaloy.service;

import com.bhojanaloy.dao.CustomerDAO;
import com.bhojanaloy.model.Customer;
import com.bhojanaloy.model.MenuItem;

import java.util.List;

/**
 * Announces new menu items to registered loyalty customers by email. Reuses
 * {@link ReceiptDeliveryService}'s simulated Email/SMS send — no live email
 * provider is configured yet (see ReceiptDeliveryService's class comment for
 * how to go live with real JavaMail/SendGrid/etc).
 */
public class NotificationService {

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final ReceiptDeliveryService deliveryService = new ReceiptDeliveryService();

    /**
     * Emails every registered customer who has an email on file about a
     * newly-added menu item. Returns how many customers were notified.
     * Safe to call with zero registered customers (just does nothing).
     */
    public int notifyNewItem(MenuItem item) {
        List<Customer> recipients = customerDAO.getAllWithEmail();
        String body = buildAnnouncement(item);
        int sent = 0;
        for (Customer c : recipients) {
            try {
                deliveryService.send("EMAIL", c.getEmail(), body);
                sent++;
            } catch (Exception e) {
                e.printStackTrace(); // one bad address shouldn't stop the rest of the batch
            }
        }
        return sent;
    }

    private String buildAnnouncement(MenuItem item) {
        return "Good news from Bhojan-Aloy!\n\n" +
                "We just added a new item to our menu: " + item.getItemName() +
                (item.getDescription() != null && !item.getDescription().isEmpty() ? " — " + item.getDescription() : "") +
                "\nPrice: $" + item.getPrice() +
                "\n\nCome try it next time you visit, and don't forget — every order earns you a loyalty " +
                "point, and 50 points gets you a free item!\n\n— Bhojan-Aloy, Food & Coffee Shop";
    }
}
