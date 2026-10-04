package edu.simulator.model;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Internal end-of-week record; hidden fields are not part of gameplay DTOs. */
public class WeeklySnapshot {
    private final int week;
    private final BigDecimal spent;
    private final BigDecimal remainingBudget;
    private final BigDecimal weeklyCost;
    private final double perceivedProgress;
    private final double trueProgress;
    private final int teamSize;
    private final Map<Role, Integer> teamCounts;
    private final Map<Role, Map<ExperienceLevel, Integer>> experienceCounts;
    private final int onboardingEmployees;
    private final int pendingHires;
    private final double averageOnboardingEffectiveness;
    private final double mentoringLoad;
    private final double mentoringCoverage;
    private final double coordinationOverhead;
    private final BigDecimal weeklyPayroll;
    private final BigDecimal hiringCost;
    private final double averageProductivity;
    private final double averageFatigue;
    private final double knownRework;
    private final double unknownRework;
    private final int estimatedCompletionWeek;
    private final double schedulePressure;
    private final List<String> recentMessages;
    private final Map<ProjectPhase, Double> workAttemptedByPhase;
    private final Map<ProjectPhase, Double> correctWorkByPhase;
    private final Map<ProjectPhase, Double> unknownReworkByPhase;
    private final Map<ProjectPhase, Double> knownReworkByPhase;
    private final Map<ProjectPhase, Double> reworkCompletedByPhase;
    private final Map<ProjectPhase, Double> defectsCreatedByPhase;
    private final Map<ProjectPhase, Double> defectsDiscoveredByPhase;
    private final double testingBacklog;
    private final double qaCapacity;
    private final int releasedDefects;
    private final String qualityHealth;
    private final String testingBacklogStatus;
    private final TestingPriority testingPriority;
    private final WorkIntensity workIntensity;
    private final double maximumFatigue;
    private final double averageMorale;
    private final BigDecimal overtimeCost;
    private final double averageOvertimeStreak;
    private final String turnoverRisk;
    private final List<String> employeesDeparted;

    public WeeklySnapshot(int week, BigDecimal spent, BigDecimal remainingBudget,
                          BigDecimal weeklyCost, double perceivedProgress,
                          double trueProgress, int teamSize, Map<Role, Integer> teamCounts,
                          Map<Role, Map<ExperienceLevel, Integer>> experienceCounts,
                          int onboardingEmployees, int pendingHires,
                          double averageOnboardingEffectiveness, double mentoringLoad,
                          double mentoringCoverage, double coordinationOverhead,
                          BigDecimal weeklyPayroll, BigDecimal hiringCost,
                          double averageProductivity, double averageFatigue,
                          double knownRework, double unknownRework,
                          int estimatedCompletionWeek, double schedulePressure,
                          List<String> recentMessages,
                          Map<ProjectPhase, Double> workAttemptedByPhase,
                          Map<ProjectPhase, Double> correctWorkByPhase,
                          Map<ProjectPhase, Double> unknownReworkByPhase,
                          Map<ProjectPhase, Double> knownReworkByPhase,
                          Map<ProjectPhase, Double> reworkCompletedByPhase,
                          Map<ProjectPhase, Double> defectsCreatedByPhase,
                          Map<ProjectPhase, Double> defectsDiscoveredByPhase,
                          double testingBacklog, double qaCapacity, int releasedDefects,
                          String qualityHealth, String testingBacklogStatus,
                          TestingPriority testingPriority, WorkIntensity workIntensity,
                          double maximumFatigue, double averageMorale, BigDecimal overtimeCost,
                          double averageOvertimeStreak, String turnoverRisk,
                          List<String> employeesDeparted) {
        if (week < 1) {
            throw new IllegalArgumentException("A weekly snapshot must represent Week 1 or later");
        }
        this.week = week;
        this.spent = spent;
        this.remainingBudget = remainingBudget;
        this.weeklyCost = weeklyCost;
        this.perceivedProgress = SimulationValues.unitInterval(perceivedProgress);
        this.trueProgress = SimulationValues.unitInterval(trueProgress);
        this.teamSize = Math.max(0, teamSize);
        this.teamCounts = Map.copyOf(new EnumMap<>(teamCounts));
        Map<Role, Map<ExperienceLevel, Integer>> copiedExperienceCounts = new EnumMap<>(Role.class);
        experienceCounts.forEach((role, counts) ->
                copiedExperienceCounts.put(role, Map.copyOf(new EnumMap<>(counts))));
        this.experienceCounts = Map.copyOf(copiedExperienceCounts);
        this.onboardingEmployees = Math.max(0, onboardingEmployees);
        this.pendingHires = Math.max(0, pendingHires);
        this.averageOnboardingEffectiveness = SimulationValues.unitInterval(averageOnboardingEffectiveness);
        this.mentoringLoad = SimulationValues.unitInterval(mentoringLoad);
        this.mentoringCoverage = SimulationValues.unitInterval(mentoringCoverage);
        this.coordinationOverhead = SimulationValues.unitInterval(coordinationOverhead);
        this.weeklyPayroll = weeklyPayroll;
        this.hiringCost = hiringCost;
        this.averageProductivity = SimulationValues.nonNegativeFinite(averageProductivity);
        this.averageFatigue = SimulationValues.unitInterval(averageFatigue);
        this.knownRework = SimulationValues.nonNegativeFinite(knownRework);
        this.unknownRework = SimulationValues.nonNegativeFinite(unknownRework);
        this.estimatedCompletionWeek = estimatedCompletionWeek;
        this.schedulePressure = SimulationValues.unitInterval(schedulePressure);
        this.recentMessages = List.copyOf(recentMessages);
        this.workAttemptedByPhase = immutablePhaseMap(workAttemptedByPhase);
        this.correctWorkByPhase = immutablePhaseMap(correctWorkByPhase);
        this.unknownReworkByPhase = immutablePhaseMap(unknownReworkByPhase);
        this.knownReworkByPhase = immutablePhaseMap(knownReworkByPhase);
        this.reworkCompletedByPhase = immutablePhaseMap(reworkCompletedByPhase);
        this.defectsCreatedByPhase = immutablePhaseMap(defectsCreatedByPhase);
        this.defectsDiscoveredByPhase = immutablePhaseMap(defectsDiscoveredByPhase);
        this.testingBacklog = SimulationValues.nonNegativeFinite(testingBacklog);
        this.qaCapacity = SimulationValues.nonNegativeFinite(qaCapacity);
        this.releasedDefects = Math.max(0, releasedDefects);
        this.qualityHealth = qualityHealth;
        this.testingBacklogStatus = testingBacklogStatus;
        this.testingPriority = testingPriority;
        this.workIntensity = workIntensity;
        this.maximumFatigue = SimulationValues.unitInterval(maximumFatigue);
        this.averageMorale = SimulationValues.unitInterval(averageMorale);
        this.overtimeCost = overtimeCost;
        this.averageOvertimeStreak = SimulationValues.nonNegativeFinite(averageOvertimeStreak);
        this.turnoverRisk = turnoverRisk;
        this.employeesDeparted = List.copyOf(employeesDeparted);
    }

