package edu.simulator.event;

public class ProjectEvent {
    private final EventType type;
    private final String summary;
    private final int week;

    public ProjectEvent(EventType type, String summary, int week) {
        this.type = type;
        this.summary = summary;
        if (week < 1) {
            throw new IllegalArgumentException("An event must occur during a simulated week");
        }
        this.week = week;
    }

    public EventType getType() {
        return type;
    }

    public String getSummary() {
        return summary;
    }

    public int getWeek() {
        return week;
    }
}
