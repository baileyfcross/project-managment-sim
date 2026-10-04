package edu.simulator.report;

import edu.simulator.event.EventDecision;
import edu.simulator.event.ProjectEvent;
import edu.simulator.event.EventType;
import edu.simulator.model.WeeklySnapshot;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class ScoringService {
    public FinalProjectReport.ScoreSummary score(ScoringInput input) {
        Objects.requireNonNull(input, "input");
        double progress = unit(input.trueProgress());
        double totalScope = Math.max(1.0, finiteNonNegative(input.totalScope()));
        double lateWeeks = Math.max(0, input.actualWeek() - input.deadline());

        double schedule;
        String scheduleExplanation;
        if (input.released()) {
            double lateness = lateWeeks / input.deadline();
            schedule = 25.0 * unit(1.0 - 0.55 * lateness - 0.45 * lateness * lateness);
            scheduleExplanation = lateWeeks == 0
                    ? "The project released on or before its Week " + input.deadline() + " deadline."
                    : "The project released " + lateWeeks + " week"
                        + (lateWeeks == 1 ? "" : "s") + " after its deadline.";
        } else {
            schedule = Math.min(15.0, 15.0 * progress);
            scheduleExplanation = "The project ended incomplete at "
                    + Math.round(progress * 100.0) + "% true progress; incomplete work limits this score.";
        }

        double budgetLimit = input.budget().doubleValue();
        double cost = input.actualCost().doubleValue();
        double overrun = budgetLimit <= 0.0
                ? (cost <= 0.0 ? 0.0 : 1.0)
                : Math.max(0.0, cost / budgetLimit - 1.0);
        double budget = 25.0 * unit(1.0 - 0.75 * overrun - 0.25 * overrun * overrun);
        if (!input.released()) {
            budget = Math.min(14.0, budget * progress);
        }
        String budgetExplanation = input.released()
                ? (overrun == 0.0
                    ? "The released project stayed within its approved budget."
                    : "The released project exceeded its budget by "
                        + Math.round(overrun * 100.0) + "%.")
                : "The project did not release; incomplete delivery caps the budget score even when spending was low.";

        double hiddenDefects = Math.max(input.releasedDefects(),
                finiteNonNegative(input.unknownRework()));
        double qualityIndex = 1.0
                - 2.5 * hiddenDefects / totalScope
                - 0.9 * finiteNonNegative(input.knownRework()) / totalScope
                - 0.25 * finiteNonNegative(input.testingBacklog()) / totalScope
                - 0.25 * unit(input.technicalDebt());
        double quality = 25.0 * unit(qualityIndex);
        String qualityExplanation = hiddenDefects > 0.0
                ? Math.round(hiddenDefects) + " hidden defect work units remained at the end; "
                    + Math.round(input.knownRework()) + " known rework units and "
                    + Math.round(input.testingBacklog()) + " testing backlog units also remained."
                : "No hidden defect work remained; final rework, test backlog, and technical debt were included.";

        List<WeeklySnapshot> history = input.history();
        double averageFatigue = average(history, WeeklySnapshot::getAverageFatigue);
        double peakFatigue = history.stream().mapToDouble(WeeklySnapshot::getMaximumFatigue)
                .max().orElse(0.0);
        double averageMorale = average(history, WeeklySnapshot::getAverageMorale);
        long crunchWeeks = history.stream()
                .filter(snapshot -> snapshot.getWorkIntensity().name().equals("CRUNCH")).count();
        long harmfulWeeks = history.stream()
                .filter(snapshot -> snapshot.getAverageFatigue() >= 0.7).count();
        double runWeeks = Math.max(1.0, history.size());
        double overtimeLoad = history.stream().mapToDouble(snapshot ->
                switch (snapshot.getWorkIntensity()) {
                    case SUSTAINABLE -> 0.0;
                    case INCREASED -> 1.0;
                    case CRUNCH -> 1.5;
                }).sum() / runWeeks;
        double burnoutRatio = harmfulWeeks / runWeeks;
        double departureRatio = input.initialTeamSize() <= 0 ? 0.0
                : Math.min(1.0, (double) input.departures() / input.initialTeamSize());
        double sustainabilityIndex = 1.0 - 0.3 * averageFatigue - 0.1 * peakFatigue
                - 0.25 * (1.0 - averageMorale) - 0.2 * overtimeLoad
                - 0.1 * burnoutRatio - 0.05 * departureRatio;
        double sustainability = 15.0 * unit(sustainabilityIndex);
        String sustainabilityExplanation = "Average fatigue was "
                + Math.round(averageFatigue * 100.0) + "%, peak fatigue "
                + Math.round(peakFatigue * 100.0) + "%, with " + crunchWeeks
                + " Crunch weeks and " + input.departures() + " departures.";

        double potential = input.events().stream()
                .filter(event -> event.getType() == EventType.CUSTOMER_FEATURE_REQUEST)
                .mapToDouble(ProjectEvent::getCustomerValuePotential).sum();
        double value = 0.0;
        if (potential > 0.0) {
            for (ProjectEvent event : input.events()) {
                if (event.getType() != EventType.CUSTOMER_FEATURE_REQUEST
                        || event.getSelectedOption() == null) {
                    continue;
                }
                double optionValue = switch (event.getSelectedOption()) {
                    case "ACCEPT" -> event.getCustomerValuePotential() * progress;
                    case "DEFER" -> event.getCustomerValuePotential() * 0.45;
                    case "REJECT" -> event.getCustomerValuePotential() * 0.08;
                    default -> 0.0;
                };
                value += optionValue;
            }
            value = 10.0 * unit(value / potential);
        } else {
            value = 5.0;
        }
        long accepted = input.events().stream().filter(event ->
                event.getType() == EventType.CUSTOMER_FEATURE_REQUEST
                        && "ACCEPT".equals(event.getSelectedOption())).count();
        long deferred = input.events().stream().filter(event ->
                event.getType() == EventType.CUSTOMER_FEATURE_REQUEST
                        && "DEFER".equals(event.getSelectedOption())).count();
        long rejected = input.events().stream().filter(event ->
                event.getType() == EventType.CUSTOMER_FEATURE_REQUEST
                        && "REJECT".equals(event.getSelectedOption())).count();
        String customerExplanation = potential == 0.0
                ? "No customer feature requests occurred; this category is neutral."
                : accepted + " requests accepted, " + deferred + " deferred, and "
                    + rejected + " rejected; accepted value is weighted by delivered project progress.";

        List<FinalProjectReport.CategoryScore> categories = List.of(
                new FinalProjectReport.CategoryScore("Schedule", schedule, 25.0, scheduleExplanation),
                new FinalProjectReport.CategoryScore("Budget", budget, 25.0, budgetExplanation),
                new FinalProjectReport.CategoryScore("Quality", quality, 25.0, qualityExplanation),
                new FinalProjectReport.CategoryScore("Team Sustainability", sustainability, 15.0,
                        sustainabilityExplanation),
                new FinalProjectReport.CategoryScore("Customer Value", value, 10.0,
                        customerExplanation));
        double total = categories.stream().mapToDouble(FinalProjectReport.CategoryScore::score).sum();
        total = Math.round(total * 100.0) / 100.0;
        return new FinalProjectReport.ScoreSummary(total, performanceBand(total), categories);
    }

    private String performanceBand(double score) {
        if (score >= 90.0) return "Excellent";
        if (score >= 80.0) return "Strong";
        if (score >= 70.0) return "Effective";
        if (score >= 60.0) return "Mixed";
        return "Needs Improvement";
    }

    private double average(List<WeeklySnapshot> history,
                           java.util.function.ToDoubleFunction<WeeklySnapshot> measure) {
        return history.stream().mapToDouble(measure).average().orElse(0.0);
    }

    private double unit(double value) {
        return Double.isFinite(value) ? Math.max(0.0, Math.min(1.0, value)) : 0.0;
    }

    private double finiteNonNegative(double value) {
        return Double.isFinite(value) ? Math.max(0.0, value) : 0.0;
    }

    public record ScoringInput(boolean released, int actualWeek, int deadline,
                               BigDecimal actualCost, BigDecimal budget,
                               double trueProgress, double totalScope,
                               int releasedDefects, double unknownRework,
                               double knownRework, double testingBacklog,
                               double technicalDebt, int departures, int initialTeamSize,
                               List<WeeklySnapshot> history, List<ProjectEvent> events,
                               List<EventDecision> eventDecisions) {
        public ScoringInput {
            if (actualWeek < 1 || deadline < 1 || departures < 0 || initialTeamSize < 0) {
                throw new IllegalArgumentException("Scoring input counts and weeks must be valid");
            }
            Objects.requireNonNull(actualCost, "actualCost");
            Objects.requireNonNull(budget, "budget");
            history = List.copyOf(history);
            events = List.copyOf(events);
            eventDecisions = List.copyOf(eventDecisions);
        }
    }
}