    public int getWeek() {
        return week;
    }

    public BigDecimal getSpent() {
        return spent;
    }

    public BigDecimal getRemainingBudget() {
        return remainingBudget;
    }

    public BigDecimal getWeeklyCost() {
        return weeklyCost;
    }

    public double getPerceivedProgress() {
        return perceivedProgress;
    }

    public double getTrueProgress() {
        return trueProgress;
    }

    public int getTeamSize() {
        return teamSize;
    }

    public Map<Role, Integer> getTeamCounts() {
        return teamCounts;
    }

    public Map<Role, Map<ExperienceLevel, Integer>> getExperienceCounts() {
        return experienceCounts;
    }

    public int getOnboardingEmployees() {
        return onboardingEmployees;
    }

    public int getPendingHires() {
        return pendingHires;
    }

    public double getAverageOnboardingEffectiveness() {
        return averageOnboardingEffectiveness;
    }

    public double getMentoringLoad() {
        return mentoringLoad;
    }

    public double getMentoringCoverage() {
        return mentoringCoverage;
    }

    public double getCoordinationOverhead() {
        return coordinationOverhead;
    }

    public BigDecimal getWeeklyPayroll() {
        return weeklyPayroll;
    }

    public BigDecimal getHiringCost() {
        return hiringCost;
    }

    public double getAverageProductivity() {
        return averageProductivity;
    }

    public double getAverageFatigue() {
        return averageFatigue;
    }

    public double getKnownRework() {
        return knownRework;
    }

    public double getUnknownRework() {
        return unknownRework;
    }

    public int getEstimatedCompletionWeek() {
        return estimatedCompletionWeek;
    }

    public double getSchedulePressure() {
        return schedulePressure;
    }

    public List<String> getRecentMessages() {
        return recentMessages;
    }

    public Map<ProjectPhase, Double> getWorkAttemptedByPhase() { return workAttemptedByPhase; }
    public Map<ProjectPhase, Double> getCorrectWorkByPhase() { return correctWorkByPhase; }
    public Map<ProjectPhase, Double> getUnknownReworkByPhase() { return unknownReworkByPhase; }
    public Map<ProjectPhase, Double> getKnownReworkByPhase() { return knownReworkByPhase; }
    public Map<ProjectPhase, Double> getReworkCompletedByPhase() { return reworkCompletedByPhase; }
    public Map<ProjectPhase, Double> getDefectsCreatedByPhase() { return defectsCreatedByPhase; }
    public Map<ProjectPhase, Double> getDefectsDiscoveredByPhase() { return defectsDiscoveredByPhase; }
    public double getTestingBacklog() { return testingBacklog; }
    public double getQaCapacity() { return qaCapacity; }
    public int getReleasedDefects() { return releasedDefects; }
    public String getQualityHealth() { return qualityHealth; }
    public String getTestingBacklogStatus() { return testingBacklogStatus; }
    public TestingPriority getTestingPriority() { return testingPriority; }
    public WorkIntensity getWorkIntensity() { return workIntensity; }
    public double getMaximumFatigue() { return maximumFatigue; }
    public double getAverageMorale() { return averageMorale; }
    public BigDecimal getOvertimeCost() { return overtimeCost; }
    public double getAverageOvertimeStreak() { return averageOvertimeStreak; }
    public String getTurnoverRisk() { return turnoverRisk; }
    public List<String> getEmployeesDeparted() { return employeesDeparted; }

    private Map<ProjectPhase, Double> immutablePhaseMap(Map<ProjectPhase, Double> source) {
        EnumMap<ProjectPhase, Double> copy = new EnumMap<>(ProjectPhase.class);
        for (ProjectPhase phase : ProjectPhase.values()) {
            copy.put(phase, SimulationValues.nonNegativeFinite(source.getOrDefault(phase, 0.0)));
        }
        return Map.copyOf(copy);
    }
}
