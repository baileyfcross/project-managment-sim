package edu.simulator.report;

import edu.simulator.configuration.ScenarioConfiguration;
import edu.simulator.event.EventDecision;
import edu.simulator.event.ProjectEvent;
import edu.simulator.model.Project;
import edu.simulator.model.ProjectPhase;
import edu.simulator.model.Role;
import edu.simulator.model.Team;
import edu.simulator.model.WeeklySnapshot;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

public class FinalReportService {
    private static final List<String> REFLECTION_PROMPTS = List.of(
            "Which decision helped your project the most?",
            "Which decision created the largest unintended consequence?",
            "When did perceived progress differ most from true progress?",
            "What would you change if you ran the same seed again?");

    private final ScoringService scoringService = new ScoringService();
    private final CausalAnalysisService causalAnalysisService = new CausalAnalysisService();

    public FinalProjectReport createReport(ScenarioConfiguration scenario, Project project,
                                           Team team, List<WeeklySnapshot> history,
                                           List<ProjectEvent> events,
                                           List<EventDecision> eventDecisions,
                                           List<ManagementDecisionRecord> decisions,
                                           Map<Role, Integer> initialTeam,
                                           TerminationReason terminationReason,
                                           long seed, int totalTurnover,
                                           int releasedDefects, BigDecimal hiringCost) {
        List<WeeklySnapshot> immutableHistory = List.copyOf(history);
        validateHistory(project.getCurrentWeek(), immutableHistory);
        WeeklySnapshot last = immutableHistory.get(immutableHistory.size() - 1);
        Map<String, Integer> initialCounts = new LinkedHashMap<>();
        for (Role role : Role.values()) {
            initialCounts.put(role.name(), initialTeam.getOrDefault(role, 0));
        }

        boolean released = terminationReason == TerminationReason.RELEASED;
        FinalProjectReport.Outcome outcome = outcome(
                terminationReason, project.getCurrentWeek(),
                scenario.getDeadlineWeeks());
        double averageFatigue = immutableHistory.stream()
                .mapToDouble(WeeklySnapshot::getAverageFatigue).average().orElse(0.0);
        double peakFatigue = immutableHistory.stream()
                .mapToDouble(WeeklySnapshot::getMaximumFatigue).max().orElse(0.0);
        double averageMorale = immutableHistory.stream()
                .mapToDouble(WeeklySnapshot::getAverageMorale).average().orElse(0.0);
        double unknownRework = project.getWorkState().totalUnknownRework();
        double knownRework = project.getWorkState().totalKnownRework();
        double testingBacklog = project.getWorkState().getTotalTestingBacklog();
        int accepted = countFeatureChoice(events, "ACCEPT");
        int deferred = countFeatureChoice(events, "DEFER");
        int rejected = countFeatureChoice(events, "REJECT");

        FinalProjectReport.ScoreSummary scores = scoringService.score(
                new ScoringService.ScoringInput(
                        released, project.getCurrentWeek(), scenario.getDeadlineWeeks(),
                        project.getSpent(), scenario.getBudget(),
                        project.calculateTrueProgress(), project.getWorkState().totalScope(),
                        releasedDefects, unknownRework, knownRework, testingBacklog,
                        project.getTechnicalDebt(), totalTurnover,
                        initialTeam.values().stream().mapToInt(Integer::intValue).sum(),
                        immutableHistory, events, eventDecisions));

        FinalProjectReport.RunMetadata metadata = new FinalProjectReport.RunMetadata(
                scenario.getId(), scenario.getName(), Long.toString(seed),
                System.getProperty("simulator.version", "development"),
                "unversioned-defaults", scenario.getDeadlineWeeks(),
                scenario.getBudget(), project.getCurrentWeek(), terminationReason, initialCounts);
        FinalProjectReport.FinalMetrics metrics = new FinalProjectReport.FinalMetrics(
                project.getSpent(), last.getPerceivedProgress(), last.getTrueProgress(),
                releasedDefects, knownRework, unknownRework, testingBacklog,
                project.getTechnicalDebt(), team.totalCount(), totalTurnover, peakFatigue,
                averageFatigue, averageMorale, project.getScopeExpansionRatio(),
                accepted, deferred, rejected, hiringCost);

        List<ManagementDecisionRecord> orderedDecisions = decisions.stream()
                .sorted(Comparator.comparingInt(ManagementDecisionRecord::week)
                        .thenComparing(decision -> decision.decisionType().name())
                        .thenComparing(ManagementDecisionRecord::description))
                .toList();
        List<CausalFinding> findings = causalAnalysisService.analyze(
                immutableHistory, orderedDecisions, events);
        FinalProjectReport.DecisionSummary decisionSummary =
                new FinalProjectReport.DecisionSummary(orderedDecisions.size(),
                        countsByDecision(orderedDecisions), orderedDecisions,
                        decisionReviews(immutableHistory, orderedDecisions, findings));
        FinalProjectReport.EventSummary eventSummary = new FinalProjectReport.EventSummary(
                events.size(), (int) events.stream().filter(ProjectEvent::isResolved).count(),
                countsByEvent(events), eventDetails(events));

        return new FinalProjectReport(metadata, outcome, scores, metrics,
                decisionSummary, eventSummary, timeline(events, orderedDecisions),
                findings,
                charts(immutableHistory, scenario.getBudget()),
                instructorWeeks(immutableHistory), REFLECTION_PROMPTS);
    }

