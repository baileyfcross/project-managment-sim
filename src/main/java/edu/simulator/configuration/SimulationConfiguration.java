package edu.simulator.configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SimulationConfiguration {
    private Productivity productivity = new Productivity();
    private Quality quality = new Quality();
    private Qa qa = new Qa();
    private Fatigue fatigue = new Fatigue();
    private Onboarding onboarding = new Onboarding();
    private Costs costs = new Costs();
    private Schedule schedule = new Schedule();
    private Turnover turnover = new Turnover();

    public Productivity getProductivity() {
        return productivity;
    }

    public void setProductivity(Productivity productivity) {
        this.productivity = productivity;
    }

    public Quality getQuality() {
        return quality;
    }

    public void setQuality(Quality quality) {
        this.quality = quality;
    }

    public Qa getQa() {
        return qa;
    }

    public void setQa(Qa qa) {
        this.qa = qa;
    }

    public Fatigue getFatigue() {
        return fatigue;
    }

    public void setFatigue(Fatigue fatigue) {
        this.fatigue = fatigue;
    }

    public Onboarding getOnboarding() {
        return onboarding;
    }

    public void setOnboarding(Onboarding onboarding) {
        this.onboarding = onboarding;
    }

    public Costs getCosts() {
        return costs;
    }

    public void setCosts(Costs costs) {
        this.costs = costs;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public Turnover getTurnover() {
        return turnover;
    }

    public void setTurnover(Turnover turnover) {
        this.turnover = turnover;
    }

    public void validate() {
        if (productivity == null || quality == null || qa == null || fatigue == null
                || onboarding == null || costs == null || schedule == null || turnover == null) {
            throw new IllegalArgumentException("Simulation configuration sections cannot be null");
        }
        validateNonNegative(
                productivity.junior, productivity.mid, productivity.senior,
                productivity.baseTeamSizePenalty, productivity.coordinationMaxPenalty,
                quality.baseDefectRate, quality.defectCap, quality.reworkCreationRate,
                qa.baseDetectionRate, qa.capacityMultiplier, fatigue.recoveryRate,
                fatigue.increasedWorkRate, fatigue.crunchRate, fatigue.maxValue,
                fatigue.burnoutThreshold, onboarding.mentoringCapacity,
                costs.juniorDeveloperWeekly, costs.midDeveloperWeekly, costs.seniorDeveloperWeekly,
                costs.qaWeekly, costs.devopsWeekly, costs.projectManagerWeekly,
                costs.overtimePremiumRate, schedule.pressureWeight,
                schedule.deadlineUrgencyWeight, schedule.targetScheduleHealth,
                turnover.baseRate, turnover.fatigueWeight, turnover.pressureWeight,
                turnover.moraleWeight, turnover.maxRate
        );
        if (quality.baseDefectRate > 1 || quality.defectCap > 1 || qa.baseDetectionRate > 1
                || fatigue.maxValue > 1 || fatigue.burnoutThreshold > 1
                || productivity.coordinationMaxPenalty > 1 || turnover.maxRate > 1) {
            throw new IllegalArgumentException("Probability and normalized configuration values must not exceed 1");
        }
        if (onboarding.juniorWeeks < 1 || onboarding.midWeeks < 1 || onboarding.seniorWeeks < 1
                || costs.hiringDelayWeeks < 0) {
            throw new IllegalArgumentException("Onboarding durations must be positive and hiring delay non-negative");
        }
    }

    private void validateNonNegative(double... values) {
        for (double value : values) {
            if (!Double.isFinite(value) || value < 0.0) {
                throw new IllegalArgumentException("Simulation configuration values must be finite and non-negative");
            }
        }
    }

    public static class Productivity {
        private double junior = 0.55;
        private double mid = 1.0;
        private double senior = 1.3;
        private double baseTeamSizePenalty = 0.04;
        private double coordinationMaxPenalty = 0.32;

        public double getJunior() {
            return junior;
        }

        public void setJunior(double junior) {
            this.junior = junior;
        }

        public double getMid() {
            return mid;
        }

        public void setMid(double mid) {
            this.mid = mid;
        }

        public double getSenior() {
            return senior;
        }

        public void setSenior(double senior) {
            this.senior = senior;
        }

        public double getBaseTeamSizePenalty() {
            return baseTeamSizePenalty;
        }

        public void setBaseTeamSizePenalty(double baseTeamSizePenalty) {
            this.baseTeamSizePenalty = baseTeamSizePenalty;
        }

        public double getCoordinationMaxPenalty() {
            return coordinationMaxPenalty;
        }

        public void setCoordinationMaxPenalty(double coordinationMaxPenalty) {
            this.coordinationMaxPenalty = coordinationMaxPenalty;
        }
    }

    public static class Quality {
        private double baseDefectRate = 0.08;
        private double defectCap = 0.42;
        private double reworkCreationRate = 0.18;

        public double getBaseDefectRate() {
            return baseDefectRate;
        }

        public void setBaseDefectRate(double baseDefectRate) {
            this.baseDefectRate = baseDefectRate;
        }

        public double getDefectCap() {
            return defectCap;
        }

        public void setDefectCap(double defectCap) {
            this.defectCap = defectCap;
        }

        public double getReworkCreationRate() {
            return reworkCreationRate;
        }

        public void setReworkCreationRate(double reworkCreationRate) {
            this.reworkCreationRate = reworkCreationRate;
        }
    }

    public static class Qa {
        private double baseDetectionRate = 0.18;
        private double capacityMultiplier = 1.0;

        public double getBaseDetectionRate() {
            return baseDetectionRate;
        }

        public void setBaseDetectionRate(double baseDetectionRate) {
            this.baseDetectionRate = baseDetectionRate;
        }

        public double getCapacityMultiplier() {
            return capacityMultiplier;
        }

        public void setCapacityMultiplier(double capacityMultiplier) {
            this.capacityMultiplier = capacityMultiplier;
        }
    }

    public static class Fatigue {
        private double recoveryRate = 0.08;
        private double increasedWorkRate = 0.03;
        private double crunchRate = 0.06;
        private double maxValue = 1.0;
        private double burnoutThreshold = 0.6;

        public double getRecoveryRate() {
            return recoveryRate;
        }

        public void setRecoveryRate(double recoveryRate) {
            this.recoveryRate = recoveryRate;
        }

        public double getIncreasedWorkRate() {
            return increasedWorkRate;
        }

        public double getCrunchRate() {
            return crunchRate;
        }

        public void setCrunchRate(double crunchRate) {
            this.crunchRate = crunchRate;
        }

        public void setIncreasedWorkRate(double increasedWorkRate) {
            this.increasedWorkRate = increasedWorkRate;
        }

        public double getMaxValue() {
            return maxValue;
        }

        public void setMaxValue(double maxValue) {
            this.maxValue = maxValue;
        }

        public double getBurnoutThreshold() {
            return burnoutThreshold;
        }

        public void setBurnoutThreshold(double burnoutThreshold) {
            this.burnoutThreshold = burnoutThreshold;
        }
    }

    public static class Onboarding {
        private int juniorWeeks = 4;
        private int midWeeks = 3;
        private int seniorWeeks = 2;
        private double mentoringCapacity = 0.4;

        public int getJuniorWeeks() {
            return juniorWeeks;
        }

        public void setJuniorWeeks(int juniorWeeks) {
            this.juniorWeeks = juniorWeeks;
        }

        public int getMidWeeks() {
            return midWeeks;
        }

        public void setMidWeeks(int midWeeks) {
            this.midWeeks = midWeeks;
        }

        public int getSeniorWeeks() {
            return seniorWeeks;
        }

        public void setSeniorWeeks(int seniorWeeks) {
            this.seniorWeeks = seniorWeeks;
        }

        public double getMentoringCapacity() {
            return mentoringCapacity;
        }

        public void setMentoringCapacity(double mentoringCapacity) {
            this.mentoringCapacity = mentoringCapacity;
        }
    }

    public static class Costs {
        private double juniorDeveloperWeekly = 1500.0;
        private double midDeveloperWeekly = 2100.0;
        private double seniorDeveloperWeekly = 2900.0;
        private double qaWeekly = 1900.0;
        private double devopsWeekly = 2400.0;
        private double projectManagerWeekly = 2600.0;
        private int hiringDelayWeeks = 1;
        private double overtimePremiumRate = 0.15;

        public double getJuniorDeveloperWeekly() {
            return juniorDeveloperWeekly;
        }

        public void setJuniorDeveloperWeekly(double juniorDeveloperWeekly) {
            this.juniorDeveloperWeekly = juniorDeveloperWeekly;
        }

        public double getMidDeveloperWeekly() {
            return midDeveloperWeekly;
        }

        public void setMidDeveloperWeekly(double midDeveloperWeekly) {
            this.midDeveloperWeekly = midDeveloperWeekly;
        }

        public double getSeniorDeveloperWeekly() {
            return seniorDeveloperWeekly;
        }

        public void setSeniorDeveloperWeekly(double seniorDeveloperWeekly) {
            this.seniorDeveloperWeekly = seniorDeveloperWeekly;
        }

        public double getQaWeekly() {
            return qaWeekly;
        }

        public void setQaWeekly(double qaWeekly) {
            this.qaWeekly = qaWeekly;
        }

        public double getDevopsWeekly() {
            return devopsWeekly;
        }

        public void setDevopsWeekly(double devopsWeekly) {
            this.devopsWeekly = devopsWeekly;
        }

        public double getProjectManagerWeekly() {
            return projectManagerWeekly;
        }

        public void setProjectManagerWeekly(double projectManagerWeekly) {
            this.projectManagerWeekly = projectManagerWeekly;
        }

        public int getHiringDelayWeeks() {
            return hiringDelayWeeks;
        }

        public void setHiringDelayWeeks(int hiringDelayWeeks) {
            this.hiringDelayWeeks = hiringDelayWeeks;
        }

        public double getOvertimePremiumRate() {
            return overtimePremiumRate;
        }

        public void setOvertimePremiumRate(double overtimePremiumRate) {
            this.overtimePremiumRate = overtimePremiumRate;
        }
    }

    public static class Schedule {
        private double pressureWeight = 0.06;
        private double deadlineUrgencyWeight = 0.14;
        private double targetScheduleHealth = 0.85;

        public double getPressureWeight() {
            return pressureWeight;
        }

        public void setPressureWeight(double pressureWeight) {
            this.pressureWeight = pressureWeight;
        }

        public double getDeadlineUrgencyWeight() {
            return deadlineUrgencyWeight;
        }

        public void setDeadlineUrgencyWeight(double deadlineUrgencyWeight) {
            this.deadlineUrgencyWeight = deadlineUrgencyWeight;
        }

        public double getTargetScheduleHealth() {
            return targetScheduleHealth;
        }

        public void setTargetScheduleHealth(double targetScheduleHealth) {
            this.targetScheduleHealth = targetScheduleHealth;
        }
    }

    public static class Turnover {
        private double baseRate = 0.012;
        private double fatigueWeight = 0.25;
        private double pressureWeight = 0.18;
        private double moraleWeight = 0.12;
        private double maxRate = 0.2;

        public double getBaseRate() {
            return baseRate;
        }

        public void setBaseRate(double baseRate) {
            this.baseRate = baseRate;
        }

        public double getFatigueWeight() {
            return fatigueWeight;
        }

        public void setFatigueWeight(double fatigueWeight) {
            this.fatigueWeight = fatigueWeight;
        }

        public double getPressureWeight() {
            return pressureWeight;
        }

        public void setPressureWeight(double pressureWeight) {
            this.pressureWeight = pressureWeight;
        }

        public double getMoraleWeight() {
            return moraleWeight;
        }

        public void setMoraleWeight(double moraleWeight) {
            this.moraleWeight = moraleWeight;
        }

        public double getMaxRate() {
            return maxRate;
        }

        public void setMaxRate(double maxRate) {
            this.maxRate = maxRate;
        }
    }
}
