package edu.simulator.model;

import java.util.EnumMap;
import java.util.Map;

public class WorkState {
    private final Map<ProjectPhase, Double> initialBaseWork = new EnumMap<>(ProjectPhase.class);
    private final Map<ProjectPhase, Double> baseWorkRemaining = new EnumMap<>(ProjectPhase.class);
    private final Map<ProjectPhase, Double> correctWorkCompleted = new EnumMap<>(ProjectPhase.class);
    private final Map<ProjectPhase, Double> knownRework = new EnumMap<>(ProjectPhase.class);
    private final Map<ProjectPhase, Double> unknownRework = new EnumMap<>(ProjectPhase.class);
    private final Map<ProjectPhase, Double> reworkCompleted = new EnumMap<>(ProjectPhase.class);
    private final Map<ProjectPhase, Double> workAttemptedThisWeek = new EnumMap<>(ProjectPhase.class);
    private final Map<ProjectPhase, Double> defectsCreatedThisWeek = new EnumMap<>(ProjectPhase.class);
    private final Map<ProjectPhase, Double> defectsDiscoveredThisWeek = new EnumMap<>(ProjectPhase.class);
    private final Map<ProjectPhase, Double> reworkCompletedThisWeek = new EnumMap<>(ProjectPhase.class);
    private final Map<ProjectPhase, Double> testingBacklog = new EnumMap<>(ProjectPhase.class);

    public WorkState() {
        for (ProjectPhase phase : ProjectPhase.values()) {
            initialBaseWork.put(phase, 0.0);
            baseWorkRemaining.put(phase, 0.0);
            correctWorkCompleted.put(phase, 0.0);
            knownRework.put(phase, 0.0);
            unknownRework.put(phase, 0.0);
            reworkCompleted.put(phase, 0.0);
            workAttemptedThisWeek.put(phase, 0.0);
            defectsCreatedThisWeek.put(phase, 0.0);
            defectsDiscoveredThisWeek.put(phase, 0.0);
            reworkCompletedThisWeek.put(phase, 0.0);
            testingBacklog.put(phase, 0.0);
        }
    }

    public void setTotalWork(ProjectPhase phase, double value) {
        double work = SimulationValues.nonNegativeFinite(value);
        initialBaseWork.put(phase, work);
        baseWorkRemaining.put(phase, work);
    }

    public void addScope(ProjectPhase phase, double value) {
        double work = SimulationValues.nonNegativeFinite(value);
        if (work == 0.0) {
            return;
        }
        initialBaseWork.put(phase, safeAdd(getTotalWork(phase), work));
        baseWorkRemaining.put(phase, safeAdd(getBaseWorkRemaining(phase), work));
    }

    public double getTotalWork(ProjectPhase phase) {
        return initialBaseWork.getOrDefault(phase, 0.0);
    }

    public Map<ProjectPhase, Double> phaseTotalScope() {
        return Map.copyOf(initialBaseWork);
    }

    public double getInitialBaseWork(ProjectPhase phase) {
        return getTotalWork(phase);
    }

    public double getBaseWorkRemaining(ProjectPhase phase) {
        return baseWorkRemaining.getOrDefault(phase, 0.0);
    }

    public double getCorrectWorkCompleted(ProjectPhase phase) {
        return correctWorkCompleted.getOrDefault(phase, 0.0);
    }

    public double getCompletedWork(ProjectPhase phase) {
        return getCorrectWorkCompleted(phase);
    }

    public double getKnownRework(ProjectPhase phase) {
        return knownRework.getOrDefault(phase, 0.0);
    }

    public double getUnknownRework(ProjectPhase phase) {
        return unknownRework.getOrDefault(phase, 0.0);
    }

    public double getCompletedRework(ProjectPhase phase) {
        return reworkCompleted.getOrDefault(phase, 0.0);
    }

    public double getWorkAttemptedThisWeek(ProjectPhase phase) {
        return workAttemptedThisWeek.getOrDefault(phase, 0.0);
    }

    public double getDefectsCreatedThisWeek(ProjectPhase phase) {
        return defectsCreatedThisWeek.getOrDefault(phase, 0.0);
    }

    public double getDefectsDiscoveredThisWeek(ProjectPhase phase) {
        return defectsDiscoveredThisWeek.getOrDefault(phase, 0.0);
    }

