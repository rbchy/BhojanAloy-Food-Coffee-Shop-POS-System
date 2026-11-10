package com.bhojanaloy.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilTest {

    @ParameterizedTest
    @ValueSource(strings = {"rahim@example.com", "first.last+pos@shop.co", " ok@mail.org "})
    void validEmails(String email) {
        assertTrue(ValidationUtil.isValidEmail(email));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "plainaddress", "a@b", "a@b.c", "@shop.com", "a b@shop.com"})
    void invalidEmails(String email) {
        assertFalse(ValidationUtil.isValidEmail(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {"01712345678", "+8801712345678", "267-342-0000", "555 123 4567"})
    void validPhones(String phone) {
        assertTrue(ValidationUtil.isValidPhone(phone));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "12345", "phone", "+1234567890123456", "017-ABC-5678"})
    void invalidPhones(String phone) {
        assertFalse(ValidationUtil.isValidPhone(phone));
    }

    @Test
    void prices() {
        assertEquals(new BigDecimal("3.50"), ValidationUtil.parsePositiveDecimal(" 3.50 "));
        assertNull(ValidationUtil.parsePositiveDecimal("0"), "a menu item cannot be free");
        assertNull(ValidationUtil.parsePositiveDecimal("-1"));
        assertNull(ValidationUtil.parsePositiveDecimal("abc"));
        assertNull(ValidationUtil.parsePositiveDecimal(null));
    }

    @Test
    void hoursAndStock() {
        assertEquals(BigDecimal.ZERO, ValidationUtil.parseNonNegativeDecimal("0"));
        assertNull(ValidationUtil.parseNonNegativeDecimal("-0.5"));
        assertEquals(12, ValidationUtil.parseNonNegativeInt("12"));
        assertNull(ValidationUtil.parseNonNegativeInt("-3"));
        assertNull(ValidationUtil.parseNonNegativeInt("1.5"));
    }

    @Test
    void nonEmpty() {
        assertTrue(ValidationUtil.isNonEmpty("x"));
        assertFalse(ValidationUtil.isNonEmpty("   "));
        assertFalse(ValidationUtil.isNonEmpty(null));
    }
}
