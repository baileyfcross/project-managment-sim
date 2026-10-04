package edu.simulator.event;

import edu.simulator.model.ProjectPhase;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ProjectEvent {
    private final String id;
    private final EventType type;
    private final String title;
    private final String description;
    private final int week;
    private final List<EventOption> options;
    private final Map<ProjectPhase, Double> featureWork;
    private final String featureName;
    private final String impactEstimate;
    private final double complexityMultiplier;
    private final double customerValuePotential;
    private boolean resolved;
    private String selectedOption;
    private EventResult eventResult;

    public ProjectEvent(EventType type, String summary, int week) {
        this("legacy-" + type.name().toLowerCase() + "-" + week, type,
                type.name().replace('_', ' '), summary, week, List.of(), Map.of(),
                "", "Uncertain", 1.0, 0.0);
        this.resolved = true;
        this.eventResult = new EventResult(summary, week, Map.of());
    }

    public ProjectEvent(String id, EventType type, String title, String description,
                        int week, List<EventOption> options,
                        Map<ProjectPhase, Double> featureWork,
                        String featureName, String impactEstimate) {
        this(id, type, title, description, week, options, featureWork,
                featureName, impactEstimate, 1.0, 0.0);
    }

    public ProjectEvent(String id, EventType type, String title, String description,
                        int week, List<EventOption> options,
                        Map<ProjectPhase, Double> featureWork,
                        String featureName, String impactEstimate,
                        double complexityMultiplier, double customerValuePotential) {
        this.id = Objects.requireNonNull(id, "id");
        this.type = Objects.requireNonNull(type, "type");
        this.title = Objects.requireNonNull(title, "title");
        this.description = Objects.requireNonNull(description, "description");
        if (week < 1) {
            throw new IllegalArgumentException("An event must occur during a simulated week");
        }
        this.week = week;
        this.options = List.copyOf(options);
        EnumMap<ProjectPhase, Double> work = new EnumMap<>(ProjectPhase.class);
        featureWork.forEach((phase, amount) -> {
            if (amount == null || !Double.isFinite(amount) || amount < 0.0) {
                throw new IllegalArgumentException("Event feature work must be finite and non-negative");
            }
            work.put(phase, amount);
        });
        this.featureWork = Map.copyOf(work);
        this.featureName = Objects.requireNonNull(featureName, "featureName");
        this.impactEstimate = Objects.requireNonNull(impactEstimate, "impactEstimate");
        if (!Double.isFinite(complexityMultiplier) || complexityMultiplier <= 0.0
                || !Double.isFinite(customerValuePotential)
                || customerValuePotential < 0.0 || customerValuePotential > 1.0) {
            throw new IllegalArgumentException("Feature request complexity and value must be valid");
        }
        this.complexityMultiplier = complexityMultiplier;
        this.customerValuePotential = customerValuePotential;
    }

    public void resolve(String optionId, String visibleResult) {
        resolve(optionId, new EventResult(visibleResult, week, Map.of()));
    }

    public void resolve(String optionId, EventResult result) {
        if (resolved) {
            throw new IllegalStateException("Event " + id + " has already been resolved");
        }
        EventOption option = options.stream().filter(candidate -> candidate.id().equals(optionId))
                .findFirst().orElseThrow(() ->
                        new IllegalArgumentException("That choice is not available for event " + id));
        EventResult resolvedResult = Objects.requireNonNull(result, "result");
        resolved = true;
        selectedOption = option.id();
        eventResult = resolvedResult;
    }

    public String getId() { return id; }
    public EventType getType() { return type; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getSummary() { return getResult() == null ? description : getResult(); }
    public int getWeek() { return week; }
    public List<EventOption> getOptions() { return options; }
    public Map<ProjectPhase, Double> getFeatureWork() { return featureWork; }
    public String getFeatureName() { return featureName; }
    public String getImpactEstimate() { return impactEstimate; }
    public double getComplexityMultiplier() { return complexityMultiplier; }
    public double getCustomerValuePotential() { return customerValuePotential; }
    public boolean isResolved() { return resolved; }
    public String getSelectedOption() { return selectedOption; }
    public String getResult() { return eventResult == null ? null : eventResult.visibleMessage(); }
    public EventResult getEventResult() { return eventResult; }
    public boolean isBlocking() { return !resolved && !options.isEmpty(); }
}
