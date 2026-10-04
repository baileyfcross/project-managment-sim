package edu.simulator.simulation;

import edu.simulator.configuration.SimulationConfiguration;
import edu.simulator.model.Employee;
import edu.simulator.model.ExperienceLevel;
import edu.simulator.model.Role;
import edu.simulator.model.Team;

import java.math.BigDecimal;

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

    public BigDecimal calculateOvertimeCost(BigDecimal payroll, WorkIntensity intensity, SimulationConfiguration config) {
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
