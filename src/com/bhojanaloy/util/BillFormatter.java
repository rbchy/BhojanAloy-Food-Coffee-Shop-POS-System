package com.bhojanaloy.util;

import com.bhojanaloy.model.Order;
import com.bhojanaloy.model.OrderItem;

/** Renders an Order as a plain-text, monospaced-friendly bill — used for the on-screen bill preview, printing, and email/SMS send. */
public class BillFormatter {

    private BillFormatter() {}

    public static String format(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("        BHOJAN-ALOY\n");
        sb.append("     Food & Coffee Shop\n");
        sb.append(divider());
        sb.append(String.format("Order #: %s%n", order.getOrderNumber()));
        sb.append(String.format("Date   : %s%n", order.getOrderDate()));
        sb.append(String.format("Type   : %s (%s)%n", order.getOrderType(), order.getOrderSource()));
        if (order.getCustomerName() != null && !order.getCustomerName().isEmpty()) {
            sb.append(String.format("Customer: %s%n", order.getCustomerName()));
            if (order.getCustomerPhone() != null) sb.append(String.format("Phone   : %s%n", order.getCustomerPhone()));
            if (order.isDelivery() && order.getCustomerAddress() != null) {
                sb.append(String.format("Address : %s%n", order.getCustomerAddress()));
            }
        }
        sb.append(divider());

        if (order.getItems() != null) {
            for (OrderItem oi : order.getItems()) {
                sb.append(String.format("%-22s x%-3d %8s%n", trim(oi.getItemName(), 22), oi.getQuantity(), "$" + oi.getLineTotal()));
            }
        }
        sb.append(divider());
        if (order.getLoyaltyRewardItem() != null && !order.getLoyaltyRewardItem().isEmpty()) {
            sb.append(String.format("Loyalty Reward: %s — FREE (50 pts redeemed)%n", order.getLoyaltyRewardItem()));
        }
        sb.append(String.format("%-22s %14s%n", "Subtotal", "$" + order.getSubtotal()));
        sb.append(String.format("%-22s %14s%n", "Discount", "-$" + order.getDiscount()));
        sb.append(String.format("%-22s %14s%n", "Tax", "$" + order.getTaxAmount()));
        sb.append(String.format("%-22s %14s%n", "TOTAL", "$" + order.getTotalAmount()));
        sb.append(String.format("Payment: %s%n", order.getPaymentMethod()));
        sb.append(divider());
        sb.append("   Thank you for visiting Bhojan-Aloy!\n");
        sb.append("        Please come again.\n");
        return sb.toString();
    }

    private static String divider() {
        return "------------------------------------------\n";
    }

    private static String trim(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
