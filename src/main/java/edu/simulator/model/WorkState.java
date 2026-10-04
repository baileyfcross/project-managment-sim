package edu.simulator.model;

import java.util.EnumMap;
import java.util.Map;

public class WorkState {
    private final Map<ProjectPhase, Double> totalWork = new EnumMap<>(ProjectPhase.class);
    private final Map<ProjectPhase, Double> completedWork = new EnumMap<>(ProjectPhase.class);
    private final Map<ProjectPhase, Double> knownRework = new EnumMap<>(ProjectPhase.class);
    private final Map<ProjectPhase, Double> unknownRework = new EnumMap<>(ProjectPhase.class);
    private final Map<ProjectPhase, Double> completedRework = new EnumMap<>(ProjectPhase.class);

    public WorkState() {
        for (ProjectPhase phase : ProjectPhase.values()) {
            totalWork.put(phase, 0.0);
            completedWork.put(phase, 0.0);
            knownRework.put(phase, 0.0);
            unknownRework.put(phase, 0.0);
            completedRework.put(phase, 0.0);
        }
    }

    public void setTotalWork(ProjectPhase phase, double value) {
        totalWork.put(phase, SimulationValues.nonNegativeFinite(value));
    }

    public double getTotalWork(ProjectPhase phase) {
        return totalWork.getOrDefault(phase, 0.0);
    }

    public double getCompletedWork(ProjectPhase phase) {
        return completedWork.getOrDefault(phase, 0.0);
    }

    public double getKnownRework(ProjectPhase phase) {
        return knownRework.getOrDefault(phase, 0.0);
    }

    public double getUnknownRework(ProjectPhase phase) {
        return unknownRework.getOrDefault(phase, 0.0);
    }

    public double getCompletedRework(ProjectPhase phase) {
        return completedRework.getOrDefault(phase, 0.0);
    }

    public void addCompletedWork(ProjectPhase phase, double amount) {
        adjust(completedWork, phase, amount);
    }

    public void addKnownRework(ProjectPhase phase, double amount) {
        adjust(knownRework, phase, amount);
    }

    public void addUnknownRework(ProjectPhase phase, double amount) {
        adjust(unknownRework, phase, amount);
    }

    public void addCompletedRework(ProjectPhase phase, double amount) {
        adjust(completedRework, phase, amount);
    }

    public double totalRemainingWork() {
        double total = 0.0;
        for (ProjectPhase phase : ProjectPhase.values()) {
            total = safeAdd(total, Math.max(0.0,
                    totalWork.getOrDefault(phase, 0.0) - completedWork.getOrDefault(phase, 0.0)));
            total = safeAdd(total, knownRework.getOrDefault(phase, 0.0));
        }
        return total;
    }

    public double perceivedRemainingWork() {
        double total = 0.0;
        for (ProjectPhase phase : ProjectPhase.values()) {
            total = safeAdd(total, Math.max(0.0, totalWork.getOrDefault(phase, 0.0)
                    - completedWork.getOrDefault(phase, 0.0)
                    - unknownRework.getOrDefault(phase, 0.0)));
            total = safeAdd(total, knownRework.getOrDefault(phase, 0.0));
        }
        return total;
    }

    public boolean isReleaseReady() {
        for (ProjectPhase phase : ProjectPhase.values()) {
            double perceivedCompleted = completedWork.getOrDefault(phase, 0.0)
                    + unknownRework.getOrDefault(phase, 0.0);
            if (perceivedCompleted < totalWork.getOrDefault(phase, 0.0)
                    || knownRework.getOrDefault(phase, 0.0) > 0.0) {
                return false;
            }
        }
        return true;
    }

    public double totalCompletedWork() {
        double total = 0.0;
        for (ProjectPhase phase : ProjectPhase.values()) {
            total = safeAdd(total, completedWork.getOrDefault(phase, 0.0));
            total = safeAdd(total, completedRework.getOrDefault(phase, 0.0));
        }
        return total;
    }

    public double totalUnknownRework() {
        double total = 0.0;
        for (ProjectPhase phase : ProjectPhase.values()) {
            total = safeAdd(total, unknownRework.getOrDefault(phase, 0.0));
        }
        return total;
    }

    public double totalKnownRework() {
        double total = 0.0;
        for (ProjectPhase phase : ProjectPhase.values()) {
            total = safeAdd(total, knownRework.getOrDefault(phase, 0.0));
        }
        return total;
    }

    private void adjust(Map<ProjectPhase, Double> stock, ProjectPhase phase, double amount) {
        if (!Double.isFinite(amount)) {
            return;
        }
        double current = stock.getOrDefault(phase, 0.0);
        double next = amount > 0.0 && current > Double.MAX_VALUE - amount
                ? Double.MAX_VALUE : current + amount;
        stock.put(phase, Math.max(0.0, next));
    }

    private double safeAdd(double left, double right) {
        if (right > 0.0 && left > Double.MAX_VALUE - right) {
            return Double.MAX_VALUE;
        }
        return left + right;
    }
}
