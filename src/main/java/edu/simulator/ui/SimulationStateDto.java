package edu.simulator.ui;

import edu.simulator.model.WorkIntensity;
import edu.simulator.model.TestingPriority;
import edu.simulator.model.ConcurrencyPolicy;
import edu.simulator.model.EngineeringApproach;
import edu.simulator.model.TechnicalDebtPriority;

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
    private final Map<String, Double> phaseProgress;
    private final double knownRework;
    private final double defectsDiscoveredThisWeek;
    private final String testingBacklogStatus;
    private final TestingPriority testingPriority;
    private final double qaCapacity;
    private final int workIntensityHours;
    private final String averageFatigueHealth;
    private final String turnoverRisk;
    private final int employeesDepartedThisWeek;
    private final BigDecimal overtimeCost;
    private final double scopeExpansion;
    private final int acceptedFeatureCount;
    private final int deferredFeatureCount;
    private final int rejectedFeatureCount;
    private final String technicalDebtHealth;
    private final ConcurrencyPolicy concurrencyPolicy;
    private final EngineeringApproach engineeringApproach;
    private final TechnicalDebtPriority technicalDebtPriority;
    private final List<ProjectEventDto> pendingEvents;
    private final List<ProjectEventDto> eventHistory;

    public SimulationStateDto(int week, long seed, int deadline, BigDecimal budget,
                              BigDecimal spent, BigDecimal remainingBudget,
                              BigDecimal burnRate, BigDecimal forecastCost,
                              int estimatedCompletionWeek, double perceivedProgress,
                              Map<String, Integer> teamCounts, String scheduleHealth,
                              String budgetHealth, String qualityHealth, String moraleHealth,
                              List<String> recentMessages, WorkIntensity workIntensity,
                              boolean complete, Map<String, Double> phaseProgress,
                              double knownRework, double defectsDiscoveredThisWeek,
                              String testingBacklogStatus, TestingPriority testingPriority,
                              double qaCapacity, int workIntensityHours,
                              String averageFatigueHealth, String turnoverRisk,
                              int employeesDepartedThisWeek, BigDecimal overtimeCost,
                              double scopeExpansion, int acceptedFeatureCount,
                              int deferredFeatureCount, int rejectedFeatureCount,
                              String technicalDebtHealth, ConcurrencyPolicy concurrencyPolicy,
                              EngineeringApproach engineeringApproach,
                              TechnicalDebtPriority technicalDebtPriority,
                              List<ProjectEventDto> pendingEvents,
                              List<ProjectEventDto> eventHistory) {
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
        this.phaseProgress = Map.copyOf(phaseProgress);
        this.knownRework = knownRework;
        this.defectsDiscoveredThisWeek = defectsDiscoveredThisWeek;
        this.testingBacklogStatus = testingBacklogStatus;
        this.testingPriority = testingPriority;
        this.qaCapacity = qaCapacity;
        this.workIntensityHours = workIntensityHours;
        this.averageFatigueHealth = averageFatigueHealth;
        this.turnoverRisk = turnoverRisk;
        this.employeesDepartedThisWeek = employeesDepartedThisWeek;
        this.overtimeCost = overtimeCost;
        this.scopeExpansion = Math.max(0.0, Double.isFinite(scopeExpansion) ? scopeExpansion : 0.0);
        this.acceptedFeatureCount = Math.max(0, acceptedFeatureCount);
        this.deferredFeatureCount = Math.max(0, deferredFeatureCount);
        this.rejectedFeatureCount = Math.max(0, rejectedFeatureCount);
        this.technicalDebtHealth = technicalDebtHealth;
        this.concurrencyPolicy = concurrencyPolicy;
        this.engineeringApproach = engineeringApproach;
        this.technicalDebtPriority = technicalDebtPriority;
        this.pendingEvents = List.copyOf(pendingEvents);
        this.eventHistory = List.copyOf(eventHistory);
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

    public Map<String, Double> getPhaseProgress() { return phaseProgress; }
    public double getKnownRework() { return knownRework; }
    public double getDefectsDiscoveredThisWeek() { return defectsDiscoveredThisWeek; }
    public String getTestingBacklogStatus() { return testingBacklogStatus; }
    public TestingPriority getTestingPriority() { return testingPriority; }
    public double getQaCapacity() { return qaCapacity; }
    public int getWorkIntensityHours() { return workIntensityHours; }
    public String getAverageFatigueHealth() { return averageFatigueHealth; }
    public String getTurnoverRisk() { return turnoverRisk; }
    public int getEmployeesDepartedThisWeek() { return employeesDepartedThisWeek; }
    public BigDecimal getOvertimeCost() { return overtimeCost; }
    public double getScopeExpansion() { return scopeExpansion; }
    public int getAcceptedFeatureCount() { return acceptedFeatureCount; }
    public int getDeferredFeatureCount() { return deferredFeatureCount; }
    public int getRejectedFeatureCount() { return rejectedFeatureCount; }
    public String getTechnicalDebtHealth() { return technicalDebtHealth; }
    public ConcurrencyPolicy getConcurrencyPolicy() { return concurrencyPolicy; }
    public EngineeringApproach getEngineeringApproach() { return engineeringApproach; }
    public TechnicalDebtPriority getTechnicalDebtPriority() { return technicalDebtPriority; }
    public List<ProjectEventDto> getPendingEvents() { return pendingEvents; }
    public List<ProjectEventDto> getEventHistory() { return eventHistory; }
}
