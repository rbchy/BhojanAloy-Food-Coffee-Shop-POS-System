package com.bhojanaloy.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** A single clock-in/clock-out record for one employee's shift. */
public class TimeClock {
    private int clockId;
    private int employeeId;
    private String employeeName; // convenience join field
    private LocalDate workDate;
    private LocalDateTime clockIn;
    private LocalDateTime clockOut; // null while still clocked in
    private BigDecimal totalHours;  // null while still clocked in
    private String notes;

    public TimeClock() {}

    public boolean isOpen() { return clockOut == null; }

    public int getClockId() { return clockId; }
    public void setClockId(int clockId) { this.clockId = clockId; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public LocalDate getWorkDate() { return workDate; }
    public void setWorkDate(LocalDate workDate) { this.workDate = workDate; }

    public LocalDateTime getClockIn() { return clockIn; }
    public void setClockIn(LocalDateTime clockIn) { this.clockIn = clockIn; }

    public LocalDateTime getClockOut() { return clockOut; }
    public void setClockOut(LocalDateTime clockOut) { this.clockOut = clockOut; }

    public BigDecimal getTotalHours() { return totalHours; }
    public void setTotalHours(BigDecimal totalHours) { this.totalHours = totalHours; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    @Override
    public String toString() { return employeeName + ": " + workDate + " (" + (isOpen() ? "clocked in" : totalHours + "h") + ")"; }
}
