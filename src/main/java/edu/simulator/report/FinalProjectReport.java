package edu.simulator.report;

import java.math.BigDecimal;

public class FinalProjectReport {
    private final int actualWeek;
    private final int deadline;
    private final BigDecimal actualBudget;
    private final BigDecimal targetBudget;
    private final int defectsReleased;
    private final int teamTurnover;
    private final double peakFatigue;
    private final double averageMorale;
    private final double customerValue;

    public FinalProjectReport(int actualWeek, int deadline, BigDecimal actualBudget,
                             BigDecimal targetBudget, int defectsReleased,
                             int teamTurnover, double peakFatigue,
                             double averageMorale, double customerValue) {
        this.actualWeek = actualWeek;
        this.deadline = deadline;
        this.actualBudget = actualBudget;
        this.targetBudget = targetBudget;
        this.defectsReleased = defectsReleased;
        this.teamTurnover = teamTurnover;
        this.peakFatigue = peakFatigue;
        this.averageMorale = averageMorale;
        this.customerValue = customerValue;
    }

    public int getActualWeek() {
        return actualWeek;
    }

    public int getDeadline() {
        return deadline;
    }

    public BigDecimal getActualBudget() {
        return actualBudget;
    }

    public BigDecimal getTargetBudget() {
        return targetBudget;
    }

    public int getDefectsReleased() {
        return defectsReleased;
    }

    public int getTeamTurnover() {
        return teamTurnover;
    }

    public double getPeakFatigue() {
        return peakFatigue;
    }

    public double getAverageMorale() {
        return averageMorale;
    }

    public double getCustomerValue() {
        return customerValue;
    }
}
