package com.bhojanaloy.dao;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** orders.order_number is VARCHAR(30) UNIQUE: a repeated number fails the checkout. */
class OrderNumberTest {

    private final OrderDAO dao = new OrderDAO();

    @Test
    void aBurstOfOrderNumbersNeverRepeats() {
        // Far more than 100 per millisecond, which is where a naive "time + 2 digits" scheme repeats.
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 50_000; i++) {
            String n = dao.generateOrderNumber();
            assertTrue(seen.add(n), "repeated " + n + " after " + i);
        }
    }

    @Test
    void theFormatFitsTheColumn() {
        String n = dao.generateOrderNumber();
        assertTrue(n.matches("ORD-\\d{8}-\\d{9}-\\d{2}"), n);
        assertTrue(n.length() <= 30, n);
    }

    @Test
    void concurrentTillsDoNotCollide() throws Exception {
        Set<String> seen = java.util.Collections.synchronizedSet(new HashSet<>());
        Thread[] tills = new Thread[4];
        for (int t = 0; t < tills.length; t++) {
            tills[t] = new Thread(() -> {
                for (int i = 0; i < 5_000; i++) seen.add(dao.generateOrderNumber());
            });
            tills[t].start();
        }
        for (Thread t : tills) t.join();
        assertEquals(20_000, seen.size());
    }
}
