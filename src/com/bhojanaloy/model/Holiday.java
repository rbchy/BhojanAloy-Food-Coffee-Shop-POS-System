package com.bhojanaloy.model;

import java.time.LocalDate;

/** A company holiday — hours worked on this date earn the holiday pay multiplier. */
public class Holiday {
    private int holidayId;
    private LocalDate holidayDate;
    private String holidayName;

    public Holiday() {}

    public Holiday(LocalDate holidayDate, String holidayName) {
        this.holidayDate = holidayDate;
        this.holidayName = holidayName;
    }

    public int getHolidayId() { return holidayId; }
    public void setHolidayId(int holidayId) { this.holidayId = holidayId; }

    public LocalDate getHolidayDate() { return holidayDate; }
    public void setHolidayDate(LocalDate holidayDate) { this.holidayDate = holidayDate; }

    public String getHolidayName() { return holidayName; }
    public void setHolidayName(String holidayName) { this.holidayName = holidayName; }

    @Override
    public String toString() { return holidayDate + " - " + holidayName; }
}
