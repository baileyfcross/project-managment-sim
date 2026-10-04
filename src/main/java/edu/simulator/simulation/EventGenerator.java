package edu.simulator.simulation;

import edu.simulator.configuration.PhaseFiveConfiguration;
import edu.simulator.configuration.ScenarioConfiguration;
import edu.simulator.event.EventOption;
import edu.simulator.event.EventType;
import edu.simulator.event.ProjectEvent;
import edu.simulator.model.ConcurrencyPolicy;
import edu.simulator.model.EngineeringApproach;
import edu.simulator.model.Project;
import edu.simulator.model.ProjectPhase;
import edu.simulator.model.Role;
import edu.simulator.model.Team;
import edu.simulator.model.TechnicalDebtPriority;
import edu.simulator.model.WorkIntensity;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.HashSet;
import java.util.Set;

public class EventGenerator {
    private static final List<String> FEATURE_NAMES = List.of(
            "Advanced reporting", "Account authentication", "Mobile-friendly checkout",
            "Automated notifications", "Data export tools");
    private final Set<String> requestedFeatureNames = new HashSet<>();

    public ProjectEvent generate(int week, String eventId, Project project, Team team,
                                 ConcurrencyPolicy concurrency, EngineeringApproach approach,
                                 TechnicalDebtPriority debtPriority, WorkIntensity intensity,
                                 ScenarioConfiguration scenario, PhaseFiveConfiguration config,
                                 Random random) {
        var settings = config.getEvents();
        double elapsed = Math.min(1.0, (double) week / scenario.getDeadlineWeeks());
        double rate = settings.getBaseProbability()
                * settings.getStakeholderVolatility() * scenario.getStakeholderVolatility();
        if (random.nextDouble() >= Math.min(0.95, rate)) {
            return null;
        }

        Map<EventType, Double> weights = contextualWeights(
                project, team, concurrency, approach, debtPriority, intensity,
                scenario, config, elapsed);
        EventType selected = weightedChoice(weights, random);
        if (selected == null) {
            return null;
        }
        if (selected == EventType.CUSTOMER_FEATURE_REQUEST) {
            ProjectEvent request = featureRequest(
                    week, eventId, project, scenario, config, random, elapsed);
            if (request == null) {
                return null;
            }
            requestedFeatureNames.add(request.getFeatureName());
            return request;
        }
        return contextualEvent(week, eventId, selected, project, config);
    }

    private Map<EventType, Double> contextualWeights(
            Project project, Team team, ConcurrencyPolicy concurrency,
            EngineeringApproach approach, TechnicalDebtPriority debtPriority,
            WorkIntensity intensity, ScenarioConfiguration scenario,
            PhaseFiveConfiguration config, double elapsed) {
        var work = project.getWorkState();
        double debt = project.getTechnicalDebt();
        double developmentProgress = work.perceivedPhaseProgress(ProjectPhase.DEVELOPMENT);
        double upstreamInstability = 1.0 - work.perceivedPhaseProgress(ProjectPhase.DESIGN);
        double hiddenDefectRatio = work.totalUnknownRework()
                / Math.max(1.0, work.totalScope());
        double fatigue = team.activeEmployees().stream().mapToDouble(
                edu.simulator.model.Employee::getFatigue).average().orElse(0.0);
        var eventSettings = config.getEvents();
        double devopsCoverage = team.count(Role.DEVOPS_ENGINEER) == 0 ? 1.0
                : 1.0 / (1.0 + team.count(Role.DEVOPS_ENGINEER)
                        * config.getEvents().getDevopsCoverageWeight());
        EnumMap<EventType, Double> result = new EnumMap<>(EventType.class);
        Map<String, Double> base = config.getEvents().getEventWeights();
        for (Map.Entry<String, Double> entry : base.entrySet()) {
            EventType type = EventType.valueOf(entry.getKey().toUpperCase());
            double weight = entry.getValue();
            if (scenario.getEventWeights().containsKey(type.name())) {
                weight *= scenario.getEventWeights().get(type.name());
            }
            switch (type) {
                case CUSTOMER_FEATURE_REQUEST ->
                    weight *= config.getEvents().getFeatureRequestRate()
                            * scenario.getFeatureRequestRate()
                            * (eventSettings.getFeatureTimingFloor() + elapsed);
                case REQUIREMENTS_MISUNDERSTANDING ->
                    weight *= config.getEvents().getProjectComplexity()
                            * scenario.getProjectComplexity()
                            * (eventSettings.getRequirementComplexityFloor()
                            + developmentProgress * eventSettings.getRequirementProgressWeight()
                            + elapsed * eventSettings.getLateRequirementWeight());
                case DEPENDENCY_PROBLEM ->
                    weight *= config.getEvents().getDependencyRisk() * scenario.getDependencyRisk()
                            * (1.0 + (concurrency == ConcurrencyPolicy.AGGRESSIVE
                                    ? eventSettings.getDependencyConcurrencyWeight() * upstreamInstability : 0.0)
                            + eventSettings.getDependencyInstabilityWeight() * upstreamInstability
                            + eventSettings.getDependencyPressureWeight() * estimatePressure(project));
                case FAILED_INTEGRATION ->
                    weight *= (eventSettings.getIntegrationProgressFloor() + developmentProgress)
                            * devopsCoverage * (1.0 + eventSettings.getIntegrationDebtWeight() * debt
                            + eventSettings.getIntegrationConcurrencyWeight()
                                * (concurrency == ConcurrencyPolicy.AGGRESSIVE ? upstreamInstability : 0.0)
                            + eventSettings.getIntegrationHiddenDefectWeight() * hiddenDefectRatio);
                case SECURITY_VULNERABILITY ->
                    weight *= eventSettings.getSecurityBaseWeight()
                            + eventSettings.getSecurityDebtWeight() * debt
                            + (approach == EngineeringApproach.CUT_CORNERS
                                    ? eventSettings.getSecurityCutCornersWeight() : 0.0)
                            + eventSettings.getSecurityFatigueWeight() * fatigue
                            + (intensity == WorkIntensity.CRUNCH
                                    ? eventSettings.getSecurityCrunchWeight() : 0.0);
                case TECHNICAL_DEBT_ISSUE ->
                    weight *= config.getEvents().getTechnicalDebtSensitivity()
                            * scenario.getTechnicalDebtSensitivity()
                            * (eventSettings.getDebtIssueFloor()
                            + debt * eventSettings.getDebtIssueWeight()
                            + (debtPriority == TechnicalDebtPriority.IGNORE
                                    ? eventSettings.getIgnoredDebtWeight() : 0.0));
                default -> weight = 0.0;
            }
            if (weight > 0.0) {
                result.put(type, weight);
            }
        }
        return result;
    }

