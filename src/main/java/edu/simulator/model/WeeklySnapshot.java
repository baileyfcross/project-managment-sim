package edu.simulator.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class WeeklySnapshot {
    private final int week;
    private final BigDecimal spent;
    private final BigDecimal remainingBudget;
    private final double burnRate;
    private final double perceivedProgress;
    private final double trueProgress;
    private final double schedulePressure;
    private final double fatigue;
    private final int teamSize;
    private final List<String> recentMessages;

    public WeeklySnapshot(int week, BigDecimal spent, BigDecimal remainingBudget, double burnRate,
                         double perceivedProgress, double trueProgress, double schedulePressure,
                         double fatigue, int teamSize, List<String> recentMessages) {
        this.week = week;
        this.spent = spent;
        this.remainingBudget = remainingBudget;
        this.burnRate = burnRate;
        this.perceivedProgress = perceivedProgress;
        this.trueProgress = trueProgress;
        this.schedulePressure = schedulePressure;
        this.fatigue = fatigue;
        this.teamSize = teamSize;
        this.recentMessages = new ArrayList<>(recentMessages == null ? List.of() : recentMessages);
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

    public double getBurnRate() {
        return burnRate;
    }

    public double getPerceivedProgress() {
        return perceivedProgress;
    }

    public double getTrueProgress() {
        return trueProgress;
    }

    public double getSchedulePressure() {
        return schedulePressure;
    }

    public double getFatigue() {
        return fatigue;
    }

    public int getTeamSize() {
        return teamSize;
    }

    public List<String> getRecentMessages() {
        return new ArrayList<>(recentMessages);
    }
}
