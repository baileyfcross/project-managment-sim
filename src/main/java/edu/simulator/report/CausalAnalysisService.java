package edu.simulator.report;

import edu.simulator.event.ProjectEvent;
import edu.simulator.model.WeeklySnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CausalAnalysisService {
    public List<CausalFinding> analyze(List<WeeklySnapshot> history,
                                       List<ManagementDecisionRecord> decisions,
                                       List<ProjectEvent> events) {
        List<WeeklySnapshot> weeks = List.copyOf(history);
        List<ManagementDecisionRecord> choices = List.copyOf(decisions);
        List<CausalFinding> findings = new ArrayList<>();
        addPerceptionFinding(weeks, findings);
        addSustainedOvertimeFinding(weeks, choices, findings);
        addTestingFinding(weeks, findings);
        addScopeFinding(weeks, choices, findings);
        addConcurrencyFinding(weeks, findings);
        addCutCornersFinding(weeks, findings);
        addDebtPayDownFinding(weeks, choices, findings);
        addEventDecisionEvidence(events, findings);
        return findings.stream()
                .sorted(Comparator.comparingInt(CausalFinding::importance).reversed()
                        .thenComparingInt(CausalFinding::startingWeek)
                        .thenComparing(CausalFinding::title))
                .limit(6)
                .toList();
    }

    private void addPerceptionFinding(List<WeeklySnapshot> history,
                                     List<CausalFinding> findings) {
        WeeklySnapshot peak = history.stream().max(Comparator.comparingDouble(
                snapshot -> Math.abs(snapshot.getPerceivedProgress() - snapshot.getTrueProgress())))
                .orElse(null);
        if (peak == null) {
            return;
        }
        double divergence = Math.abs(peak.getPerceivedProgress() - peak.getTrueProgress());
        if (divergence < 0.15) {
            return;
        }
        findings.add(new CausalFinding(
                "Perceived progress diverged from completed work",
                "At Week " + peak.getWeek() + ", the progress estimate differed materially from verified work.",
                List.of("Perceived progress was " + percent(peak.getPerceivedProgress())
                        + "; true progress was " + percent(peak.getTrueProgress())
                        + ", a " + percent(divergence) + " gap."),
                peak.getWeek(), peak.getWeek(), 4, List.of()));
    }

    private void addSustainedOvertimeFinding(List<WeeklySnapshot> history,
                                             List<ManagementDecisionRecord> decisions,
                                             List<CausalFinding> findings) {
        int start = -1;
        int end = -1;
        for (int index = 0; index <= history.size(); index++) {
            boolean overtime = index < history.size()
                    && history.get(index).getWorkIntensity().name().equals("CRUNCH");
            if (overtime && start < 0) {
                start = index;
            } else if (!overtime && start >= 0) {
                if (index - start >= 3) {
                    end = index - 1;
                    break;
                }
                start = -1;
            }
        }
        if (start < 0 || end < start) {
            return;
        }
        WeeklySnapshot first = history.get(start);
        WeeklySnapshot last = history.get(end);
        if (last.getAverageFatigue() - first.getAverageFatigue() < 0.08
                && last.getAverageFatigue() < 0.6) {
            return;
        }
        List<String> related = decisions.stream()
                .filter(choice -> choice.decisionType() == DecisionType.WORK_INTENSITY
                        && choice.week() >= first.getWeek() && choice.week() <= last.getWeek())
                .map(ManagementDecisionRecord::relatedId).filter(id -> !id.isBlank()).toList();
        findings.add(new CausalFinding(
                "Sustained crunch coincided with rising fatigue",
                "The sustained high-intensity period was associated with higher team fatigue and reduced recovery time.",
                List.of("Crunch persisted from Week " + first.getWeek() + " through Week "
                        + last.getWeek() + ".", "Average fatigue moved from "
                        + percent(first.getAverageFatigue()) + " to "
                        + percent(last.getAverageFatigue()) + ".",
                        "Average productivity in the recorded snapshots was "
                                + number(first.getAverageProductivity()) + " at the start and "
                                + number(last.getAverageProductivity()) + " at the end."),
                first.getWeek(), last.getWeek(), 5, related));
    }

    private void addTestingFinding(List<WeeklySnapshot> history,
                                   List<CausalFinding> findings) {
        List<WeeklySnapshot> highBacklog = history.stream()
                .filter(snapshot -> snapshot.getTestingBacklog() >= 650.0
                        || snapshot.getTestingBacklogStatus().contains("High")
                        || snapshot.getTestingBacklogStatus().contains("Critical"))
                .toList();
        if (highBacklog.size() < 2) {
            return;
        }
        WeeklySnapshot first = highBacklog.get(0);
        WeeklySnapshot last = highBacklog.get(highBacklog.size() - 1);
        if (last.getUnknownRework() <= first.getUnknownRework()
                && history.stream().noneMatch(snapshot ->
                        snapshot.getDefectsDiscoveredByPhase().values().stream()
                                .mapToDouble(Double::doubleValue).sum() > 0.0)) {
            return;
        }
        double discovered = history.stream().mapToDouble(snapshot ->
                snapshot.getDefectsDiscoveredByPhase().values().stream()
                        .mapToDouble(Double::doubleValue).sum()).sum();
        findings.add(new CausalFinding(
                "Testing backlog left quality signals unresolved",
                "Limited inspection capacity was associated with a persistent test queue and hidden defect work.",
                List.of("The testing backlog was " + number(first.getTestingBacklog())
                        + " in Week " + first.getWeek() + " and "
                        + number(last.getTestingBacklog()) + " in Week " + last.getWeek() + ".",
                        "Unknown rework changed from " + number(first.getUnknownRework())
                                + " to " + number(last.getUnknownRework()) + ".",
                        number(discovered) + " defect work units were discovered during the run."),
                first.getWeek(), last.getWeek(), 4, List.of()));
    }

    private void addScopeFinding(List<WeeklySnapshot> history,
                                 List<ManagementDecisionRecord> decisions,
                                 List<CausalFinding> findings) {
        ManagementDecisionRecord accepted = decisions.stream()
                .filter(choice -> choice.decisionType() == DecisionType.FEATURE_DECISION
                        && choice.newValue().equals("ACCEPTED"))
                .filter(choice -> !history.isEmpty()
                        && choice.week() * 2 >= history.get(history.size() - 1).getWeek())
                .max(Comparator.comparingInt(ManagementDecisionRecord::week)).orElse(null);
        if (accepted == null) {
            return;
        }
        WeeklySnapshot snapshot = snapshotAt(history, accepted.week() + 1);
        if (snapshot == null || snapshot.getPhaseFive().scopeChangeRework() <= 0.0) {
            return;
        }
        findings.add(new CausalFinding(
                "A late accepted feature expanded current work",
                "The feature was accepted after the project was underway, when completed work could be affected by the change.",
                List.of(accepted.description(), "Scope expansion reached "
                        + percent(snapshot.getPhaseFive().scopeExpansion()) + " of the original plan.",
                        "The decision week recorded "
                                + number(snapshot.getPhaseFive().scopeChangeRework())
                                + " units of change-related rework."),
                accepted.week(), accepted.week(), 4,
                accepted.relatedId().isBlank() ? List.of() : List.of(accepted.relatedId())));
    }

    private void addConcurrencyFinding(List<WeeklySnapshot> history,
                                      List<CausalFinding> findings) {
        List<WeeklySnapshot> aggressive = history.stream()
                .filter(snapshot -> snapshot.getPhaseFive().concurrencyPolicy().name().equals("AGGRESSIVE")
                        && (snapshot.getPhaseFive().dependencyUncertainty() > 0.0
                            || snapshot.getPhaseFive().outOfSequenceWork() > 0.0))
                .toList();
        if (aggressive.isEmpty()) {
            return;
        }
        WeeklySnapshot first = aggressive.get(0);
        WeeklySnapshot last = aggressive.get(aggressive.size() - 1);
        findings.add(new CausalFinding(
                "Aggressive concurrency created dependency uncertainty",
                "More downstream work was attempted while upstream phases were still unsettled, increasing dependency risk.",
                List.of(aggressive.size() + " recorded weeks used aggressive concurrency with non-zero uncertainty.",
                        "The first recorded uncertainty was "
                                + percent(first.getPhaseFive().dependencyUncertainty())
                                + " in Week " + first.getWeek() + ".",
                        "Unknown rework was " + number(first.getUnknownRework())
                                + " at Week " + first.getWeek() + " and "
                                + number(last.getUnknownRework()) + " at Week " + last.getWeek() + "."),
                first.getWeek(), last.getWeek(), aggressive.size() >= 2 ? 4 : 3, List.of()));
    }

    private void addCutCornersFinding(List<WeeklySnapshot> history,
                                     List<CausalFinding> findings) {
        List<WeeklySnapshot> shortcuts = history.stream()
                .filter(snapshot -> snapshot.getPhaseFive().engineeringApproach().name()
                        .equals("CUT_CORNERS"))
                .toList();
        if (shortcuts.size() < 2) {
            return;
        }
        WeeklySnapshot first = shortcuts.get(0);
        WeeklySnapshot last = shortcuts.get(shortcuts.size() - 1);
        if (last.getPhaseFive().technicalDebt() <= first.getPhaseFive().technicalDebt()) {
            return;
        }
        findings.add(new CausalFinding(
                "Cut-corner periods accumulated technical debt",
                "Shortcut-oriented engineering was associated with increased debt that can make later work and repairs harder.",
                List.of("Technical debt moved from "
                        + percent(first.getPhaseFive().technicalDebt()) + " in Week "
                        + first.getWeek() + " to "
                        + percent(last.getPhaseFive().technicalDebt()) + " in Week "
                        + last.getWeek() + ".",
                        "Unknown rework at the end of this period was "
                                + number(last.getUnknownRework()) + "."),
                first.getWeek(), last.getWeek(), 4, List.of()));
    }

    private void addDebtPayDownFinding(List<WeeklySnapshot> history,
                                       List<ManagementDecisionRecord> decisions,
                                       List<CausalFinding> findings) {
        ManagementDecisionRecord payDown = decisions.stream()
                .filter(choice -> choice.decisionType() == DecisionType.TECHNICAL_DEBT_PRIORITY
                        && choice.newValue().equals("PAY_DOWN"))
                .filter(choice -> choice.week() < history.size())
                .findFirst().orElse(null);
        if (payDown == null) {
            return;
        }
        WeeklySnapshot before = snapshotAt(history, payDown.week());
        WeeklySnapshot after = snapshotAt(history, payDown.week() + 1);
        if (before == null || after == null
                || after.getPhaseFive().technicalDebt() >= before.getPhaseFive().technicalDebt()) {
            return;
        }
        findings.add(new CausalFinding(
                "Debt pay-down reduced later technical debt",
                "Capacity reserved for debt work was followed by a lower debt level in the next recorded week.",
                List.of(payDown.description(), "Technical debt changed from "
                        + percent(before.getPhaseFive().technicalDebt()) + " to "
                        + percent(after.getPhaseFive().technicalDebt()) + "."),
                payDown.week(), payDown.week() + 1, 3,
                payDown.relatedId().isBlank() ? List.of() : List.of(payDown.relatedId())));
    }

    private void addEventDecisionEvidence(List<ProjectEvent> events,
                                         List<CausalFinding> findings) {
        long deferred = events.stream().filter(event -> "DEFER".equals(event.getSelectedOption())).count();
        long rejected = events.stream().filter(event -> "REJECT".equals(event.getSelectedOption())).count();
        if (deferred + rejected > 0 && findings.size() < 6) {
            int lastWeek = events.stream().mapToInt(ProjectEvent::getWeek).max().orElse(0);
            findings.add(new CausalFinding(
                    "Customer requests were screened rather than accepted automatically",
                    "Deferrals preserve an option for a later release, while rejections protect current scope and give up that opportunity.",
                    List.of(deferred + " feature requests were deferred and "
                            + rejected + " were rejected."),
                    events.stream().filter(event -> "DEFER".equals(event.getSelectedOption())
                                    || "REJECT".equals(event.getSelectedOption()))
                            .mapToInt(ProjectEvent::getWeek).min().orElse(lastWeek),
                    lastWeek, 2, List.of()));
        }
    }

    private WeeklySnapshot snapshotAt(List<WeeklySnapshot> history, int week) {
        return history.stream().filter(snapshot -> snapshot.getWeek() == week)
                .findFirst().orElse(null);
    }

    private String percent(double value) {
        return Math.round(value * 100.0) + "%";
    }

    private String number(double value) {
        return String.format(java.util.Locale.ROOT, "%.1f", value);
    }
}
