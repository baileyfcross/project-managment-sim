package edu.simulator.simulation;

import edu.simulator.configuration.SimulationConfiguration;
import edu.simulator.model.ProjectPhase;
import edu.simulator.model.WorkState;

public class PhaseReadinessModel {
    public double readiness(ProjectPhase phase, WorkState work,
                            SimulationConfiguration configuration) {
        double minimumOverlap = configuration.getPhaseReadiness().getMinimumOverlap();
        return switch (phase) {
            case REQUIREMENTS -> 1.0;
            case DESIGN -> downstreamReadiness(ProjectPhase.REQUIREMENTS, work, minimumOverlap);
            case DEVELOPMENT -> downstreamReadiness(ProjectPhase.DESIGN, work, minimumOverlap);
            case TESTING -> work.getTotalWork(ProjectPhase.DEVELOPMENT) <= 0.0
                    ? 1.0 : work.perceivedPhaseProgress(ProjectPhase.DEVELOPMENT);
            case DEPLOYMENT -> downstreamReadiness(ProjectPhase.TESTING, work, minimumOverlap);
        };
    }

    private double downstreamReadiness(ProjectPhase upstream, WorkState work, double overlap) {
        double upstreamProgress = work.perceivedPhaseProgress(upstream);
        return overlap + (1.0 - overlap) * upstreamProgress;
    }
}
