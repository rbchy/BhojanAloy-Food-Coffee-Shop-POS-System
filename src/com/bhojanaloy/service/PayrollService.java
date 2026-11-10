package com.bhojanaloy.service;

import com.bhojanaloy.model.Employee;
import com.bhojanaloy.model.PayrollRun;
import com.bhojanaloy.model.TimeClock;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Payroll calculation engine: turns a set of clocked shifts into a pay
 * breakdown of regular / overtime / weekend / holiday hours and the
 * resulting gross pay, using layered multipliers:
 *
 *   - Any hours beyond {@link #DAILY_REGULAR_CAP} worked in a single day
 *     are Overtime for that day (default 1.5x on top of the day's base rate).
 *   - Any day in {@link #WEEKEND_DAYS} pays its first 8 hours at the
 *     Weekend multiplier (default 1.5x) instead of the normal rate.
 *   - Any day present in the company holiday calendar pays its first 8
 *     hours at the Holiday multiplier (default 2.0x) — holiday takes
 *     precedence over weekend if a holiday falls on a weekend day.
 *   - Overtime on a weekend/holiday stacks: e.g. holiday overtime hours
 *     are paid at HOLIDAY_MULTIPLIER x OVERTIME_MULTIPLIER (3.0x by default).
 *
 * All thresholds/multipliers are simple constants below — adjust them to
 * match your local labor law or company policy.
 */
public class PayrollService {

    public static final BigDecimal DAILY_REGULAR_CAP = BigDecimal.valueOf(8);
    public static final BigDecimal OVERTIME_MULTIPLIER = new BigDecimal("1.5");
    public static final BigDecimal WEEKEND_MULTIPLIER = new BigDecimal("1.5");
    public static final BigDecimal HOLIDAY_MULTIPLIER = new BigDecimal("2.0");

    /** Bangladesh-style weekend (Friday & Saturday). Change to SATURDAY/SUNDAY etc. as needed. */
    public static final Set<DayOfWeek> WEEKEND_DAYS = EnumSet.of(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY);

    /**
     * Calculates a full payroll run for one employee's completed shifts within a pay period.
     * @param shifts       completed (clocked-out) shifts whose work_date falls in [periodStart, periodEnd]
     * @param holidayDates the set of company holiday dates that overlap this period
     */
    public PayrollRun calculate(Employee employee, LocalDate periodStart, LocalDate periodEnd,
                                 List<TimeClock> shifts, Set<LocalDate> holidayDates) {
        BigDecimal rate = employee.getHourlyRate() != null ? employee.getHourlyRate() : BigDecimal.ZERO;

        BigDecimal regularHours = BigDecimal.ZERO;
        BigDecimal overtimeHours = BigDecimal.ZERO;
        BigDecimal weekendHours = BigDecimal.ZERO;
        BigDecimal holidayHours = BigDecimal.ZERO;
        BigDecimal grossPay = BigDecimal.ZERO;

        // The 8-hour cap is per DAY, not per shift: two 6-hour shifts on the same day are
        // 8 regular + 4 overtime hours (before this fix they were paid as 12 regular hours).
        java.util.Map<LocalDate, BigDecimal> hoursPerDay = new java.util.TreeMap<>();
        for (TimeClock shift : shifts) {
            if (shift.getTotalHours() == null || shift.getWorkDate() == null) continue;
            if (shift.getTotalHours().signum() <= 0) continue;
            hoursPerDay.merge(shift.getWorkDate(), shift.getTotalHours(), BigDecimal::add);
        }

        for (java.util.Map.Entry<LocalDate, BigDecimal> day : hoursPerDay.entrySet()) {
            BigDecimal hours = day.getValue();
            LocalDate date = day.getKey();
            boolean isHoliday = holidayDates.contains(date);
            boolean isWeekend = WEEKEND_DAYS.contains(date.getDayOfWeek());

            BigDecimal basePortion = hours.min(DAILY_REGULAR_CAP);
            BigDecimal otPortion = hours.subtract(basePortion).max(BigDecimal.ZERO);

            BigDecimal dayMultiplier = isHoliday ? HOLIDAY_MULTIPLIER : (isWeekend ? WEEKEND_MULTIPLIER : BigDecimal.ONE);

            grossPay = grossPay.add(basePortion.multiply(rate).multiply(dayMultiplier));
            grossPay = grossPay.add(otPortion.multiply(rate).multiply(dayMultiplier).multiply(OVERTIME_MULTIPLIER));

            overtimeHours = overtimeHours.add(otPortion);
            if (isHoliday) holidayHours = holidayHours.add(basePortion);
            else if (isWeekend) weekendHours = weekendHours.add(basePortion);
            else regularHours = regularHours.add(basePortion);
        }

        PayrollRun run = new PayrollRun();
        run.setEmployeeId(employee.getEmployeeId());
        run.setEmployeeName(employee.getFullName());
        run.setPeriodStart(periodStart);
        run.setPeriodEnd(periodEnd);
        run.setRegularHours(round(regularHours));
        run.setOvertimeHours(round(overtimeHours));
        run.setWeekendHours(round(weekendHours));
        run.setHolidayHours(round(holidayHours));
        run.setHourlyRate(rate);
        run.setGrossPay(round(grossPay));
        run.setDeductions(BigDecimal.ZERO); // no tax/benefit deductions modeled — adjust manually if needed
        run.setNetPay(round(grossPay));
        run.setPaymentMethod(employee.getPaymentPreference() != null ? employee.getPaymentPreference() : "CASH");
        run.setStatus("PENDING");
        return run;
    }

    private BigDecimal round(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Simulates sending a direct-deposit bank transfer for a payroll payout.
     * Like {@link PaymentService}, there's no live banking/ACH integration
     * wired up, so this generates a realistic-looking transfer reference
     * after a short simulated delay. To go live, replace this with a call
     * to your bank's business/ACH transfer API using the employee's
     * bank_name / bank_account_number / bank_routing_number.
     */
    public String simulateDirectDeposit(Employee employee, BigDecimal amount) {
        try {
            Thread.sleep(500 + new java.security.SecureRandom().nextInt(500));
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        String bankCode = employee.getBankName() != null && employee.getBankName().length() >= 3
                ? employee.getBankName().substring(0, 3).toUpperCase() : "DEP";
        StringBuilder sb = new StringBuilder(bankCode);
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        java.security.SecureRandom random = new java.security.SecureRandom();
        for (int i = 0; i < 10; i++) sb.append(chars.charAt(random.nextInt(chars.length())));
        return sb.toString();
    }
}
