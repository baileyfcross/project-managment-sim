package edu.simulator.simulation;

import edu.simulator.configuration.SimulationConfiguration;
import edu.simulator.model.Employee;
import edu.simulator.model.Role;
import edu.simulator.model.Team;

import java.math.BigDecimal;
import java.util.Map;

public class CostModel {
    public BigDecimal calculateWeeklyPayroll(Team team, SimulationConfiguration config) {
        double total = 0.0;
        for (Employee employee : team.allEmployees()) {
            if (!employee.isActive()) {
                continue;
            }
            total += switch (employee.getRole()) {
                case PROJECT_MANAGER -> config.getCosts().getProjectManagerWeekly();
                case QA_ENGINEER -> config.getCosts().getQaWeekly();
                case DEVOPS_ENGINEER -> config.getCosts().getDevopsWeekly();
                case DEVELOPER -> switch (employee.getExperienceLevel()) {
                    case JUNIOR -> config.getCosts().getJuniorDeveloperWeekly();
                    case MID_LEVEL -> config.getCosts().getMidDeveloperWeekly();
                    case SENIOR -> config.getCosts().getSeniorDeveloperWeekly();
                };
            };
        }
        return BigDecimal.valueOf(total);
    }

    public BigDecimal calculateWeeklyPayroll(Map<Role, Integer> counts, SimulationConfiguration config) {
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Role, Integer> entry : counts.entrySet()) {
            int count = entry.getValue();
            if (count < 0) {
                throw new IllegalArgumentException("Employee count cannot be negative");
            }
            total = total.add(weeklyRate(entry.getKey(), config).multiply(BigDecimal.valueOf(count)));
        }
        return total;
    }

    public BigDecimal weeklyRate(Role role, SimulationConfiguration config) {
        return BigDecimal.valueOf(switch (role) {
            case PROJECT_MANAGER -> config.getCosts().getProjectManagerWeekly();
            case QA_ENGINEER -> config.getCosts().getQaWeekly();
            case DEVOPS_ENGINEER -> config.getCosts().getDevopsWeekly();
            case DEVELOPER -> config.getCosts().getMidDeveloperWeekly();
        });
    }

    public BigDecimal calculateOvertimeCost(BigDecimal payroll, WorkIntensity intensity,
                                            SimulationConfiguration config) {
        double factor = switch (intensity) {
            case SUSTAINABLE -> 0.0;
            case INCREASED -> 0.08;
            case CRUNCH -> 0.16;
        };
        return payroll.multiply(BigDecimal.valueOf(factor));
    }

    public double calculateAverageWeeklyCost(Team team, SimulationConfiguration config) {
        return calculateWeeklyPayroll(team, config).doubleValue();
    }
}
