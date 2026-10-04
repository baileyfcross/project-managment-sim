package edu.simulator.simulation;

import edu.simulator.configuration.SimulationConfiguration;
import edu.simulator.model.Employee;
import edu.simulator.model.ExperienceLevel;
import edu.simulator.model.Role;
import edu.simulator.model.Team;

public class ProductivityModel {
    public ProductivityResult calculate(Team team, WorkIntensity intensity, double fatigue,
                                       double schedulePressure, SimulationConfiguration config) {
        MentoringModel.MentoringResult mentoring = new MentoringModel().calculate(team, 0, config);
        return calculate(team, intensity, fatigue, schedulePressure, config, mentoring);
    }

    public ProductivityResult calculate(Team team, WorkIntensity intensity, double fatigue,
                                        double schedulePressure, SimulationConfiguration config,
                                        MentoringModel.MentoringResult mentoring) {
        int totalTeamSize = team.totalCount();
        double coordinationPenalty = new CoordinationModel().calculatePenalty(team, config);
        double fatigueModifier = 1.0 - Math.max(0.0, fatigue * 0.45);
        double schedulePressureModifier = 1.0 + Math.min(0.35, schedulePressure * 0.25);

        double effectiveProductivity = 0.0;
        double developerEffectiveCapacity = 0.0;
        double qaEffectiveCapacity = 0.0;
        double devopsEffectiveCapacity = 0.0;
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
            double onboardingFactor = employee.getOnboardingProgress();
            double fatigueImpact = 1.0 - employee.getFatigue() * 0.5;
            double intensityModifier = switch (intensity) {
                case SUSTAINABLE -> 1.0;
                case INCREASED -> 1.12;
                case CRUNCH -> 1.18;
            };
            double mentoringModifier = new MentoringModel().directProductivityModifier(
                    employee, mentoring, config);
            double roleCapacity = roleWeight * experienceMultiplier * onboardingFactor
                    * fatigueImpact * intensityModifier * mentoringModifier;
            effectiveProductivity += roleCapacity;
            if (employee.getRole() == Role.DEVELOPER) {
                developerEffectiveCapacity += roleCapacity;
            } else if (employee.getRole() == Role.QA_ENGINEER) {
                qaEffectiveCapacity += roleCapacity;
            } else if (employee.getRole() == Role.DEVOPS_ENGINEER) {
                devopsEffectiveCapacity += roleCapacity;
            }
        }

        effectiveProductivity *= (1.0 - coordinationPenalty) * fatigueModifier * schedulePressureModifier;
        developerEffectiveCapacity *= (1.0 - coordinationPenalty) * fatigueModifier * schedulePressureModifier;
        qaEffectiveCapacity *= (1.0 - coordinationPenalty) * fatigueModifier * schedulePressureModifier;
        devopsEffectiveCapacity *= (1.0 - coordinationPenalty) * fatigueModifier * schedulePressureModifier;
        effectiveProductivity = Math.max(0.0, effectiveProductivity);
        developerEffectiveCapacity = Math.max(0.0, developerEffectiveCapacity);
        qaEffectiveCapacity = Math.max(0.0, qaEffectiveCapacity);
        devopsEffectiveCapacity = Math.max(0.0, devopsEffectiveCapacity);

        return new ProductivityResult(effectiveProductivity, developerEffectiveCapacity,
                qaEffectiveCapacity, devopsEffectiveCapacity, coordinationPenalty,
                mentoring, fatigueModifier, schedulePressureModifier,
                totalTeamSize, intensity);
    }

    public record ProductivityResult(double totalEffectiveCapacity, double developerEffectiveCapacity,
                                    double qaEffectiveCapacity, double devopsEffectiveCapacity,
                                    double coordinationPenalty, MentoringModel.MentoringResult mentoring,
                                    double fatigueModifier, double schedulePressureModifier,
                                    int totalTeamSize, WorkIntensity intensity) {
    }
}
