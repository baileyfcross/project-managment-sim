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
        totalWork.put(phase, Math.max(0.0, value));
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
        double next = completedWork.getOrDefault(phase, 0.0) + amount;
        completedWork.put(phase, Math.max(0.0, next));
    }

    public void addKnownRework(ProjectPhase phase, double amount) {
        double next = knownRework.getOrDefault(phase, 0.0) + amount;
        knownRework.put(phase, Math.max(0.0, next));
    }

    public void addUnknownRework(ProjectPhase phase, double amount) {
        double next = unknownRework.getOrDefault(phase, 0.0) + amount;
        unknownRework.put(phase, Math.max(0.0, next));
    }

    public void addCompletedRework(ProjectPhase phase, double amount) {
        double next = completedRework.getOrDefault(phase, 0.0) + amount;
        completedRework.put(phase, Math.max(0.0, next));
    }

    public double totalRemainingWork() {
        double total = 0.0;
        for (ProjectPhase phase : ProjectPhase.values()) {
            total += Math.max(0.0, totalWork.getOrDefault(phase, 0.0) - completedWork.getOrDefault(phase, 0.0));
            total += Math.max(0.0, knownRework.getOrDefault(phase, 0.0));
        }
        return total;
    }

    public double totalCompletedWork() {
        double total = 0.0;
        for (ProjectPhase phase : ProjectPhase.values()) {
            total += completedWork.getOrDefault(phase, 0.0);
            total += completedRework.getOrDefault(phase, 0.0);
        }
        return total;
    }

    public double totalUnknownRework() {
        double total = 0.0;
        for (ProjectPhase phase : ProjectPhase.values()) {
            total += unknownRework.getOrDefault(phase, 0.0);
        }
        return total;
    }

    public double totalKnownRework() {
        double total = 0.0;
        for (ProjectPhase phase : ProjectPhase.values()) {
            total += knownRework.getOrDefault(phase, 0.0);
        }
        return total;
    }
}
