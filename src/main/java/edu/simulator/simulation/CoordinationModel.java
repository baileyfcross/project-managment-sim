package edu.simulator.simulation;

import edu.simulator.configuration.SimulationConfiguration;
import edu.simulator.model.Role;
import edu.simulator.model.Team;

public class CoordinationModel {
    public double calculatePenalty(Team team, SimulationConfiguration config) {
        int teamSize = team.totalCount();
        int managerCount = team.count(Role.PROJECT_MANAGER);
        double effectiveTeamSize = Math.max(1, teamSize - managerCount);
        double penalty = (effectiveTeamSize * (effectiveTeamSize - 1.0)) / 160.0;
        return Math.max(0.0, Math.min(config.getProductivity().getCoordinationMaxPenalty(), penalty));
    }

    public double calculatePenalty(int teamSize, int managerCount, SimulationConfiguration config) {
        double effectiveTeamSize = Math.max(1, teamSize - managerCount);
        double penalty = (effectiveTeamSize * (effectiveTeamSize - 1.0)) / 160.0;
        return Math.max(0.0, Math.min(config.getProductivity().getCoordinationMaxPenalty(), penalty));
    }
}
