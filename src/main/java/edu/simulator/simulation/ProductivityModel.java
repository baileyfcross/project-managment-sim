package edu.simulator.simulation;

import edu.simulator.configuration.SimulationConfiguration;
import edu.simulator.model.Employee;
import edu.simulator.model.ExperienceLevel;
import edu.simulator.model.Role;
import edu.simulator.model.Team;

public class ProductivityModel {
    public ProductivityResult calculate(Team team, WorkIntensity intensity, double fatigue,
                                       double schedulePressure, SimulationConfiguration config) {
        int totalTeamSize = team.totalCount();
        double coordinationPenalty = new CoordinationModel().calculatePenalty(totalTeamSize, team.count(Role.PROJECT_MANAGER), config);
        double fatigueModifier = 1.0 - Math.max(0.0, fatigue * 0.45);
        double schedulePressureModifier = 1.0 + Math.min(0.35, schedulePressure * 0.25);

        double effectiveProductivity = 0.0;
        double developerEffectiveCapacity = 0.0;
        for (Employee employee : team.allEmployees()) {
            if (!employee.isActive()) {
                continue;
            }
            double roleWeight = switch (employee.getRole()) {
                case DEVELOPER -> 1.0;
                case QA_ENGINEER -> 0.55;
                case DEVOPS_ENGINEER -> 0.65;
                case PROJECT_MANAGER -> 0.35;
            };
            double experienceMultiplier = switch (employee.getExperienceLevel()) {
                case JUNIOR -> config.getProductivity().getJunior();
                case MID_LEVEL -> config.getProductivity().getMid();
                case SENIOR -> config.getProductivity().getSenior();
            };
            double onboardingFactor = 0.25 + (0.75 * employee.getOnboardingProgress());
            double fatigueImpact = 1.0 - fatigue * 0.5;
            double intensityModifier = switch (intensity) {
                case SUSTAINABLE -> 1.0;
                case INCREASED -> 1.12;
                case CRUNCH -> 1.18;
            };
            double roleCapacity = roleWeight * experienceMultiplier * onboardingFactor * fatigueImpact * intensityModifier;
            effectiveProductivity += roleCapacity;
            if (employee.getRole() == Role.DEVELOPER) {
                developerEffectiveCapacity += roleCapacity;
            }
        }

        effectiveProductivity *= (1.0 - coordinationPenalty) * fatigueModifier * schedulePressureModifier;
        developerEffectiveCapacity *= (1.0 - coordinationPenalty) * fatigueModifier * schedulePressureModifier;
        effectiveProductivity = Math.max(0.0, effectiveProductivity);
        developerEffectiveCapacity = Math.max(0.0, developerEffectiveCapacity);

        return new ProductivityResult(effectiveProductivity, developerEffectiveCapacity, coordinationPenalty, fatigueModifier, schedulePressureModifier,
                totalTeamSize, intensity);
    }

    public record ProductivityResult(double totalEffectiveCapacity, double developerEffectiveCapacity, double coordinationPenalty,
                                    double fatigueModifier, double schedulePressureModifier,
                                    int totalTeamSize, WorkIntensity intensity) {
    }

    public static class CoordinationModel {
        private double calculatePenalty(int teamSize, int managerCount, SimulationConfiguration config) {
            double effectiveSize = Math.max(1, teamSize - managerCount);
            double overhead = (effectiveSize * (effectiveSize - 1.0)) / 120.0;
            double penalty = Math.min(config.getProductivity().getCoordinationMaxPenalty(), overhead);
            return Math.max(0.0, penalty);
        }
    }
}
