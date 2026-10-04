package edu.simulator.event;

import java.util.Map;
import java.util.Objects;

public record EventResult(String visibleMessage, int resolutionWeek,
                          Map<String, Double> internalEffects) {
    public EventResult {
        Objects.requireNonNull(visibleMessage, "visibleMessage");
        if (resolutionWeek < 0) {
            throw new IllegalArgumentException("Event resolution week cannot be negative");
        }
        Objects.requireNonNull(internalEffects, "internalEffects");
        if (internalEffects.values().stream().anyMatch(value -> value == null
                || !Double.isFinite(value))) {
            throw new IllegalArgumentException("Event effects must be finite");
        }
        internalEffects = Map.copyOf(internalEffects);
    }
}
