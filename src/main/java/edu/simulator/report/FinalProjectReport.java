package edu.simulator.report;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class FinalProjectReport {
    private final RunMetadata metadata;
    private final Outcome outcome;
    private final ScoreSummary scores;
    private final FinalMetrics metrics;
    private final DecisionSummary decisionSummary;
    private final EventSummary eventSummary;
    private final List<TimelineEntry> timeline;
    private final List<CausalFinding> findings;
    private final List<Chart> charts;
    private final List<InstructorWeek> instructorWeeks;
    private final List<String> reflectionPrompts;

    public FinalProjectReport(RunMetadata metadata, Outcome outcome, ScoreSummary scores,
                              FinalMetrics metrics, DecisionSummary decisionSummary,
                              EventSummary eventSummary, List<TimelineEntry> timeline,
                              List<CausalFinding> findings, List<Chart> charts,
                              List<InstructorWeek> instructorWeeks,
                              List<String> reflectionPrompts) {
        this.metadata = Objects.requireNonNull(metadata, "metadata");
        this.outcome = Objects.requireNonNull(outcome, "outcome");
        this.scores = Objects.requireNonNull(scores, "scores");
        this.metrics = Objects.requireNonNull(metrics, "metrics");
        this.decisionSummary = Objects.requireNonNull(decisionSummary, "decisionSummary");
        this.eventSummary = Objects.requireNonNull(eventSummary, "eventSummary");
        this.timeline = List.copyOf(timeline);
        this.findings = List.copyOf(findings);
        this.charts = List.copyOf(charts);
        this.instructorWeeks = List.copyOf(instructorWeeks);
        this.reflectionPrompts = List.copyOf(reflectionPrompts);
    }

    public RunMetadata getMetadata() { return metadata; }
    public Outcome getOutcome() { return outcome; }
    public ScoreSummary getScores() { return scores; }
    public FinalMetrics getMetrics() { return metrics; }
    public DecisionSummary getDecisionSummary() { return decisionSummary; }
    public EventSummary getEventSummary() { return eventSummary; }
    public List<TimelineEntry> getTimeline() { return timeline; }
    public List<CausalFinding> getFindings() { return findings; }
    public List<Chart> getCharts() { return charts; }
    public List<InstructorWeek> getInstructorWeeks() { return instructorWeeks; }
    public List<String> getReflectionPrompts() { return reflectionPrompts; }

    public int getActualWeek() { return metadata.finalWeek(); }
    public int getDeadline() { return metadata.deadlineWeeks(); }
    public BigDecimal getActualBudget() { return metrics.totalSpent(); }
    public BigDecimal getTargetBudget() { return metadata.budget(); }
    public int getDefectsReleased() { return metrics.releasedDefects(); }
    public int getTeamTurnover() { return metrics.departures(); }
    public double getPeakFatigue() { return metrics.peakFatigue(); }
    public double getAverageMorale() { return metrics.averageMorale(); }
    public double getCustomerValue() { return scores.category("Customer Value").score() / 10.0; }

    public record RunMetadata(String scenarioId, String scenarioName, String seed,
                              String applicationVersion, String configurationVersion,
                              int deadlineWeeks, BigDecimal budget, int finalWeek,
                              TerminationReason terminationReason,
                              Map<String, Integer> initialTeam) {
        public RunMetadata {
            Objects.requireNonNull(scenarioId, "scenarioId");
            Objects.requireNonNull(scenarioName, "scenarioName");
            Objects.requireNonNull(seed, "seed");
            Objects.requireNonNull(applicationVersion, "applicationVersion");
            Objects.requireNonNull(configurationVersion, "configurationVersion");
            Objects.requireNonNull(budget, "budget");
            Objects.requireNonNull(terminationReason, "terminationReason");
            initialTeam = Map.copyOf(initialTeam);
            if (deadlineWeeks < 1 || finalWeek < 1 || budget.signum() < 0) {
                throw new IllegalArgumentException("Run metadata must contain valid project limits");
            }
        }
    }

    public record Outcome(String status, String label, boolean released) {
        public Outcome {
            Objects.requireNonNull(status, "status");
            Objects.requireNonNull(label, "label");
        }
    }

    public record ScoreSummary(double total, String performanceBand,
                               List<CategoryScore> categories) {
        public ScoreSummary {
            total = bounded(total, 0.0, 100.0, "total score");
            Objects.requireNonNull(performanceBand, "performanceBand");
            categories = List.copyOf(categories);
            double sum = categories.stream().mapToDouble(CategoryScore::score).sum();
            if (Math.abs(sum - total) > 0.011) {
                throw new IllegalArgumentException("Total score must equal the category score sum");
            }
        }

        public CategoryScore category(String name) {
            return categories.stream().filter(score -> score.name().equals(name))
                    .findFirst().orElseThrow(() ->
                            new IllegalArgumentException("Unknown score category: " + name));
        }
    }

    public record CategoryScore(String name, double score, double maximum, String explanation) {
        public CategoryScore {
            Objects.requireNonNull(name, "name");
            Objects.requireNonNull(explanation, "explanation");
            maximum = bounded(maximum, 0.0, 100.0, "category maximum");
            score = bounded(score, 0.0, maximum, "category score");
        }
    }

    public record FinalMetrics(BigDecimal totalSpent, double perceivedProgress,
                               double trueProgress, int releasedDefects,
                               double knownRework, double unknownRework,
                               double testingBacklog, double technicalDebt,
                               int finalTeamSize, int departures, double peakFatigue,
                               double averageFatigue, double averageMorale,
                               double scopeExpansion, int acceptedFeatures,
                               int deferredFeatures, int rejectedFeatures,
                               BigDecimal hiringCost) {
        public FinalMetrics {
            Objects.requireNonNull(totalSpent, "totalSpent");
            Objects.requireNonNull(hiringCost, "hiringCost");
            perceivedProgress = bounded(perceivedProgress, 0.0, 1.0, "perceived progress");
            trueProgress = bounded(trueProgress, 0.0, 1.0, "true progress");
            knownRework = nonNegative(knownRework, "known rework");
            unknownRework = nonNegative(unknownRework, "unknown rework");
            testingBacklog = nonNegative(testingBacklog, "testing backlog");
            technicalDebt = bounded(technicalDebt, 0.0, 1.0, "technical debt");
            peakFatigue = bounded(peakFatigue, 0.0, 1.0, "peak fatigue");
            averageFatigue = bounded(averageFatigue, 0.0, 1.0, "average fatigue");
            averageMorale = bounded(averageMorale, 0.0, 1.0, "average morale");
            scopeExpansion = nonNegative(scopeExpansion, "scope expansion");
            if (releasedDefects < 0 || finalTeamSize < 0 || departures < 0
                    || acceptedFeatures < 0 || deferredFeatures < 0 || rejectedFeatures < 0) {
                throw new IllegalArgumentException("Final counts cannot be negative");
            }
        }
    }

    public record DecisionSummary(int total, Map<String, Integer> countsByType,
                                  List<ManagementDecisionRecord> decisions,
                                  List<DecisionReview> reviews) {
        public DecisionSummary {
            if (total < 0) {
                throw new IllegalArgumentException("Decision count cannot be negative");
            }
            countsByType = Map.copyOf(countsByType);
            decisions = List.copyOf(decisions);
            reviews = List.copyOf(reviews);
        }
    }

    public record DecisionReview(ManagementDecisionRecord decision,
                                 String immediateEffect, String laterConsequence,
                                 List<String> evidence) {
        public DecisionReview {
            Objects.requireNonNull(decision, "decision");
            Objects.requireNonNull(immediateEffect, "immediateEffect");
            Objects.requireNonNull(laterConsequence, "laterConsequence");
            evidence = List.copyOf(evidence);
        }
    }

    public record EventSummary(int total, int resolved, Map<String, Integer> countsByType,
                              List<EventDetail> details) {
        public EventSummary {
            if (total < 0 || resolved < 0 || resolved > total) {
                throw new IllegalArgumentException("Event counts must be valid");
            }
            countsByType = Map.copyOf(countsByType);
            details = List.copyOf(details);
        }
    }

    public record EventDetail(String id, String type, String title, int generationWeek,
                              int resolutionWeek, String selectedOption, String result,
                              Map<String, Double> internalEffects,
                              Map<String, Double> featureWorkByPhase,
                              double customerValuePotential) {
        public EventDetail {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(type, "type");
            Objects.requireNonNull(title, "title");
            selectedOption = selectedOption == null ? "" : selectedOption;
            result = result == null ? "" : result;
            internalEffects = Map.copyOf(internalEffects);
            featureWorkByPhase = Map.copyOf(featureWorkByPhase);
            customerValuePotential = bounded(customerValuePotential, 0.0, 1.0,
                    "customer value potential");
        }
    }

    public record TimelineEntry(int week, String type, String title,
                                String description, String relatedId) {
        public TimelineEntry {
            if (week < 0) {
                throw new IllegalArgumentException("Timeline week cannot be negative");
            }
            Objects.requireNonNull(type, "type");
            Objects.requireNonNull(title, "title");
            Objects.requireNonNull(description, "description");
            relatedId = relatedId == null ? "" : relatedId;
        }
    }

    public record Chart(String id, String title, String description,
                        List<ChartSeries> series) {
        public Chart {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(title, "title");
            Objects.requireNonNull(description, "description");
            series = List.copyOf(series);
        }
    }

    public record ChartSeries(String name, String unit, List<ChartPoint> points) {
        public ChartSeries {
            Objects.requireNonNull(name, "name");
            Objects.requireNonNull(unit, "unit");
            points = List.copyOf(points);
        }
    }

    public record ChartPoint(int week, double value) {
        public ChartPoint {
            if (week < 1) {
                throw new IllegalArgumentException("Chart points represent simulated weeks");
            }
            value = nonNegative(value, "chart value");
        }
    }

    public record InstructorWeek(int week, StudentView visibleDuringProject,
                                 HiddenView hiddenStateAtTime) {
        public InstructorWeek {
            if (week < 1) {
                throw new IllegalArgumentException("Instructor history starts at Week 1");
            }
            Objects.requireNonNull(visibleDuringProject, "visibleDuringProject");
            Objects.requireNonNull(hiddenStateAtTime, "hiddenStateAtTime");
        }
    }

    public record StudentView(double perceivedProgress, double schedulePressure,
                              BigDecimal spent, double knownRework, double testingBacklog,
                              int teamSize, String qualityHealth, String fatigueCategory,
                              Map<String, Double> phaseProgress,
                              String testingPriority, String workIntensity,
                              String concurrencyPolicy, String engineeringApproach,
                              String technicalDebtPriority, double scopeExpansion,
                              List<String> activity) {
        public StudentView {
            Objects.requireNonNull(spent, "spent");
            Objects.requireNonNull(qualityHealth, "qualityHealth");
            Objects.requireNonNull(fatigueCategory, "fatigueCategory");
            phaseProgress = Map.copyOf(phaseProgress);
            activity = List.copyOf(activity);
        }
    }

    public record HiddenView(double trueProgress, double averageFatigue, double maximumFatigue,
                             double averageMorale, double unknownRework, double knownRework,
                             Map<String, Double> unknownReworkByPhase,
                             Map<String, Double> knownReworkByPhase,
                             Map<String, Double> trueProgressByPhase,
                             Map<String, Double> perceivedProgressByPhase,
                             Map<String, Double> correctWorkByPhase,
                             Map<String, Double> defectsCreatedByPhase,
                             Map<String, Double> defectsDiscoveredByPhase,
                             double testingBacklog, double qaCapacity,
                             double averageProductivity, double mentoringCoverage,
                             double coordinationOverhead, String turnoverRiskCategory,
                             double technicalDebt, double dependencyUncertainty,
                             double outOfSequenceWork) {
        public HiddenView {
            unknownReworkByPhase = Map.copyOf(unknownReworkByPhase);
            knownReworkByPhase = Map.copyOf(knownReworkByPhase);
            trueProgressByPhase = Map.copyOf(trueProgressByPhase);
            perceivedProgressByPhase = Map.copyOf(perceivedProgressByPhase);
            correctWorkByPhase = Map.copyOf(correctWorkByPhase);
            defectsCreatedByPhase = Map.copyOf(defectsCreatedByPhase);
            defectsDiscoveredByPhase = Map.copyOf(defectsDiscoveredByPhase);
        }
    }

    private static double bounded(double value, double minimum, double maximum, String name) {
        if (!Double.isFinite(value) || value < minimum || value > maximum) {
            throw new IllegalArgumentException(name + " must be finite and within bounds");
        }
        return value;
    }

    private static double nonNegative(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException(name + " must be finite and non-negative");
        }
        return value;
    }
}
