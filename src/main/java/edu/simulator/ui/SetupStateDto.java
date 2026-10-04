package edu.simulator.ui;

import java.math.BigDecimal;
import java.util.Map;

public class SetupStateDto {
    private final String scenarioId;
    private final String scenarioName;
    private final int deadlineWeeks;
    private final BigDecimal budget;
    private final Map<String, Integer> teamCounts;
    private final BigDecimal weeklyPayroll;
    private final BigDecimal projectedPayroll;
    private final BigDecimal contingency;

    public SetupStateDto(String scenarioId, String scenarioName, int deadlineWeeks,
                         BigDecimal budget, Map<String, Integer> teamCounts,
                         BigDecimal weeklyPayroll, BigDecimal projectedPayroll,
                         BigDecimal contingency) {
        this.scenarioId = scenarioId;
        this.scenarioName = scenarioName;
        this.deadlineWeeks = deadlineWeeks;
        this.budget = budget;
        this.teamCounts = Map.copyOf(teamCounts);
        this.weeklyPayroll = weeklyPayroll;
        this.projectedPayroll = projectedPayroll;
        this.contingency = contingency;
    }

    public String getScenarioId() {
        return scenarioId;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public int getDeadlineWeeks() {
        return deadlineWeeks;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public Map<String, Integer> getTeamCounts() {
        return teamCounts;
    }

    public BigDecimal getWeeklyPayroll() {
        return weeklyPayroll;
    }

    public BigDecimal getProjectedPayroll() {
        return projectedPayroll;
    }

    public BigDecimal getContingency() {
        return contingency;
    }
}
