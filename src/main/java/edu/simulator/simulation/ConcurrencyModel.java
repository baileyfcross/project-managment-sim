package edu.simulator.simulation;

import edu.simulator.configuration.PhaseFiveConfiguration;
import edu.simulator.model.ConcurrencyPolicy;
import edu.simulator.model.ProjectPhase;
import edu.simulator.model.SimulationValues;
import edu.simulator.model.WorkState;

public class ConcurrencyModel {
    public double readiness(ProjectPhase phase, WorkState work, ConcurrencyPolicy policy,
                            PhaseFiveConfiguration configuration) {
        ProjectPhase upstream = upstreamPhase(phase);
        if (upstream == null) {
            return 1.0;
        }
        double maturity = work.perceivedPhaseProgress(upstream);
        double floor = configuration.getConcurrency().readiness(policy);
        return SimulationValues.unitInterval(floor + (1.0 - floor) * maturity);
    }

    public double dependencyUncertainty(ProjectPhase phase, WorkState work,
                                        ConcurrencyPolicy policy, double coordinationPenalty,
                                        PhaseFiveConfiguration configuration) {
        ProjectPhase upstream = upstreamPhase(phase);
        if (upstream == null) {
            return 0.0;
        }
        double unfinished = 1.0 - work.perceivedPhaseProgress(upstream);
        double maxPenalty = Math.max(1.0e-9,
                configuration.getConcurrency().getCoordinationPenaltyScale());
        double managementSupport = 1.0 - SimulationValues.unitInterval(
                Math.max(0.0, coordinationPenalty) / maxPenalty);
        double coordinationModifier = 1.0
                - configuration.getConcurrency().getCoordinationReduction() * managementSupport;
        return SimulationValues.unitInterval(unfinished
                * configuration.getConcurrency().uncertainty(policy) * coordinationModifier);
    }

    public double capacityModifier(ConcurrencyPolicy policy, double coordinationPenalty,
                                   PhaseFiveConfiguration configuration) {
        double benefit = configuration.getConcurrency().capacity(policy) - 1.0;
        double maxPenalty = Math.max(1.0e-9,
                configuration.getConcurrency().getCoordinationPenaltyScale());
        double managementSupport = 1.0 - SimulationValues.unitInterval(
                Math.max(0.0, coordinationPenalty) / maxPenalty);
        double policyEffect = 1.0 - configuration.getConcurrency().getCoordinationReduction()
                * (1.0 - managementSupport);
        return Math.max(0.5, 1.0 + benefit * policyEffect);
    }

    public double defectModifier(double uncertainty, PhaseFiveConfiguration configuration) {
        return 1.0 + SimulationValues.unitInterval(uncertainty)
                * configuration.getConcurrency().getDependencyDefectSensitivity();
    }

    private ProjectPhase upstreamPhase(ProjectPhase phase) {
        return switch (phase) {
            case REQUIREMENTS -> null;
            case DESIGN -> ProjectPhase.REQUIREMENTS;
            case DEVELOPMENT -> ProjectPhase.DESIGN;
            case TESTING -> ProjectPhase.DEVELOPMENT;
            case DEPLOYMENT -> ProjectPhase.TESTING;
        };
    }
}
