package edu.simulator.event;

import java.time.LocalDateTime;

public class ProjectEvent {
    private final EventType type;
    private final String summary;
    private final LocalDateTime createdAt;

    public ProjectEvent(EventType type, String summary) {
        this.type = type;
        this.summary = summary;
        this.createdAt = LocalDateTime.now();
    }

    public EventType getType() {
        return type;
    }

    public String getSummary() {
        return summary;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
