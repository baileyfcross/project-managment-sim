package edu.simulator.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Team {
    private final Map<Role, List<Employee>> employeesByRole = new EnumMap<>(Role.class);

    public Team() {
        for (Role role : Role.values()) {
            employeesByRole.put(role, new ArrayList<>());
        }
    }

    public void addEmployee(Employee employee) {
        if (employee == null) {
            return;
        }
        employeesByRole.get(employee.getRole()).add(employee);
    }

    public void addEmployees(Role role, int count, EmployeeFactory factory) {
        if (count < 0) {
            throw new IllegalArgumentException("Employee count cannot be negative");
        }
        for (int index = 0; index < count; index++) {
            addEmployee(factory.create(role));
        }
    }

    public int count(Role role) {
        return employeesByRole.getOrDefault(role, List.of()).size();
    }

    public int totalCount() {
        int total = 0;
        for (List<Employee> employees : employeesByRole.values()) {
            total += employees.size();
        }
        return total;
    }

    public List<Employee> allEmployees() {
        List<Employee> all = new ArrayList<>();
        for (List<Employee> employees : employeesByRole.values()) {
            all.addAll(employees);
        }
        return all;
    }

    public void removeInactiveEmployees() {
        for (List<Employee> employees : employeesByRole.values()) {
            employees.removeIf(employee -> !employee.isActive());
        }
    }

    public Map<Role, Integer> toRoleCounts() {
        Map<Role, Integer> counts = new EnumMap<>(Role.class);
        for (Role role : Role.values()) {
            counts.put(role, count(role));
        }
        return counts;
    }

    @FunctionalInterface
    public interface EmployeeFactory {
        Employee create(Role role);
    }
}
