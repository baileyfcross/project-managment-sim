package edu.simulator.model;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Internal end-of-week record; hidden fields are not part of gameplay DTOs. */
public class WeeklySnapshot {
    private final int week;
    private final BigDecimal spent;
    private final BigDecimal remainingBudget;
    private final BigDecimal weeklyCost;
    private final double perceivedProgress;
    private final double trueProgress;
    private final int teamSize;
    private final Map<Role, Integer> teamCounts;
    private final double averageProductivity;
    private final double averageFatigue;
    private final double knownRework;
    private final double unknownRework;
    private final int estimatedCompletionWeek;
    private final double schedulePressure;
    private final List<String> recentMessages;

    public WeeklySnapshot(int week, BigDecimal spent, BigDecimal remainingBudget,
                          BigDecimal weeklyCost, double perceivedProgress,
                          double trueProgress, int teamSize, Map<Role, Integer> teamCounts,
                          double averageProductivity, double averageFatigue,
                          double knownRework, double unknownRework,
                          int estimatedCompletionWeek, double schedulePressure,
                          List<String> recentMessages) {
        if (week < 1) {
            throw new IllegalArgumentException("A weekly snapshot must represent Week 1 or later");
        }
        this.week = week;
        this.spent = spent;
        this.remainingBudget = remainingBudget;
        this.weeklyCost = weeklyCost;
        this.perceivedProgress = SimulationValues.unitInterval(perceivedProgress);
        this.trueProgress = SimulationValues.unitInterval(trueProgress);
        this.teamSize = Math.max(0, teamSize);
        this.teamCounts = Map.copyOf(new EnumMap<>(teamCounts));
        this.averageProductivity = SimulationValues.nonNegativeFinite(averageProductivity);
        this.averageFatigue = SimulationValues.unitInterval(averageFatigue);
        this.knownRework = SimulationValues.nonNegativeFinite(knownRework);
        this.unknownRework = SimulationValues.nonNegativeFinite(unknownRework);
        this.estimatedCompletionWeek = estimatedCompletionWeek;
        this.schedulePressure = SimulationValues.unitInterval(schedulePressure);
        this.recentMessages = List.copyOf(recentMessages);
    }

    public int getWeek() {
        return week;
    }

    public BigDecimal getSpent() {
        return spent;
    }

    public BigDecimal getRemainingBudget() {
        return remainingBudget;
    }

    public BigDecimal getWeeklyCost() {
        return weeklyCost;
    }

    public double getPerceivedProgress() {
        return perceivedProgress;
    }

    public double getTrueProgress() {
        return trueProgress;
    }

    public int getTeamSize() {
        return teamSize;
    }

    public Map<Role, Integer> getTeamCounts() {
        return teamCounts;
    }

    public double getAverageProductivity() {
        return averageProductivity;
    }

    public double getAverageFatigue() {
        return averageFatigue;
    }

    public double getKnownRework() {
        return knownRework;
    }

    public double getUnknownRework() {
        return unknownRework;
    }

    public int getEstimatedCompletionWeek() {
        return estimatedCompletionWeek;
    }

    public double getSchedulePressure() {
        return schedulePressure;
    }

    public List<String> getRecentMessages() {
        return recentMessages;
    }
}
