package edu.simulator.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

public class Team {
    private final Map<Role, List<Employee>> employeesByRole = new EnumMap<>(Role.class);

    public Team() {
        for (Role role : Role.values()) {
            employeesByRole.put(role, new ArrayList<>());
        }
    }

    public void addEmployee(Employee employee) {
        if (employee == null) {
            throw new IllegalArgumentException("Employee is required");
        }
        if (allEmployees().stream().anyMatch(existing -> existing.getId().equals(employee.getId()))) {
            throw new IllegalArgumentException("Employee is already on this team");
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
        return (int) employeesByRole.getOrDefault(role, List.of()).stream()
                .filter(Employee::isActive).count();
    }

    public int totalCount() {
        int total = 0;
        for (Role role : Role.values()) {
            total += count(role);
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
        return Map.copyOf(counts);
    }

    public Map<Role, Map<ExperienceLevel, Integer>> toExperienceCounts() {
        Map<Role, Map<ExperienceLevel, Integer>> counts = new EnumMap<>(Role.class);
        for (Role role : Role.values()) {
            Map<ExperienceLevel, Integer> byExperience = new EnumMap<>(ExperienceLevel.class);
            for (ExperienceLevel level : ExperienceLevel.values()) {
                byExperience.put(level, 0);
            }
            for (Employee employee : employeesByRole.get(role)) {
                if (employee.isActive()) {
                    byExperience.compute(employee.getExperienceLevel(), (level, count) -> count + 1);
                }
            }
            counts.put(role, Map.copyOf(byExperience));
        }
        return Map.copyOf(counts);
    }

    public List<Employee> activeEmployees() {
        return allEmployees().stream().filter(Employee::isActive).toList();
    }

    public List<Employee> onboardingEmployees() {
        return activeEmployees().stream()
                .filter(employee -> employee.getOnboardingProgress() < 1.0)
                .toList();
    }

    public Set<String> employeeIds() {
        Set<String> ids = new HashSet<>();
        for (Employee employee : allEmployees()) {
            ids.add(employee.getId());
        }
        return Set.copyOf(ids);
    }

    @FunctionalInterface
    public interface EmployeeFactory {
        Employee create(Role role);
    }
}