    public double getReworkCompletedThisWeek(ProjectPhase phase) {
        return reworkCompletedThisWeek.getOrDefault(phase, 0.0);
    }

    public void beginWeek() {
        for (ProjectPhase phase : ProjectPhase.values()) {
            workAttemptedThisWeek.put(phase, 0.0);
            defectsCreatedThisWeek.put(phase, 0.0);
            defectsDiscoveredThisWeek.put(phase, 0.0);
            reworkCompletedThisWeek.put(phase, 0.0);
        }
    }

    public double recordNewWork(ProjectPhase phase, double attempted, double defective) {
        double available = getBaseWorkRemaining(phase);
        double amount = Math.min(available, SimulationValues.nonNegativeFinite(attempted));
        double bad = Math.min(amount, SimulationValues.nonNegativeFinite(defective));
        double correct = amount - bad;
        baseWorkRemaining.put(phase, Math.max(0.0, available - amount));
        correctWorkCompleted.put(phase, safeAdd(getCorrectWorkCompleted(phase), correct));
        unknownRework.put(phase, safeAdd(getUnknownRework(phase), bad));
        workAttemptedThisWeek.put(phase, safeAdd(getWorkAttemptedThisWeek(phase), amount));
        defectsCreatedThisWeek.put(phase, safeAdd(getDefectsCreatedThisWeek(phase), bad));
        return amount;
    }

    public void recordRework(ProjectPhase phase, double attempted, double defective) {
        double available = getKnownRework(phase);
        double amount = Math.min(available, SimulationValues.nonNegativeFinite(attempted));
        double bad = Math.min(amount, SimulationValues.nonNegativeFinite(defective));
        double fixed = amount - bad;
        knownRework.put(phase, Math.max(0.0, available - amount));
        correctWorkCompleted.put(phase, safeAdd(getCorrectWorkCompleted(phase), fixed));
        unknownRework.put(phase, safeAdd(getUnknownRework(phase), bad));
        workAttemptedThisWeek.put(phase, safeAdd(getWorkAttemptedThisWeek(phase), amount));
        reworkCompleted.put(phase, safeAdd(getCompletedRework(phase), amount));
        reworkCompletedThisWeek.put(phase, safeAdd(getReworkCompletedThisWeek(phase), amount));
        defectsCreatedThisWeek.put(phase, safeAdd(getDefectsCreatedThisWeek(phase), bad));
    }

    public double discoverDefects(ProjectPhase phase, double requested) {
        double found = Math.min(getUnknownRework(phase),
                SimulationValues.nonNegativeFinite(requested));
        unknownRework.put(phase, Math.max(0.0, getUnknownRework(phase) - found));
        knownRework.put(phase, safeAdd(getKnownRework(phase), found));
        defectsDiscoveredThisWeek.put(phase,
                safeAdd(getDefectsDiscoveredThisWeek(phase), found));
        return found;
    }

    public void addCompletedWork(ProjectPhase phase, double amount) {
        double work = Math.min(getBaseWorkRemaining(phase), SimulationValues.nonNegativeFinite(amount));
        recordNewWork(phase, work, 0.0);
    }

    public void addKnownRework(ProjectPhase phase, double amount) {
        adjust(knownRework, phase, amount);
    }

    public void addUnknownRework(ProjectPhase phase, double amount) {
        adjust(unknownRework, phase, amount);
    }

    public void addCompletedRework(ProjectPhase phase, double amount) {
        adjust(reworkCompleted, phase, amount);
    }

    public double addTestableWork(ProjectPhase phase, double amount) {
        double accepted = SimulationValues.nonNegativeFinite(amount);
        testingBacklog.put(phase, safeAdd(getTestingBacklog(phase), accepted));
        return accepted;
    }

    public double inspectTestableWork(ProjectPhase phase, double amount) {
        double inspected = Math.min(getTestingBacklog(phase),
                SimulationValues.nonNegativeFinite(amount));
        testingBacklog.put(phase, Math.max(0.0, getTestingBacklog(phase) - inspected));
        return inspected;
    }

    public double getTestingBacklog(ProjectPhase phase) {
        return testingBacklog.getOrDefault(phase, 0.0);
    }

    public Map<ProjectPhase, Double> phaseTestingBacklog() {
        return Map.copyOf(testingBacklog);
    }

    public double getTotalTestingBacklog() {
        return sum(testingBacklog);
    }

    public double getTotalWorkAttemptedThisWeek() {
        return sum(workAttemptedThisWeek);
    }