    private FinalProjectReport.Outcome outcome(
            TerminationReason reason, int actualWeek, int deadline) {
        return switch (reason) {
            case RELEASED -> actualWeek <= deadline
                    ? new FinalProjectReport.Outcome("COMPLETED", "Completed on time", true)
                    : new FinalProjectReport.Outcome("COMPLETED_LATE", "Completed late", true);
            case DEADLINE_REACHED -> new FinalProjectReport.Outcome(
                    "INCOMPLETE_AT_DEADLINE", "Incomplete at deadline", false);
            case BUDGET_EXHAUSTED -> new FinalProjectReport.Outcome(
                    "BUDGET_EXHAUSTED", "Budget exhausted", false);
        };
    }

    private void validateHistory(int finalWeek, List<WeeklySnapshot> history) {
        if (finalWeek < 1 || history.size() != finalWeek) {
            throw new IllegalStateException("Final report requires exactly one snapshot per simulated week");
        }
        for (int index = 0; index < history.size(); index++) {
            if (history.get(index).getWeek() != index + 1) {
                throw new IllegalStateException("Weekly history must be contiguous and ordered");
            }
        }
    }

    private int countFeatureChoice(List<ProjectEvent> events, String option) {
        return (int) events.stream().filter(event ->
                event.getType().name().equals("CUSTOMER_FEATURE_REQUEST")
                        && option.equals(event.getSelectedOption())).count();
    }

