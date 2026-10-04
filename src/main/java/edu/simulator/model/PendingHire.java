package edu.simulator.model;

import java.util.Objects;

public class PendingHire {
    private final Employee employee;
    private int weeksUntilStart;

    public PendingHire(Employee employee, int weeksUntilStart) {
        this.employee = Objects.requireNonNull(employee, "employee");
        if (weeksUntilStart < 0) {
            throw new IllegalArgumentException("Pending hire delay cannot be negative");
        }
        this.weeksUntilStart = weeksUntilStart;
    }

    public Employee getEmployee() {
        return employee;
    }

    public int getWeeksUntilStart() {
        return weeksUntilStart;
    }

    public boolean advanceWeek() {
        if (weeksUntilStart > 0) {
            weeksUntilStart--;
        }
        return weeksUntilStart == 0;
    }
}
