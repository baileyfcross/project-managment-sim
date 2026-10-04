package edu.simulator.event;

import java.util.Objects;

public record EventOption(String id, String label, String description) {
    public EventOption {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(description, "description");
    }
}