    private double estimatePressure(Project project) {
        return project.getAverageSchedulePressure();
    }

    private EventType weightedChoice(Map<EventType, Double> weights, Random random) {
        double total = weights.values().stream().mapToDouble(Double::doubleValue).sum();
        if (!Double.isFinite(total) || total <= 0.0) {
            return null;
        }
        double selected = random.nextDouble() * total;
        for (Map.Entry<EventType, Double> entry : weights.entrySet()) {
            selected -= entry.getValue();
            if (selected < 0.0) {
                return entry.getKey();
            }
        }
        return weights.keySet().stream().findFirst().orElse(null);
    }

    private ProjectEvent featureRequest(int week, String id, Project project,
                                        ScenarioConfiguration scenario,
                                        PhaseFiveConfiguration config, Random random,
                                        double elapsed) {
        List<String> availableNames = FEATURE_NAMES.stream()
                .filter(name -> !requestedFeatureNames.contains(name)).toList();
        if (availableNames.isEmpty()) {
            return null;
        }
        String name = availableNames.get(random.nextInt(availableNames.size()));
        double complexityMultiplier = 0.85 + random.nextDouble() * 0.3;
        double complexityFactor = complexityMultiplier
                * Math.max(0.25, Math.min(2.0, scenario.getProjectComplexity()
                        * config.getEvents().getProjectComplexity()));
        EnumMap<ProjectPhase, Double> work = new EnumMap<>(ProjectPhase.class);
        config.getScope().getFeatureWorkFractions().forEach((key, fraction) -> {
            ProjectPhase phase = ProjectPhase.valueOf(key.toUpperCase());
            work.put(phase, project.getWorkState().getTotalWork(phase) * fraction * complexityFactor);
        });
        double visibleProgress = project.calculatePerceivedProgress();
        String impact = visibleProgress < 0.25 ? "Likely low disruption"
                : visibleProgress < 0.65 ? "Moderate rework risk" : "Significant rework risk";
        return new ProjectEvent(id, EventType.CUSTOMER_FEATURE_REQUEST,
                "Customer feature request", "The customer requested " + name.toLowerCase()
                        + " for the current product. The feature may add customer value.",
                week, List.of(
                new EventOption("ACCEPT", "Accept", "Add the feature to this release."),
                new EventOption("DEFER", "Defer", "Move the feature to a future release."),
                new EventOption("REJECT", "Reject", "Decline the request.")
        ), work, name, impact, complexityMultiplier,
                0.5 + random.nextDouble() * 0.5);
    }

    private ProjectEvent contextualEvent(int week, String id, EventType type, Project project,
                                         PhaseFiveConfiguration config) {
        List<EventOption> options = new ArrayList<>();
        String title;
        String description;
        switch (type) {
            case REQUIREMENTS_MISUNDERSTANDING -> {
                title = "Requirements misunderstanding";
                description = "A stakeholder clarified a requirement after design work had begun.";
                options.add(new EventOption("REVISE", "Revise the design", "Update affected work now."));
                options.add(new EventOption("KEEP", "Keep the current direction", "Avoid immediate work, accepting risk."));
            }
            case DEPENDENCY_PROBLEM -> {
                title = "Dependency problem";
                description = "A third-party library no longer behaves as expected.";
                options.add(new EventOption("REFACTOR", "Refactor around it", "Spend capacity on a cleaner solution."));
                options.add(new EventOption("WORKAROUND", "Use a workaround", "Keep delivery moving with a shortcut."));
            }
            case FAILED_INTEGRATION -> {
                title = "Integration problem";
                description = "Recently combined components need additional stabilization.";
                options.add(new EventOption("STABILIZE", "Stabilize now", "Add integration and regression work."));
                options.add(new EventOption("DEFER", "Defer stabilization", "Keep moving and accept more technical risk."));
            }
            case SECURITY_VULNERABILITY -> {
                title = "Security review";
                description = "A review found a vulnerability that should be addressed before release.";
                options.add(new EventOption("FIX", "Fix now", "Add development and testing work."));
                options.add(new EventOption("TEMPORARY_FIX", "Use a temporary workaround", "Reduce immediate exposure, with future cleanup."));
            }
            case TECHNICAL_DEBT_ISSUE -> {
                title = "Technical debt issue";
                description = "Shortcuts in the code are making changes harder to deliver.";
                options.add(new EventOption("REFACTOR", "Refactor now", "Invest capacity in improving the code."));
                options.add(new EventOption("DEFER", "Defer cleanup", "Preserve near-term capacity."));
            }
            default -> throw new IllegalArgumentException("Unsupported generated event type: " + type);
        }
        return new ProjectEvent(id, type, title, description, week, options,
                Map.of(), "", "Impact depends on your choice");
    }
}
