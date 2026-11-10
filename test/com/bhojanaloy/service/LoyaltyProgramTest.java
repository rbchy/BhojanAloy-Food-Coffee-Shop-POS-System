package com.bhojanaloy.service;

import com.bhojanaloy.model.OrderItem;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** 1 point per order; the 50th point makes the cheapest item free and resets the points. */
class LoyaltyProgramTest {

    private static final List<OrderItem> CART = List.of(
            new OrderItem(1, "Latte", 2, new BigDecimal("4.50")),
            new OrderItem(2, "Plain Bagel", 3, new BigDecimal("2.25")),
            new OrderItem(3, "Club Sandwich", 1, new BigDecimal("7.95")));

    @Test
    void anOrdinaryOrderEarnsOnePoint() {
        LoyaltyProgram.Decision d = LoyaltyProgram.decide(10, CART);
        assertEquals(11, d.pointsAfterOrder);
        assertFalse(d.rewardApplied());
        assertEquals(BigDecimal.ZERO, d.rewardDiscount);
    }

    @Test
    void theFiftiethPointMakesOneUnitOfTheCheapestItemFree() {
        LoyaltyProgram.Decision d = LoyaltyProgram.decide(49, CART);
        assertTrue(d.rewardApplied());
        assertEquals("Plain Bagel", d.freeItem.getItemName());
        assertEquals(new BigDecimal("2.25"), d.rewardDiscount, "one bagel, not the whole line of 3");
        assertEquals(0, d.pointsAfterOrder, "points reset once redeemed");
    }

    @Test
    void pointsAboveTheThresholdStillRedeem() {
        assertTrue(LoyaltyProgram.decide(73, CART).rewardApplied());
    }

    @Test
    void oneOrderBeforeTheRewardDoesNotRedeem() {
        LoyaltyProgram.Decision d = LoyaltyProgram.decide(48, CART);
        assertFalse(d.rewardApplied());
        assertEquals(49, d.pointsAfterOrder);
    }

    @Test
    void anEmptyCartEarnsThePointButNothingIsFree() {
        LoyaltyProgram.Decision d = LoyaltyProgram.decide(49, List.of());
        assertFalse(d.rewardApplied());
        assertEquals(50, d.pointsAfterOrder);
    }

    @Test
    void corruptNegativePointsStartFromZero() {
        assertEquals(1, LoyaltyProgram.decide(-5, CART).pointsAfterOrder);
    }
}
