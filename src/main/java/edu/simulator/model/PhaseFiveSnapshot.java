package edu.simulator.model;

import java.util.List;

public record PhaseFiveSnapshot(
        ConcurrencyPolicy concurrencyPolicy,
        EngineeringApproach engineeringApproach,
        TechnicalDebtPriority technicalDebtPriority,
        double technicalDebt,
        double scopeExpansion,
        int acceptedFeatureCount,
        int rejectedFeatureCount,
        int deferredFeatureCount,
        List<String> eventsGenerated,
        List<String> eventDecisions,
        double outOfSequenceWork,
        double dependencyUncertainty,
        double scopeChangeRework,
        double eventCreatedWork) {
    public PhaseFiveSnapshot {
        if (concurrencyPolicy == null || engineeringApproach == null || technicalDebtPriority == null) {
            throw new IllegalArgumentException("Phase 5 snapshot policies are required");
        }
        technicalDebt = SimulationValues.unitInterval(technicalDebt);
        scopeExpansion = SimulationValues.nonNegativeFinite(scopeExpansion);
        outOfSequenceWork = SimulationValues.nonNegativeFinite(outOfSequenceWork);
        dependencyUncertainty = SimulationValues.unitInterval(dependencyUncertainty);
        scopeChangeRework = SimulationValues.nonNegativeFinite(scopeChangeRework);
        eventCreatedWork = SimulationValues.nonNegativeFinite(eventCreatedWork);
        eventsGenerated = List.copyOf(eventsGenerated);
        eventDecisions = List.copyOf(eventDecisions);
    }
}
