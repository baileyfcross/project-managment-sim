package edu.simulator.model;

import java.util.Objects;
import java.util.UUID;

public class Employee {
    private final String id;
    private final Role role;
    private final ExperienceLevel experienceLevel;
    private int weeksOnProject;
    private double fatigue;
    private double morale;
    private double baseWeeklyCost;
    private double onboardingProgress;
    private boolean active;

    public Employee(Role role, ExperienceLevel experienceLevel, double baseWeeklyCost) {
        this.id = UUID.randomUUID().toString();
        this.role = Objects.requireNonNull(role, "role");
        this.experienceLevel = Objects.requireNonNull(experienceLevel, "experienceLevel");
        setBaseWeeklyCost(baseWeeklyCost);
        this.active = true;
        this.fatigue = 0.0;
        this.morale = 0.7;
    }

    public String getId() {
        return id;
    }

    public Role getRole() {
        return role;
    }

    public ExperienceLevel getExperienceLevel() {
        return experienceLevel;
    }

    public int getWeeksOnProject() {
        return weeksOnProject;
    }

    public void setWeeksOnProject(int weeksOnProject) {
        this.weeksOnProject = Math.max(0, weeksOnProject);
    }

    public void incrementWeek() {
        weeksOnProject++;
    }

    public double getFatigue() {
        return fatigue;
    }

    public void setFatigue(double fatigue) {
        this.fatigue = clamp(fatigue);
    }

    public double getMorale() {
        return morale;
    }

    public void setMorale(double morale) {
        this.morale = clamp(morale);
    }

    public double getBaseWeeklyCost() {
        return baseWeeklyCost;
    }

    public void setBaseWeeklyCost(double baseWeeklyCost) {
        if (!Double.isFinite(baseWeeklyCost) || baseWeeklyCost < 0.0) {
            throw new IllegalArgumentException("Employee weekly cost must be finite and non-negative");
        }
        this.baseWeeklyCost = baseWeeklyCost;
    }

    public double getOnboardingProgress() {
        return onboardingProgress;
    }

    public void setOnboardingProgress(double onboardingProgress) {
        this.onboardingProgress = clamp(onboardingProgress);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    private double clamp(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, value));
    }
}
