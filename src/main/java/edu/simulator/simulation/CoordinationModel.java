package edu.simulator.simulation;

import edu.simulator.configuration.SimulationConfiguration;
import edu.simulator.model.ExperienceLevel;
import edu.simulator.model.Role;
import edu.simulator.model.Team;

public class CoordinationModel {
    public double calculatePenalty(Team team, SimulationConfiguration config) {
        int seniorCount = team.toExperienceCounts().values().stream()
                .mapToInt(counts -> counts.getOrDefault(ExperienceLevel.SENIOR, 0)).sum();
        return calculatePenalty(team.totalCount(), team.count(Role.PROJECT_MANAGER),
                seniorCount, config);
    }

    public double calculatePenalty(int teamSize, int managerCount, SimulationConfiguration config) {
        return calculatePenalty(teamSize, managerCount, 0, config);
    }

    public double calculatePenalty(int teamSize, int managerCount, int seniorCount,
                                   SimulationConfiguration config) {
        double pairs = Math.max(0.0, (double) teamSize * (teamSize - 1) / 2.0);
        double raw = Math.sqrt(pairs) * config.getCoordination().getPairScale();
        double managementBenefit = 1.0
                - Math.min(0.8, managerCount * config.getCoordination().getProjectManagerReduction());
        double experienceBenefit = 1.0
                - Math.min(0.5, seniorCount * config.getCoordination().getSeniorReduction());
        double penalty = raw * managementBenefit * experienceBenefit;
        return Math.max(0.0, Math.min(config.getCoordination().getMaximumPenalty(), penalty));
    }

    public String healthLabel(double penalty) {
        if (penalty < 0.1) {
            return "Healthy";
        }
        if (penalty < 0.25) {
            return "Moderate";
        }
        return "High";
    }
}
