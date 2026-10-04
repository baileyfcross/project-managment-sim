package edu.simulator.report;

import java.util.List;
import java.util.Objects;

public record CausalFinding(String title, String summary, List<String> evidence,
                            int startingWeek, int consequenceWeek, int importance,
                            List<String> relatedDecisionIds) {
    public CausalFinding {
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(summary, "summary");
        evidence = List.copyOf(evidence);
        relatedDecisionIds = List.copyOf(relatedDecisionIds);
        if (startingWeek < 0 || consequenceWeek < startingWeek
                || importance < 1 || importance > 5) {
            throw new IllegalArgumentException("Causal finding timing and importance must be valid");
        }
    }
}
