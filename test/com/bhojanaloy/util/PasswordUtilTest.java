package com.bhojanaloy.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    void theRightPasswordVerifiesAndAWrongOneDoesNot() {
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hash("Cafe@2026", salt);
        assertTrue(PasswordUtil.verify("Cafe@2026", salt, hash));
        assertFalse(PasswordUtil.verify("cafe@2026", salt, hash));
        assertFalse(PasswordUtil.verify("", salt, hash));
    }

    @Test
    void theSamePasswordWithADifferentSaltHashesDifferently() {
        String a = PasswordUtil.generateSalt(), b = PasswordUtil.generateSalt();
        assertNotEquals(a, b);
        assertNotEquals(PasswordUtil.hash("same", a), PasswordUtil.hash("same", b));
    }

    @Test
    void theSampleAccountsInTheSqlUseAdmin123() {
        // database/bhojan_aloy_database.sql inserts admin / farida / sabbir with this salt + hash.
        assertTrue(PasswordUtil.verify("admin123", "Ekihs0CPu28qvXmcPmW8PA==",
                "c9lGa8b8WIujBoIb6qpeodXwF5+4A+4bUXW6jQpdjCo="));
    }

    @Test
    void placeholderOrMissingValuesNeverLogIn() {
        assertFalse(PasswordUtil.verify("x", "__SET_BY_APP__", "__SET_BY_APP__"));
        assertFalse(PasswordUtil.verify("x", null, "abc"));
        assertFalse(PasswordUtil.verify(null, PasswordUtil.generateSalt(), "abc"));
    }
}
