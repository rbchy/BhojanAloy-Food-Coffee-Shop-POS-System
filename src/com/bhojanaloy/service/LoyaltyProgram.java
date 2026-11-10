package com.bhojanaloy.service;

import com.bhojanaloy.model.OrderItem;

import java.math.BigDecimal;
import java.util.Collection;

/**
 * Customer loyalty rules, kept out of the Swing screen so they can be unit-tested:
 * every completed order earns {@link #POINTS_PER_ORDER} point; the order that brings a customer to
 * {@link #REWARD_THRESHOLD} points gets its cheapest item (one unit) free and the points reset to 0.
 */
public final class LoyaltyProgram {

    public static final int POINTS_PER_ORDER = 1;
    public static final int REWARD_THRESHOLD = 50;

    private LoyaltyProgram() {}

    /** What this order does to the customer's points, and which item (if any) becomes free. */
    public static final class Decision {
        public final int pointsAfterOrder;
        public final OrderItem freeItem;       // null when no reward on this order
        public final BigDecimal rewardDiscount; // price of one unit of freeItem, else 0

        Decision(int pointsAfterOrder, OrderItem freeItem, BigDecimal rewardDiscount) {
            this.pointsAfterOrder = pointsAfterOrder;
            this.freeItem = freeItem;
            this.rewardDiscount = rewardDiscount;
        }

        public boolean rewardApplied() {
            return freeItem != null;
        }
    }

    public static Decision decide(int currentPoints, Collection<OrderItem> cart) {
        int points = Math.max(0, currentPoints) + POINTS_PER_ORDER;
        if (points < REWARD_THRESHOLD || cart == null || cart.isEmpty()) {
            return new Decision(points, null, BigDecimal.ZERO);
        }
        OrderItem cheapest = null;
        for (OrderItem oi : cart) {
            if (oi.getUnitPrice() == null) continue;
            if (cheapest == null || oi.getUnitPrice().compareTo(cheapest.getUnitPrice()) < 0) cheapest = oi;
        }
        if (cheapest == null) {
            return new Decision(points, null, BigDecimal.ZERO);
        }
        return new Decision(0, cheapest, cheapest.getUnitPrice());
    }
}
