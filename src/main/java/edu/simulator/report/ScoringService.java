package edu.simulator.report;

import java.math.BigDecimal;

public class ScoringService {
    public double scoreProject(int actualWeek, int deadline, BigDecimal actualBudget, BigDecimal targetBudget,
                              double defectsRate, double fatigue, double morale, double customerValue) {
        double scheduleScore = Math.max(0.0, 25.0 - (Math.max(0, actualWeek - deadline) * 6.0));
        double budgetScore = Math.max(0.0, 25.0 - (actualBudget.subtract(targetBudget).abs().doubleValue() / 5000.0));
        double qualityScore = Math.max(0.0, 25.0 - (defectsRate * 30.0));
        double sustainabilityScore = Math.max(0.0, 15.0 - (fatigue * 18.0) - ((1.0 - morale) * 12.0));
        double customerValueScore = Math.max(0.0, customerValue * 10.0);
        return Math.min(100.0, scheduleScore + budgetScore + qualityScore + sustainabilityScore + customerValueScore);
    }
}