    private Map<String, Integer> countsByDecision(List<ManagementDecisionRecord> decisions) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (DecisionType type : DecisionType.values()) {
            counts.put(type.name(), (int) decisions.stream()
                    .filter(decision -> decision.decisionType() == type).count());
        }
        return Map.copyOf(counts);
    }

    private List<FinalProjectReport.DecisionReview> decisionReviews(
            List<WeeklySnapshot> history, List<ManagementDecisionRecord> decisions,
            List<CausalFinding> findings) {
        return decisions.stream().map(decision -> {
            WeeklySnapshot first = snapshotAt(history, Math.max(1, decision.week() + 1));
            WeeklySnapshot later = snapshotAt(history, Math.min(
                    history.size(), Math.max(1, decision.week() + 3)));
            String immediate = decision.description();
            if (decision.decisionType() != DecisionType.FEATURE_DECISION
                    && decision.decisionType() != DecisionType.EVENT_DECISION) {
                immediate += " The selection was made in Week " + decision.week()
                        + " and applied to subsequent work.";
            }
            List<String> evidence = new ArrayList<>();
            String consequence;
            CausalFinding related = findings.stream()
                    .filter(finding -> !decision.relatedId().isBlank()
                            && finding.relatedDecisionIds().contains(decision.relatedId()))
                    .findFirst().orElse(null);
            consequence = related == null
                    ? compareDecisionHistory(decision, first, later, evidence)
                    : related.summary();
            if (related != null) {
                evidence.addAll(related.evidence());
            }
            return new FinalProjectReport.DecisionReview(
                    decision, immediate, consequence, evidence);
        }).toList();
    }

    private String compareDecisionHistory(ManagementDecisionRecord decision,
                                          WeeklySnapshot first, WeeklySnapshot later,
                                          List<String> evidence) {
        if (first == null || later == null || later.getWeek() <= first.getWeek()) {
            return "The run ended before a later weekly consequence could be compared.";
        }
        double start;
        double end;
        String label;
        switch (decision.decisionType()) {
            case WORK_INTENSITY -> {
                start = first.getAverageFatigue();
                end = later.getAverageFatigue();
                label = "Average fatigue";
            }
            case TESTING_PRIORITY -> {
                start = first.getTestingBacklog();
                end = later.getTestingBacklog();
                label = "Testing backlog";
            }
            case HIRING -> {
                start = first.getTeamSize();
                end = later.getTeamSize();
                label = "Active team size";
                evidence.add("Onboarding counts in the selected weeks: "
                        + first.getOnboardingEmployees() + " then "
                        + later.getOnboardingEmployees() + ".");
            }
            case CONCURRENCY -> {
                start = first.getPhaseFive().dependencyUncertainty();
                end = later.getPhaseFive().dependencyUncertainty();
                label = "Dependency uncertainty";
            }
            case ENGINEERING_APPROACH, TECHNICAL_DEBT_PRIORITY -> {
                start = first.getPhaseFive().technicalDebt();
                end = later.getPhaseFive().technicalDebt();
                label = "Technical debt";
            }
            case FEATURE_DECISION -> {
                start = first.getPhaseFive().scopeChangeRework();
                end = later.getPhaseFive().scopeChangeRework();
                label = "Recorded scope-change rework";
            }
            case EVENT_DECISION -> {
                return "The selected event outcome is recorded in the event history; later effects are reflected in the weekly charts.";
            }
            default -> throw new IllegalStateException(
                    "Unsupported decision type: " + decision.decisionType());
        }
        evidence.add(label + " was " + number(start) + " in Week " + first.getWeek()
                + " and " + number(end) + " in Week " + later.getWeek() + ".");
        if (Math.abs(end - start) < 0.01) {
            return "No material change in " + label.toLowerCase()
                    + " was recorded over the following weeks.";
        }
        return label + (end > start ? " increased" : " decreased")
                + " over the following weeks; this is an observed association, not proof the decision alone caused it.";
    }

    private String number(double value) {
        return String.format(java.util.Locale.ROOT, "%.1f", value);
    }

    private WeeklySnapshot snapshotAt(List<WeeklySnapshot> history, int week) {
        return history.stream().filter(snapshot -> snapshot.getWeek() == week)
                .findFirst().orElse(null);
    }

    private Map<String, Integer> countsByEvent(List<ProjectEvent> events) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        events.stream().collect(Collectors.groupingBy(
                        event -> event.getType().name(), Collectors.summingInt(event -> 1)))
                .entrySet().stream().sorted(Map.Entry.comparingByKey())
                .forEach(entry -> counts.put(entry.getKey(), entry.getValue()));
        return Map.copyOf(counts);
    }

    private List<FinalProjectReport.EventDetail> eventDetails(List<ProjectEvent> events) {
        return events.stream().map(event -> {
            var result = event.getEventResult();
            Map<String, Double> effects = result == null ? Map.of() : result.internalEffects();
            return new FinalProjectReport.EventDetail(
                    event.getId(), event.getType().name(), event.getTitle(), event.getWeek(),
                    result == null ? 0 : result.resolutionWeek(),
                    event.getSelectedOption(), event.getResult(), effects,
                    phaseMap(event.getFeatureWork()), event.getCustomerValuePotential());
        }).toList();
    }

    private List<FinalProjectReport.TimelineEntry> timeline(
            List<ProjectEvent> events, List<ManagementDecisionRecord> decisions) {
        List<FinalProjectReport.TimelineEntry> entries = new ArrayList<>();
        for (ProjectEvent event : events) {
            entries.add(new FinalProjectReport.TimelineEntry(event.getWeek(), "EVENT",
                    event.getTitle(), event.getDescription(), event.getId()));
            if (event.getEventResult() != null
                    && event.getEventResult().resolutionWeek() > event.getWeek()) {
                entries.add(new FinalProjectReport.TimelineEntry(
                        event.getEventResult().resolutionWeek(), "EVENT_RESOLUTION",
                        "Resolved: " + event.getTitle(), event.getResult(), event.getId()));
            }
        }
        for (ManagementDecisionRecord decision : decisions) {
            entries.add(new FinalProjectReport.TimelineEntry(decision.week(),
                    decision.decisionType().name(), decision.description(),
                    decision.previousValue().isBlank() ? decision.newValue()
                            : decision.previousValue() + " -> " + decision.newValue(),
                    decision.relatedId()));
        }
        return entries.stream().sorted(Comparator.comparingInt(FinalProjectReport.TimelineEntry::week)
                .thenComparing(FinalProjectReport.TimelineEntry::type)
                .thenComparing(FinalProjectReport.TimelineEntry::title)).toList();
    }

    private List<FinalProjectReport.Chart> charts(List<WeeklySnapshot> history,
                                                  BigDecimal budget) {
        List<FinalProjectReport.Chart> result = new ArrayList<>();
        result.add(chart("progress", "Perceived and true progress",
                "Progress percentage by simulated week. True progress uses correctly completed work.",
                history, series(history, "Perceived progress", "%", WeeklySnapshot::getPerceivedProgress, 100.0),
                series(history, "True progress", "%", WeeklySnapshot::getTrueProgress, 100.0)));
        result.add(chart("budget", "Cumulative cost",
                "Cumulative spending compared with the approved project budget.",
                history, series(history, "Cumulative cost", "USD",
                        snapshot -> snapshot.getSpent().doubleValue(), 1.0),
                series(history, "Approved budget", "USD",
                        snapshot -> budget.doubleValue(), 1.0)));
        result.add(chart("quality", "Quality and rework",
                "Known and unknown rework stocks and defects discovered each week.",
                history, series(history, "Known rework", "work units", WeeklySnapshot::getKnownRework, 1.0),
                series(history, "Unknown rework", "work units", WeeklySnapshot::getUnknownRework, 1.0),
                series(history, "Defects discovered", "work units",
                        snapshot -> sum(snapshot.getDefectsDiscoveredByPhase()), 1.0)));
        result.add(chart("team-health", "Team health",
                "Average fatigue and morale by week, each on a zero to one scale.",
                history, series(history, "Average fatigue", "0-1",
                        WeeklySnapshot::getAverageFatigue, 1.0),
                series(history, "Average morale", "0-1", WeeklySnapshot::getAverageMorale, 1.0)));
        result.add(chart("team-size", "Active team size",
                "Number of active team members recorded at each week end.",
                history, series(history, "Team size", "people",
                        snapshot -> snapshot.getTeamSize(), 1.0)));
        result.add(chart("technical-debt", "Technical debt",
                "Technical debt level by week on a zero to one scale.",
                history, series(history, "Technical debt", "0-1",
                        snapshot -> snapshot.getPhaseFive().technicalDebt(), 1.0)));
        result.add(chart("schedule-pressure", "Schedule pressure",
                "Manager-visible schedule pressure by week on a zero to one scale.",
                history, series(history, "Schedule pressure", "0-1",
                        WeeklySnapshot::getSchedulePressure, 1.0)));
        return List.copyOf(result);
    }

    private FinalProjectReport.Chart chart(String id, String title, String description,
                                           List<WeeklySnapshot> history,
                                           FinalProjectReport.ChartSeries... series) {
        return new FinalProjectReport.Chart(id, title,
                history.isEmpty() ? description + " No weekly data is available." : description,
                List.of(series));
    }

    private FinalProjectReport.ChartSeries series(List<WeeklySnapshot> history, String name,
                                                  String unit,
                                                  ToDoubleFunction<WeeklySnapshot> measure,
                                                  double multiplier) {
        List<FinalProjectReport.ChartPoint> points = history.stream()
                .map(snapshot -> new FinalProjectReport.ChartPoint(
                        snapshot.getWeek(), finiteNonNegative(measure.applyAsDouble(snapshot) * multiplier)))
                .toList();
        return new FinalProjectReport.ChartSeries(name, unit, points);
    }

    private double sum(Map<ProjectPhase, Double> values) {
        return values.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    private List<FinalProjectReport.InstructorWeek> instructorWeeks(
            List<WeeklySnapshot> history) {
        return history.stream().map(snapshot -> new FinalProjectReport.InstructorWeek(
                snapshot.getWeek(),
                new FinalProjectReport.StudentView(
                        snapshot.getPerceivedProgress(), snapshot.getSchedulePressure(),
                        snapshot.getSpent(), snapshot.getKnownRework(),
                        snapshot.getTestingBacklog(), snapshot.getTeamSize(),
                        snapshot.getQualityHealth(),
                        new edu.simulator.simulation.FatigueModel().category(
                                snapshot.getAverageFatigue()),
                        phaseMap(snapshot.getPerceivedProgressByPhase()),
                        snapshot.getTestingPriority().name(), snapshot.getWorkIntensity().name(),
                        snapshot.getPhaseFive().concurrencyPolicy().name(),
                        snapshot.getPhaseFive().engineeringApproach().name(),
                        snapshot.getPhaseFive().technicalDebtPriority().name(),
                        snapshot.getPhaseFive().scopeExpansion(),
                        snapshot.getRecentMessages()),
                new FinalProjectReport.HiddenView(
                        snapshot.getTrueProgress(), snapshot.getAverageFatigue(),
                        snapshot.getMaximumFatigue(), snapshot.getAverageMorale(),
                        snapshot.getUnknownRework(), snapshot.getKnownRework(),
                        phaseMap(snapshot.getUnknownReworkByPhase()),
                        phaseMap(snapshot.getKnownReworkByPhase()),
                        phaseMap(snapshot.getTrueProgressByPhase()),
                        phaseMap(snapshot.getPerceivedProgressByPhase()),
                        phaseMap(snapshot.getCorrectWorkByPhase()),
                        phaseMap(snapshot.getDefectsCreatedByPhase()),
                        phaseMap(snapshot.getDefectsDiscoveredByPhase()),
                        snapshot.getTestingBacklog(), snapshot.getQaCapacity(),
                        snapshot.getAverageProductivity(), snapshot.getMentoringCoverage(),
                        snapshot.getCoordinationOverhead(), snapshot.getTurnoverRisk(),
                        snapshot.getPhaseFive().technicalDebt(),
                        snapshot.getPhaseFive().dependencyUncertainty(),
                        snapshot.getPhaseFive().outOfSequenceWork())))
                .toList();
    }

    private Map<String, Double> phaseMap(Map<ProjectPhase, Double> values) {
        Map<String, Double> result = new LinkedHashMap<>();
        for (ProjectPhase phase : ProjectPhase.values()) {
            result.put(phase.name(), finiteNonNegative(values.getOrDefault(phase, 0.0)));
        }
        return Map.copyOf(result);
    }

    private double finiteNonNegative(double value) {
        return Double.isFinite(value) ? Math.max(0.0, value) : 0.0;
    }
}
