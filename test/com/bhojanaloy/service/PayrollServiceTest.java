package com.bhojanaloy.service;

import com.bhojanaloy.model.Employee;
import com.bhojanaloy.model.PayrollRun;
import com.bhojanaloy.model.TimeClock;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pay rules: first 8 h of a day at the day's rate, the rest at 1.5x on top;
 * weekend (Fri/Sat) 1.5x; holiday 2.0x, and holiday beats weekend.
 */
class PayrollServiceTest {

    private static final LocalDate MON = LocalDate.of(2026, 11, 9);
    private static final LocalDate TUE = MON.plusDays(1);
    private static final LocalDate FRI = MON.plusDays(4);
    private static final LocalDate SAT = MON.plusDays(5);

    private final PayrollService payroll = new PayrollService();

    private static Employee employee(String rate) {
        Employee e = new Employee();
        e.setEmployeeId(7);
        e.setFullName("Test Barista");
        e.setHourlyRate(rate == null ? null : new BigDecimal(rate));
        return e;
    }

    private static TimeClock shift(LocalDate day, String hours) {
        TimeClock t = new TimeClock();
        t.setWorkDate(day);
        t.setTotalHours(hours == null ? null : new BigDecimal(hours));
        return t;
    }

    private PayrollRun run(String rate, Set<LocalDate> holidays, TimeClock... shifts) {
        return payroll.calculate(employee(rate), MON, MON.plusDays(13), new ArrayList<>(List.of(shifts)), holidays);
    }

    private static void money(String expected, BigDecimal actual) {
        assertEquals(new BigDecimal(expected), actual);
    }

    @Test
    void anOrdinaryWeekdayIsAllRegularPay() {
        PayrollRun r = run("10.00", Set.of(), shift(MON, "8"), shift(TUE, "6.5"));
        money("14.50", r.getRegularHours());
        money("0.00", r.getOvertimeHours());
        money("145.00", r.getGrossPay());
        money("145.00", r.getNetPay());
        assertEquals("PENDING", r.getStatus());
        assertEquals("CASH", r.getPaymentMethod(), "no preference stored -> cash");
    }

    @Test
    void hoursPastEightInADayAreOvertime() {
        PayrollRun r = run("10.00", Set.of(), shift(MON, "10"));
        money("8.00", r.getRegularHours());
        money("2.00", r.getOvertimeHours());
        money("110.00", r.getGrossPay()); // 8 x 10 + 2 x 15
    }

    @Test
    void theEightHourCapIsPerDayNotPerShift() {
        // Split shift: 6 h morning + 6 h evening on the same Monday = 8 regular + 4 overtime.
        // Before the fix each shift had its own 8 h cap, so this paid 12 regular hours (120.00).
        PayrollRun r = run("10.00", Set.of(), shift(MON, "6"), shift(MON, "6"));
        money("8.00", r.getRegularHours());
        money("4.00", r.getOvertimeHours());
        money("140.00", r.getGrossPay());
    }

    @Test
    void weekendDaysPayOneAndAHalf() {
        PayrollRun r = run("10.00", Set.of(), shift(FRI, "8"), shift(SAT, "4"));
        money("12.00", r.getWeekendHours());
        money("0.00", r.getRegularHours());
        money("180.00", r.getGrossPay());
    }

    @Test
    void holidayPaysDoubleAndOvertimeStacksToTriple() {
        PayrollRun r = run("10.00", Set.of(TUE), shift(TUE, "10"));
        money("8.00", r.getHolidayHours());
        money("2.00", r.getOvertimeHours());
        money("220.00", r.getGrossPay()); // 8 x 20 + 2 x 30
    }

    @Test
    void aHolidayOnAWeekendIsPaidAsAHoliday() {
        PayrollRun r = run("10.00", Set.of(FRI), shift(FRI, "8"));
        money("8.00", r.getHolidayHours());
        money("0.00", r.getWeekendHours());
        money("160.00", r.getGrossPay());
    }

    @Test
    void openShiftsAndNonsenseHoursAreIgnored() {
        PayrollRun r = run("10.00", Set.of(), shift(MON, null), shift(null, "5"), shift(TUE, "-3"), shift(TUE, "2"));
        money("2.00", r.getRegularHours());
        money("20.00", r.getGrossPay());
    }

    @Test
    void noRateMeansNoPayButHoursAreStillCounted() {
        PayrollRun r = run(null, Set.of(), shift(MON, "9"));
        money("0.00", r.getGrossPay());
        money("1.00", r.getOvertimeHours());
    }

    @Test
    void fractionalPayIsRoundedToCents() {
        PayrollRun r = run("12.37", Set.of(), shift(MON, "7.75"));
        money("95.87", r.getGrossPay()); // 95.8675
    }
}
