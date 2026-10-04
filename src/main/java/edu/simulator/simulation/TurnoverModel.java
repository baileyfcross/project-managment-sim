package edu.simulator.simulation;

import edu.simulator.configuration.SimulationConfiguration;
import edu.simulator.model.Employee;
import edu.simulator.model.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TurnoverModel {
    public List<Employee> resolveDepartures(Team team, double schedulePressure,
                                            SimulationConfiguration config, Random random) {
        List<Employee> departures = new ArrayList<>();
        for (Employee employee : team.activeEmployees()) {
            if (random.nextDouble() < departureProbability(employee, schedulePressure, config)) {
                employee.setActive(false);
                departures.add(employee);
            }
        }
        return List.copyOf(departures);
    }

    public double departureProbability(Employee employee, double schedulePressure,
                                       SimulationConfiguration config) {
        var settings = config.getTurnover();
        double streak = Math.min(employee.getConsecutiveOvertimeWeeks(),
                settings.getOvertimeStreakCap());
        double fatigue = employee.getFatigue();
        double moraleBaseline = Math.max(0.01, config.getMorale().getBaseline());
        double moraleDeficit = Math.max(0.0,
                (moraleBaseline - employee.getMorale()) / moraleBaseline);
        double probability = settings.getBaseRate()
                + fatigue * fatigue * settings.getFatigueWeight()
                + moraleDeficit * settings.getMoraleWeight()
                + clamp(schedulePressure) * settings.getPressureWeight()
                + streak * settings.getOvertimeStreakWeight();
        return Math.min(settings.getMaxRate(), Math.max(0.0, probability));
    }

    public int calculateTurnover(Team team, double fatigue, double morale, double schedulePressure,
                                SimulationConfiguration config, Random random) {
        int count = 0;
        double baseline = Math.max(0.01, config.getMorale().getBaseline());
        double moraleDeficit = Math.max(0.0, (baseline - morale) / baseline);
        for (Employee employee : team.activeEmployees()) {
            double employeeFatigue = clamp(fatigue);
            double probability = config.getTurnover().getBaseRate()
                    + employeeFatigue * employeeFatigue * config.getTurnover().getFatigueWeight()
                    + moraleDeficit * config.getTurnover().getMoraleWeight()
                    + clamp(schedulePressure) * config.getTurnover().getPressureWeight()
                    + Math.min(employee.getConsecutiveOvertimeWeeks(),
                    config.getTurnover().getOvertimeStreakCap())
                    * config.getTurnover().getOvertimeStreakWeight();
            probability = Math.min(config.getTurnover().getMaxRate(), Math.max(0.0, probability));
            if (random.nextDouble() < probability) {
                employee.setActive(false);
                count++;
            }
        }
        return count;
    }

    public String riskCategory(Team team, double schedulePressure,
                               SimulationConfiguration config) {
        double risk = team.activeEmployees().stream()
                .mapToDouble(employee -> departureProbability(employee, schedulePressure, config))
                .average().orElse(0.0);
        if (risk < 0.03) return "Low";
        if (risk < 0.07) return "Moderate";
        if (risk < 0.12) return "High";
        return "Severe";
    }

    private double clamp(double value) {
        return Double.isFinite(value) ? Math.max(0.0, Math.min(1.0, value)) : 0.0;
    }
}
