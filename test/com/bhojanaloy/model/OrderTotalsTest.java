package com.bhojanaloy.model;

import com.bhojanaloy.service.POSService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Order.recalculateTotals is what the receipt, the payment amount and the orders row are built from. */
class OrderTotalsTest {

    private static Order order(BigDecimal discount, OrderItem... items) {
        Order o = new Order();
        o.setItems(List.of(items));
        o.setDiscount(discount);
        o.recalculateTotals(POSService.TAX_RATE);
        return o;
    }

    private static OrderItem item(int id, String name, int qty, String price) {
        return new OrderItem(id, name, qty, new BigDecimal(price));
    }

    @Test
    void subtotalTaxAndTotalForATypicalCart() {
        Order o = order(BigDecimal.ZERO, item(1, "Cappuccino", 2, "3.50"), item(2, "Plain Bagel", 1, "2.25"));
        assertEquals(new BigDecimal("9.25"), o.getSubtotal());
        assertEquals(new BigDecimal("0.74"), o.getTaxAmount(), "8% of 9.25 = 0.74");
        assertEquals(new BigDecimal("9.99"), o.getTotalAmount());
    }

    @Test
    void discountIsTakenOffBeforeTax() {
        Order o = order(new BigDecimal("1.00"), item(1, "Latte", 2, "4.00"));
        assertEquals(new BigDecimal("8.00"), o.getSubtotal());
        assertEquals(new BigDecimal("0.56"), o.getTaxAmount(), "8% of (8.00 - 1.00)");
        assertEquals(new BigDecimal("7.56"), o.getTotalAmount());
    }

    @Test
    void moneyIsRoundedToCentsHalfUp() {
        // 3 x 1.15 = 3.45; tax 0.276 -> 0.28; before the fix the order carried 0.2760 and 3.7260
        Order o = order(BigDecimal.ZERO, item(1, "Donut", 3, "1.15"));
        assertEquals(new BigDecimal("0.28"), o.getTaxAmount());
        assertEquals(new BigDecimal("3.73"), o.getTotalAmount());
        assertEquals(2, o.getTotalAmount().scale());
    }

    @Test
    void aDiscountBiggerThanTheBillMakesItFreeNotNegative() {
        Order o = order(new BigDecimal("50.00"), item(1, "Espresso", 1, "2.50"));
        assertEquals(new BigDecimal("0.00"), o.getTaxAmount());
        assertEquals(new BigDecimal("0.00"), o.getTotalAmount(), "a negative total would be a refund nobody approved");
    }

    @Test
    void anEmptyCartTotalsZeroAndANullDiscountCountsAsZero() {
        assertEquals(new BigDecimal("0.00"), order(BigDecimal.ZERO).getTotalAmount());
        assertEquals(new BigDecimal("2.70"), order(null, item(1, "Tea", 1, "2.50")).getTotalAmount());
    }

    @Test
    void lineTotalFollowsQuantity() {
        OrderItem oi = item(1, "Muffin", 1, "2.00");
        oi.setQuantity(4);
        assertEquals(new BigDecimal("8.00"), oi.getLineTotal());
    }

    @Test
    void onlyDeliveryOrdersAreDeliveries() {
        Order o = new Order();
        assertFalse(o.isDelivery(), "default is DINE_IN");
        o.setOrderType("delivery");
        assertTrue(o.isDelivery());
        o.setOrderType("PICKUP");
        assertFalse(o.isDelivery());
    }
}
