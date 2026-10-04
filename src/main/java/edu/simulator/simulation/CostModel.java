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
            total += employee.getBaseWeeklyCost();
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
        return weeklyRate(role, config.getInitialTeamExperience().forRole(role), config);
    }

    public BigDecimal weeklyRate(Role role, edu.simulator.model.ExperienceLevel level,
                                 SimulationConfiguration config) {
        return BigDecimal.valueOf(config.getCosts().weeklySalary(role, level));
    }

    public BigDecimal hiringCost(edu.simulator.model.ExperienceLevel level,
                                 SimulationConfiguration config) {
        return BigDecimal.valueOf(config.getCosts().hiringCost(level));
    }

    public BigDecimal calculateOvertimeCost(BigDecimal payroll, WorkIntensity intensity,
                                            SimulationConfiguration config) {
        double premium = config.getCosts().getOvertimePremiumRate();
        double factor = switch (intensity) {
            case SUSTAINABLE -> 0.0;
            case INCREASED -> premium * 0.5;
            case CRUNCH -> premium;
        };
        return payroll.multiply(BigDecimal.valueOf(factor));
    }

    public double calculateAverageWeeklyCost(Team team, SimulationConfiguration config) {
        return calculateWeeklyPayroll(team, config).doubleValue();
    }
}
