package edu.simulator.ui;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class SimulationStateDto {
    private final int week;
    private final int deadline;
    private final BigDecimal budget;
    private final BigDecimal spent;
    private final BigDecimal remainingBudget;
    private final double burnRate;
    private final double forecastCost;
    private final int estimatedCompletionWeek;
    private final double perceivedProgress;
    private final double trueProgress;
    private final Map<String, Double> phaseProgress;
    private final Map<String, Integer> teamCounts;
    private final String scheduleHealth;
    private final String budgetHealth;
    private final String qualityHealth;
    private final String moraleHealth;
    private final List<String> recentMessages;
    private final double knownRework;
    private final double unknownRework;
    private final double fatigue;
    private final double morale;
    private final double schedulePressure;
    private final double totalRemainingWork;
    private final double totalKnownRework;
    private final int projectDeadline;
    private final BigDecimal projectBudget;
    private final BigDecimal projectSpent;
    private final BigDecimal projectRemainingBudget;
    private final String scenarioName;
    private final String pendingEvent;

    public SimulationStateDto(int week, int deadline, BigDecimal budget, BigDecimal spent,
                             BigDecimal remainingBudget, double burnRate, double forecastCost,
                             int estimatedCompletionWeek, double perceivedProgress, double trueProgress,
                             Map<String, Double> phaseProgress, Map<String, Integer> teamCounts,
                             String scheduleHealth, String budgetHealth, String qualityHealth,
                             String moraleHealth, List<String> recentMessages, double knownRework,
                             double unknownRework, double fatigue, double morale,
                             double schedulePressure, double totalRemainingWork,
                             double totalKnownRework, int projectDeadline, BigDecimal projectBudget,
                             BigDecimal projectSpent, BigDecimal projectRemainingBudget,
                             String scenarioName, String pendingEvent) {
        this.week = week;
        this.deadline = deadline;
        this.budget = budget;
        this.spent = spent;
        this.remainingBudget = remainingBudget;
        this.burnRate = burnRate;
        this.forecastCost = forecastCost;
        this.estimatedCompletionWeek = estimatedCompletionWeek;
        this.perceivedProgress = perceivedProgress;
        this.trueProgress = trueProgress;
        this.phaseProgress = phaseProgress;
        this.teamCounts = teamCounts;
        this.scheduleHealth = scheduleHealth;
        this.budgetHealth = budgetHealth;
        this.qualityHealth = qualityHealth;
        this.moraleHealth = moraleHealth;
        this.recentMessages = recentMessages;
        this.knownRework = knownRework;
        this.unknownRework = unknownRework;
        this.fatigue = fatigue;
        this.morale = morale;
        this.schedulePressure = schedulePressure;
        this.totalRemainingWork = totalRemainingWork;
        this.totalKnownRework = totalKnownRework;
        this.projectDeadline = projectDeadline;
        this.projectBudget = projectBudget;
        this.projectSpent = projectSpent;
        this.projectRemainingBudget = projectRemainingBudget;
        this.scenarioName = scenarioName;
        this.pendingEvent = pendingEvent;
    }

    public int getWeek() {
        return week;
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

    public double getBurnRate() {
        return burnRate;
    }

    public double getForecastCost() {
        return forecastCost;
    }

    public int getEstimatedCompletionWeek() {
        return estimatedCompletionWeek;
    }

    public double getPerceivedProgress() {
        return perceivedProgress;
    }

    public double getTrueProgress() {
        return trueProgress;
    }

    public Map<String, Double> getPhaseProgress() {
        return phaseProgress;
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

    public double getKnownRework() {
        return knownRework;
    }

    public double getUnknownRework() {
        return unknownRework;
    }

    public double getFatigue() {
        return fatigue;
    }

    public double getMorale() {
        return morale;
    }

    public double getSchedulePressure() {
        return schedulePressure;
    }

    public double getTotalRemainingWork() {
        return totalRemainingWork;
    }

    public double getTotalKnownRework() {
        return totalKnownRework;
    }

    public int getProjectDeadline() {
        return projectDeadline;
    }

    public BigDecimal getProjectBudget() {
        return projectBudget;
    }

    public BigDecimal getProjectSpent() {
        return projectSpent;
    }

    public BigDecimal getProjectRemainingBudget() {
        return projectRemainingBudget;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public String getPendingEvent() {
        return pendingEvent;
    }
}
