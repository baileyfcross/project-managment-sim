package edu.simulator.model;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

public class Project {
    private final String id;
    private final String name;
    private final int deadlineWeeks;
    private final BigDecimal budget;
    private BigDecimal spent;
    private int currentWeek;
    private final WorkState workState;
    private final Map<ProjectPhase, Double> phaseProgress = new EnumMap<>(ProjectPhase.class);
    private final Map<ProjectPhase, Double> hiddenProgress = new EnumMap<>(ProjectPhase.class);
    private double technicalDebt;
    private double morale;
    private final double[] schedulePressureHistory = new double[4];

    public Project(String id, String name, int deadlineWeeks, BigDecimal budget) {
        this.id = id;
        this.name = name;
        this.deadlineWeeks = deadlineWeeks;
        this.budget = budget;
        this.spent = BigDecimal.ZERO;
        this.currentWeek = 0;
        this.workState = new WorkState();
        for (ProjectPhase phase : ProjectPhase.values()) {
            phaseProgress.put(phase, 0.0);
            hiddenProgress.put(phase, 0.0);
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getDeadlineWeeks() {
        return deadlineWeeks;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public BigDecimal getSpent() {
        return spent;
    }

    public void setSpent(BigDecimal spent) {
        this.spent = spent;
    }

    public void addSpent(BigDecimal amount) {
        this.spent = this.spent.add(amount == null ? BigDecimal.ZERO : amount);
    }

    public BigDecimal getRemainingBudget() {
        return budget.subtract(spent).max(BigDecimal.ZERO);
    }

    public int getCurrentWeek() {
        return currentWeek;
    }

    public void setCurrentWeek(int week) {
        this.currentWeek = Math.max(0, week);
    }

    public void incrementWeek() {
        currentWeek++;
    }

    public WorkState getWorkState() {
        return workState;
    }

    public double getPhaseProgress(ProjectPhase phase) {
        return phaseProgress.getOrDefault(phase, 0.0);
    }

    public void setPhaseProgress(ProjectPhase phase, double value) {
        phaseProgress.put(phase, clamp(value));
    }

    public double getHiddenProgress(ProjectPhase phase) {
        return hiddenProgress.getOrDefault(phase, 0.0);
    }

    public void setHiddenProgress(ProjectPhase phase, double value) {
        hiddenProgress.put(phase, clamp(value));
    }

    public double getTechnicalDebt() {
        return technicalDebt;
    }

    public void setTechnicalDebt(double technicalDebt) {
        this.technicalDebt = clamp(technicalDebt);
    }

    public double getMorale() {
        return morale;
    }

    public void setMorale(double morale) {
        this.morale = clamp(morale);
    }

    public boolean isComplete() {
        for (ProjectPhase phase : ProjectPhase.values()) {
            if (workState.getTotalWork(phase) > 0.0 && workState.getCompletedWork(phase) < workState.getTotalWork(phase)) {
                return false;
            }
            if (workState.getKnownRework(phase) > 0.0) {
                return false;
            }
        }
        return true;
    }

    public double calculatePerceivedProgress() {
        double total = 0.0;
        double complete = 0.0;
        for (ProjectPhase phase : ProjectPhase.values()) {
            double phaseTotal = workState.getTotalWork(phase);
            if (phaseTotal <= 0.0) {
                continue;
            }
            double ratio = (workState.getCompletedWork(phase) + workState.getKnownRework(phase)) / phaseTotal;
            complete += Math.min(1.0, ratio) * phaseTotal;
            total += phaseTotal;
        }
        if (total <= 0.0) {
            return 0.0;
        }
        return complete / total;
    }

    public double calculateTrueProgress() {
        double total = 0.0;
        double complete = 0.0;
        for (ProjectPhase phase : ProjectPhase.values()) {
            double phaseTotal = workState.getTotalWork(phase);
            if (phaseTotal <= 0.0) {
                continue;
            }
            complete += Math.min(1.0, workState.getCompletedWork(phase) / phaseTotal) * phaseTotal;
            total += phaseTotal;
        }
        if (total <= 0.0) {
            return 0.0;
        }
        return complete / total;
    }

    public void recordSchedulePressure(double pressure) {
        for (int index = schedulePressureHistory.length - 1; index > 0; index--) {
            schedulePressureHistory[index] = schedulePressureHistory[index - 1];
        }
        schedulePressureHistory[0] = pressure;
    }

    public double getAverageSchedulePressure() {
        double total = 0.0;
        for (double value : schedulePressureHistory) {
            total += value;
        }
        return total / schedulePressureHistory.length;
    }

    private double clamp(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, value));
    }
}