    public double getTotalDefectsCreatedThisWeek() {
        return sum(defectsCreatedThisWeek);
    }

    public double getTotalDefectsDiscoveredThisWeek() {
        return sum(defectsDiscoveredThisWeek);
    }

    public double getTotalReworkCompletedThisWeek() {
        return sum(reworkCompletedThisWeek);
    }

    public double totalRemainingWork() {
        return perceivedRemainingWork() + totalUnknownRework();
    }

    public double totalScope() {
        return sum(initialBaseWork);
    }

    public double perceivedRemainingWork() {
        double total = 0.0;
        for (ProjectPhase phase : ProjectPhase.values()) {
            total = safeAdd(total, getBaseWorkRemaining(phase));
            total = safeAdd(total, getKnownRework(phase));
        }
        return total;
    }

    public boolean isReleaseReady() {
        for (ProjectPhase phase : ProjectPhase.values()) {
            if (getBaseWorkRemaining(phase) > 1.0e-8 || getKnownRework(phase) > 1.0e-8) {
                return false;
            }
        }
        return getTotalTestingBacklog() <= 1.0e-8;
    }

    public double totalCompletedWork() {
        return sum(correctWorkCompleted);
    }

    public double totalUnknownRework() {
        return sum(unknownRework);
    }

    public double totalKnownRework() {
        return sum(knownRework);
    }

    public double perceivedPhaseProgress(ProjectPhase phase) {
        double scope = getTotalWork(phase);
        return scope <= 0.0 ? 1.0 : SimulationValues.unitInterval(
                (getCorrectWorkCompleted(phase) + getUnknownRework(phase)) / scope);
    }

    public double truePhaseProgress(ProjectPhase phase) {
        double scope = getTotalWork(phase);
        return scope <= 0.0 ? 1.0 : SimulationValues.unitInterval(
                getCorrectWorkCompleted(phase) / scope);
    }

    public Map<ProjectPhase, Double> phasePerceivedProgress() {
        Map<ProjectPhase, Double> result = new EnumMap<>(ProjectPhase.class);
        for (ProjectPhase phase : ProjectPhase.values()) {
            result.put(phase, perceivedPhaseProgress(phase));
        }
        return Map.copyOf(result);
    }

    public Map<ProjectPhase, Double> phaseTrueProgress() {
        Map<ProjectPhase, Double> result = new EnumMap<>(ProjectPhase.class);
        for (ProjectPhase phase : ProjectPhase.values()) {
            result.put(phase, truePhaseProgress(phase));
        }
        return Map.copyOf(result);
    }

    public Map<ProjectPhase, Double> phaseWorkAttemptedThisWeek() {
        return Map.copyOf(workAttemptedThisWeek);
    }

    public Map<ProjectPhase, Double> phaseCorrectWorkCompleted() {
        return Map.copyOf(correctWorkCompleted);
    }

    public Map<ProjectPhase, Double> phaseUnknownRework() {
        return Map.copyOf(unknownRework);
    }

    public Map<ProjectPhase, Double> phaseKnownRework() {
        return Map.copyOf(knownRework);
    }

    public Map<ProjectPhase, Double> phaseReworkCompleted() {
        return Map.copyOf(reworkCompleted);
    }

    public Map<ProjectPhase, Double> phaseDefectsCreatedThisWeek() {
        return Map.copyOf(defectsCreatedThisWeek);
    }

    public Map<ProjectPhase, Double> phaseDefectsDiscoveredThisWeek() {
        return Map.copyOf(defectsDiscoveredThisWeek);
    }

    public Map<ProjectPhase, Double> phaseReworkCompletedThisWeek() {
        return Map.copyOf(reworkCompletedThisWeek);
    }

    private void adjust(Map<ProjectPhase, Double> stock, ProjectPhase phase, double amount) {
        if (!Double.isFinite(amount)) {
            return;
        }
        stock.put(phase, Math.max(0.0, safeAdd(stock.getOrDefault(phase, 0.0), amount)));
    }

    private double sum(Map<ProjectPhase, Double> values) {
        double total = 0.0;
        for (double value : values.values()) {
            total = safeAdd(total, value);
        }
        return total;
    }

    private double safeAdd(double left, double right) {
        if (right > 0.0 && left > Double.MAX_VALUE - right) {
            return Double.MAX_VALUE;
        }
        return left + right;
    }
}
