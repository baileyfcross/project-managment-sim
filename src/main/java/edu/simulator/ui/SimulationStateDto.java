package edu.simulator.ui;

import edu.simulator.simulation.WorkIntensity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Gameplay-only state. Hidden simulation stocks and true progress deliberately
 * remain available only to internal snapshots and post-simulation reporting.
 */
public class SimulationStateDto {
    private final int week;
    private final String seed;
    private final int deadline;
    private final BigDecimal budget;
    private final BigDecimal spent;
    private final BigDecimal remainingBudget;
    private final BigDecimal burnRate;
    private final BigDecimal forecastCost;
    private final int estimatedCompletionWeek;
    private final double perceivedProgress;
    private final Map<String, Integer> teamCounts;
    private final String scheduleHealth;
    private final String budgetHealth;
    private final String qualityHealth;
    private final String moraleHealth;
    private final List<String> recentMessages;
    private final WorkIntensity workIntensity;
    private final boolean complete;

    public SimulationStateDto(int week, long seed, int deadline, BigDecimal budget,
                              BigDecimal spent, BigDecimal remainingBudget,
                              BigDecimal burnRate, BigDecimal forecastCost,
                              int estimatedCompletionWeek, double perceivedProgress,
                              Map<String, Integer> teamCounts, String scheduleHealth,
                              String budgetHealth, String qualityHealth, String moraleHealth,
                              List<String> recentMessages, WorkIntensity workIntensity,
                              boolean complete) {
        this.week = week;
        this.seed = Long.toString(seed);
        this.deadline = deadline;
        this.budget = budget;
        this.spent = spent;
        this.remainingBudget = remainingBudget;
        this.burnRate = burnRate;
        this.forecastCost = forecastCost;
        this.estimatedCompletionWeek = estimatedCompletionWeek;
        this.perceivedProgress = perceivedProgress;
        this.teamCounts = Map.copyOf(teamCounts);
        this.scheduleHealth = scheduleHealth;
        this.budgetHealth = budgetHealth;
        this.qualityHealth = qualityHealth;
        this.moraleHealth = moraleHealth;
        this.recentMessages = List.copyOf(recentMessages);
        this.workIntensity = workIntensity;
        this.complete = complete;
    }

    public int getWeek() {
        return week;
    }

    public String getSeed() {
        return seed;
    }

    public int getDeadline() {
        return deadline;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public BigDecimal getSpent() {
        return spent;
    }

    public BigDecimal getRemainingBudget() {
        return remainingBudget;
    }

    public BigDecimal getBurnRate() {
        return burnRate;
    }

    public BigDecimal getForecastCost() {
        return forecastCost;
    }

    public int getEstimatedCompletionWeek() {
        return estimatedCompletionWeek;
    }

    public double getPerceivedProgress() {
        return perceivedProgress;
    }

    public Map<String, Integer> getTeamCounts() {
        return teamCounts;
    }

    public String getScheduleHealth() {
        return scheduleHealth;
    }

    public String getBudgetHealth() {
        return budgetHealth;
    }

    public String getQualityHealth() {
        return qualityHealth;
    }

    public String getMoraleHealth() {
        return moraleHealth;
    }

    public List<String> getRecentMessages() {
        return recentMessages;
    }

    public WorkIntensity getWorkIntensity() {
        return workIntensity;
    }

    public boolean isComplete() {
        return complete;
    }
}
