package com.studenthelpdesk.dto;

public class DepartmentCount {

    private final String department;
    private final long total;

    public DepartmentCount(String department, long total) {
        this.department = department;
        this.total = total;
    }

    public String getDepartment() {
        return department;
    }

    public long getTotal() {
        return total;
    }
}
