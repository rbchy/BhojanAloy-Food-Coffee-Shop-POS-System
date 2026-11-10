package com.bhojanaloy.util;

import com.bhojanaloy.model.Order;
import com.bhojanaloy.model.OrderItem;
import com.bhojanaloy.service.POSService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** The same text is printed, shown on screen, and emailed / texted to the customer. */
class BillFormatterTest {

    private static Order deliveryOrder() {
        Order o = new Order();
        o.setOrderNumber("ORD-20261109-120000000-01");
        o.setOrderType("DELIVERY");
        o.setOrderSource("PHONE");
        o.setCustomerName("Nusrat");
        o.setCustomerPhone("01712345678");
        o.setCustomerAddress("House 5, Road 2, Dhanmondi");
        o.setPaymentMethod("CASH");
        o.setItems(List.of(new OrderItem(1, "Cappuccino", 2, new BigDecimal("3.50")),
                new OrderItem(2, "Chicken Club Sandwich With Extra Cheese", 1, new BigDecimal("7.95"))));
        o.setDiscount(new BigDecimal("1.00"));
        o.recalculateTotals(POSService.TAX_RATE);
        return o;
    }

    @Test
    void theBillShowsEveryLineAndTheTotals() {
        String bill = BillFormatter.format(deliveryOrder());
        assertTrue(bill.contains("ORD-20261109-120000000-01"));
        assertTrue(bill.contains("Cappuccino") && bill.contains("$7.00"), bill);
        assertTrue(bill.contains("Subtotal") && bill.contains("$14.95"), bill);
        assertTrue(bill.contains("-$1.00"), bill);
        assertTrue(bill.contains("$1.12"), "tax 8% of 13.95 = 1.116 -> 1.12\n" + bill);
        assertTrue(bill.contains("$15.07"), bill);
    }

    @Test
    void longItemNamesAreCutSoColumnsStayAligned() {
        String bill = BillFormatter.format(deliveryOrder());
        assertFalse(bill.contains("Chicken Club Sandwich With Extra Cheese"));
        assertTrue(bill.contains("Chicken Club Sandwich…"), bill);
        for (String line : bill.split("\n")) {
            assertTrue(line.length() <= 46, "line too wide for a receipt printer: '" + line + "'");
        }
    }

    @Test
    void theAddressIsOnlyPrintedForDeliveries() {
        Order o = deliveryOrder();
        assertTrue(BillFormatter.format(o).contains("Dhanmondi"));
        o.setOrderType("PICKUP");
        assertFalse(BillFormatter.format(o).contains("Dhanmondi"));
    }

    @Test
    void aLoyaltyRewardIsShown() {
        Order o = deliveryOrder();
        o.setLoyaltyRewardItem("Cappuccino");
        assertTrue(BillFormatter.format(o).contains("Loyalty Reward: Cappuccino"));
    }
}
