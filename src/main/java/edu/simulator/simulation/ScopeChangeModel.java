package edu.simulator.simulation;

import edu.simulator.configuration.PhaseFiveConfiguration;
import edu.simulator.model.Project;
import edu.simulator.model.ProjectPhase;
import edu.simulator.model.SimulationValues;

import java.util.EnumMap;
import java.util.Map;

public class ScopeChangeModel {
    public ScopeChangeResult acceptFeature(Project project, Map<ProjectPhase, Double> featureWork,
                                           PhaseFiveConfiguration configuration) {
        var work = project.getWorkState();
        EnumMap<ProjectPhase, Double> added = new EnumMap<>(ProjectPhase.class);
        EnumMap<ProjectPhase, Double> ripple = new EnumMap<>(ProjectPhase.class);
        double addedTotal = 0.0;
        double reworkTotal = 0.0;
        for (ProjectPhase phase : ProjectPhase.values()) {
            double amount = SimulationValues.nonNegativeFinite(featureWork.getOrDefault(phase, 0.0));
            added.put(phase, amount);
            if (amount <= 0.0) {
                ripple.put(phase, 0.0);
                continue;
            }
            double currentScope = project.getOriginalScope(phase);
            double progress = currentScope <= 0.0 ? 0.0
                    : SimulationValues.unitInterval(work.getCorrectWorkCompleted(phase) / currentScope);
            double phaseRipple = configValue(configuration.getScope().getRippleFactors(), phase, 0.0);
            double invalidated = amount * progress * phaseRipple;
            work.addKnownRework(phase, invalidated);
            ripple.put(phase, invalidated);
            addedTotal += amount;
            reworkTotal += invalidated;
            work.addScope(phase, amount);
        }
        double downstreamRework = 0.0;
        for (ProjectPhase changedPhase : ProjectPhase.values()) {
            double addedForPhase = added.getOrDefault(changedPhase, 0.0);
            if (addedForPhase <= 0.0) {
                continue;
            }
            for (ProjectPhase downstream : ProjectPhase.values()) {
                if (downstream.ordinal() <= changedPhase.ordinal()) {
                    continue;
                }
                double downstreamScope = project.getOriginalScope(downstream);
                if (downstreamScope <= 0.0) {
                    continue;
                }
                double downstreamProgress = SimulationValues.unitInterval(
                        work.getCorrectWorkCompleted(downstream) / downstreamScope);
                double phaseRipple = configValue(
                        configuration.getScope().getRippleFactors(), downstream, 0.0);
                double impact = addedForPhase * downstreamProgress * phaseRipple
                        * configuration.getScope().getDownstreamRipple();
                work.addKnownRework(downstream, impact);
                ripple.merge(downstream, impact, Double::sum);
                downstreamRework += impact;
            }
        }
        double totalRipple = reworkTotal + downstreamRework;
        if (totalRipple > 0.0) {
            work.addTestableWork(ProjectPhase.TESTING,
                    totalRipple * configuration.getScope().getRegressionDemandFactor());
        }
        return new ScopeChangeResult(Map.copyOf(added), Map.copyOf(ripple),
                addedTotal, totalRipple);
    }

    private double configValue(Map<String, Double> values, ProjectPhase phase, double fallback) {
        return values.getOrDefault(phase.name(), values.getOrDefault(
                phase.name().toLowerCase(), fallback));
    }

    public record ScopeChangeResult(Map<ProjectPhase, Double> addedWork,
                                    Map<ProjectPhase, Double> reworkByPhase,
                                    double addedScope, double reworkGenerated) {
        public ScopeChangeResult {
            addedWork = Map.copyOf(addedWork);
            reworkByPhase = Map.copyOf(reworkByPhase);
            if (!Double.isFinite(addedScope) || addedScope < 0.0
                    || !Double.isFinite(reworkGenerated) || reworkGenerated < 0.0) {
                throw new IllegalArgumentException("Scope-change results must be finite and non-negative");
            }
        }
    }
}
