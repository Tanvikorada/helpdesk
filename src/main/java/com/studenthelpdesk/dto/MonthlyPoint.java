package com.studenthelpdesk.dto;

public class MonthlyPoint {

    private final String period;
    private final long total;

    public MonthlyPoint(String period, long total) {
        this.period = period;
        this.total = total;
    }

    public String getPeriod() {
        return period;
    }

    public long getTotal() {
        return total;
    }
}
