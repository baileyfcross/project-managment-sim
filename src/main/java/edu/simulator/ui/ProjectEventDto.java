package edu.simulator.ui;

import edu.simulator.event.EventOption;
import edu.simulator.event.ProjectEvent;

import java.util.List;

public class ProjectEventDto {
    private final String id;
    private final String type;
    private final String title;
    private final String description;
    private final int week;
    private final List<EventOption> options;
    private final String impactEstimate;
    private final boolean resolved;
    private final String selectedOption;
    private final String result;

    public ProjectEventDto(ProjectEvent event) {
        id = event.getId();
        type = event.getType().name();
        title = event.getTitle();
        description = event.getDescription();
        week = event.getWeek();
        options = event.getOptions();
        impactEstimate = event.getImpactEstimate();
        resolved = event.isResolved();
        selectedOption = event.getSelectedOption();
        result = event.getResult();
    }

    public String getId() { return id; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getWeek() { return week; }
    public List<EventOption> getOptions() { return options; }
    public String getImpactEstimate() { return impactEstimate; }
    public boolean isResolved() { return resolved; }
    public String getSelectedOption() { return selectedOption; }
    public String getResult() { return result; }
}
