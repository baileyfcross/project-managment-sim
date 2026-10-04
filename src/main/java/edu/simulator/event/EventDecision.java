package edu.simulator.event;

import java.util.Objects;

public record EventDecision(String eventId, String optionId, String optionLabel,
                            int week, String result) {
    public EventDecision {
        Objects.requireNonNull(eventId, "eventId");
        Objects.requireNonNull(optionId, "optionId");
        Objects.requireNonNull(optionLabel, "optionLabel");
        Objects.requireNonNull(result, "result");
        if (week < 0) {
            throw new IllegalArgumentException("Decision week cannot be negative");
        }
    }
}
