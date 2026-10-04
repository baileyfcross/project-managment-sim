package edu.simulator.report;

import java.util.Objects;

public record ManagementDecisionRecord(int week, DecisionType decisionType,
                                       String previousValue, String newValue,
                                       String description, String relatedId) {
    public ManagementDecisionRecord {
        if (week < 0) {
            throw new IllegalArgumentException("Decision week cannot be negative");
        }
        Objects.requireNonNull(decisionType, "decisionType");
        Objects.requireNonNull(previousValue, "previousValue");
        Objects.requireNonNull(newValue, "newValue");
        Objects.requireNonNull(description, "description");
        relatedId = relatedId == null ? "" : relatedId;
    }
}
