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
    private WorkAllocation workAllocation = new WorkAllocation();
    private PhaseReadiness phaseReadiness = new PhaseReadiness();
    private Testing testing = new Testing();
    private InitialTeamExperience initialTeamExperience = new InitialTeamExperience();
    private Mentoring mentoring = new Mentoring();
    private Coordination coordination = new Coordination();
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

    public WorkAllocation getWorkAllocation() { return workAllocation; }
    public void setWorkAllocation(WorkAllocation value) { workAllocation = value; }
    public PhaseReadiness getPhaseReadiness() { return phaseReadiness; }
    public void setPhaseReadiness(PhaseReadiness value) { phaseReadiness = value; }
    public Testing getTesting() { return testing; }
    public void setTesting(Testing value) { testing = value; }

    public InitialTeamExperience getInitialTeamExperience() {
        return initialTeamExperience;
    }

    public void setInitialTeamExperience(InitialTeamExperience initialTeamExperience) {
        this.initialTeamExperience = initialTeamExperience;
    }

    public Mentoring getMentoring() {
        return mentoring;
    }

    public void setMentoring(Mentoring mentoring) {
        this.mentoring = mentoring;
    }

    public Coordination getCoordination() {
        return coordination;
    }

    public void setCoordination(Coordination coordination) {
        this.coordination = coordination;
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
                || onboarding == null || costs == null || initialTeamExperience == null
                || workAllocation == null || phaseReadiness == null || testing == null || mentoring == null
                || coordination == null || schedule == null || turnover == null) {
            throw new IllegalArgumentException("Simulation configuration sections cannot be null");
        }
        if (initialTeamExperience.developer == null || initialTeamExperience.qaEngineer == null
                || initialTeamExperience.devopsEngineer == null
                || initialTeamExperience.projectManager == null) {
            throw new IllegalArgumentException("Initial team experience settings cannot be null");
        }
        validateNonNegative(
                productivity.junior, productivity.mid, productivity.senior,
                productivity.baseTeamSizePenalty, productivity.coordinationMaxPenalty,
                quality.baseDefectRate, quality.defectCap, quality.reworkCreationRate,
                qa.baseDetectionRate, qa.capacityMultiplier, fatigue.recoveryRate,
                fatigue.increasedWorkRate, fatigue.crunchRate, fatigue.maxValue,
                fatigue.burnoutThreshold,                 onboarding.initialEffectiveness,
                mentoring.seniorCapacity, mentoring.midLevelCapacity,
                mentoring.juniorDemand, mentoring.onboardingDemand,
                mentoring.minimumProgressFactor, mentoring.maximumDirectProductivityLoss,
                coordination.pairScale, coordination.projectManagerReduction,
                coordination.seniorReduction, coordination.maximumPenalty,
                quality.juniorDefectMultiplier, quality.midDefectMultiplier,
                quality.seniorDefectMultiplier, quality.reworkDefectMultiplier,
                quality.propagationFactor, quality.regressionTestingFactor,
                qa.throughputPerCapacity, workAllocation.knownReworkCapacityShare,
                phaseReadiness.minimumOverlap, testing.lowCapacityFactor,
                testing.normalCapacityFactor, testing.highCapacityFactor,
                testing.lowDiscoveryFactor, testing.normalDiscoveryFactor,
                testing.highDiscoveryFactor, testing.moderateBacklogThreshold,
                testing.highBacklogThreshold, testing.criticalBacklogThreshold,
                testing.concerningKnownRework, testing.atRiskKnownRework,
                costs.juniorQaWeekly, costs.midQaWeekly, costs.seniorQaWeekly,
                costs.juniorDevopsWeekly, costs.midDevopsWeekly, costs.seniorDevopsWeekly,
                costs.juniorProjectManagerWeekly, costs.midProjectManagerWeekly,
                costs.seniorProjectManagerWeekly, costs.hiringCostJunior,
                costs.hiringCostMidLevel, costs.hiringCostSenior,
                costs.overtimePremiumRate, schedule.pressureWeight,
                schedule.deadlineUrgencyWeight, schedule.targetScheduleHealth,
                turnover.baseRate, turnover.fatigueWeight, turnover.pressureWeight,
                turnover.moraleWeight, turnover.maxRate
        );
        validateNonNegative(
                costs.juniorDeveloperWeekly, costs.midDeveloperWeekly, costs.seniorDeveloperWeekly
        );
        if (quality.baseDefectRate > 1 || quality.defectCap > 1 || qa.baseDetectionRate > 1
                || fatigue.maxValue > 1 || fatigue.burnoutThreshold > 1
                || productivity.coordinationMaxPenalty > 1 || turnover.maxRate > 1
                || onboarding.initialEffectiveness > 1 || mentoring.minimumProgressFactor > 1
                || mentoring.maximumDirectProductivityLoss > 1
                || coordination.projectManagerReduction > 1 || coordination.seniorReduction > 1
                || coordination.maximumPenalty > 1) {
            throw new IllegalArgumentException("Probability and normalized configuration values must not exceed 1");
        }
        if (quality.baseDefectRate > 1 || quality.defectCap > 1
                || quality.juniorDefectMultiplier > 5 || quality.midDefectMultiplier > 5
                || quality.seniorDefectMultiplier > 5 || quality.reworkDefectMultiplier > 5
                || quality.propagationFactor > 5 || quality.regressionTestingFactor > 1
                || qa.baseDetectionRate > 1 || testing.lowCapacityFactor > 2
                || testing.normalCapacityFactor > 2 || testing.highCapacityFactor > 2
                || testing.lowDiscoveryFactor > 2 || testing.normalDiscoveryFactor > 2
                || testing.highDiscoveryFactor > 2 || workAllocation.knownReworkCapacityShare > 1
                || phaseReadiness.minimumOverlap > 1
                || testing.moderateBacklogThreshold > testing.highBacklogThreshold
                || testing.highBacklogThreshold > testing.criticalBacklogThreshold
                || testing.concerningKnownRework > testing.atRiskKnownRework) {
            throw new IllegalArgumentException("Quality and testing configuration values are out of range");
        }
        if (onboarding.juniorWeeks < 1 || onboarding.midWeeks < 1 || onboarding.seniorWeeks < 1
                || costs.hiringDelayWeeks < 0 || costs.juniorHiringDelayWeeks < 0
                || costs.midHiringDelayWeeks < 0 || costs.seniorHiringDelayWeeks < 0) {
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
        private double juniorDefectMultiplier = 1.35;
        private double midDefectMultiplier = 1.0;
        private double seniorDefectMultiplier = 0.75;
        private double reworkDefectMultiplier = 0.65;
        private double propagationFactor = 0.2;
        private double regressionTestingFactor = 0.25;

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
        public double getJuniorDefectMultiplier() { return juniorDefectMultiplier; }
        public void setJuniorDefectMultiplier(double value) { juniorDefectMultiplier = value; }
        public double getMidDefectMultiplier() { return midDefectMultiplier; }
        public void setMidDefectMultiplier(double value) { midDefectMultiplier = value; }
        public double getSeniorDefectMultiplier() { return seniorDefectMultiplier; }
        public void setSeniorDefectMultiplier(double value) { seniorDefectMultiplier = value; }
        public double getReworkDefectMultiplier() { return reworkDefectMultiplier; }
        public void setReworkDefectMultiplier(double value) { reworkDefectMultiplier = value; }
        public double getPropagationFactor() { return propagationFactor; }
        public void setPropagationFactor(double value) { propagationFactor = value; }
        public double getRegressionTestingFactor() { return regressionTestingFactor; }
        public void setRegressionTestingFactor(double value) { regressionTestingFactor = value; }
    }

    public static class Qa {
        private double baseDetectionRate = 0.18;
        private double capacityMultiplier = 1.0;
        private double throughputPerCapacity = 180.0;

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
        public double getThroughputPerCapacity() { return throughputPerCapacity; }
        public void setThroughputPerCapacity(double value) { throughputPerCapacity = value; }
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
        private double initialEffectiveness = 0.4;

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

        public double getInitialEffectiveness() {
            return initialEffectiveness;
        }

        public void setInitialEffectiveness(double initialEffectiveness) {
            this.initialEffectiveness = initialEffectiveness;
        }
    }

    public static class Mentoring {
        private double seniorCapacity = 1.0;
        private double midLevelCapacity = 0.5;
        private double juniorDemand = 0.45;
        private double onboardingDemand = 0.35;
        private double minimumProgressFactor = 0.25;
        private double maximumDirectProductivityLoss = 0.25;

        public double getSeniorCapacity() { return seniorCapacity; }
        public void setSeniorCapacity(double value) { seniorCapacity = value; }
        public double getMidLevelCapacity() { return midLevelCapacity; }
        public void setMidLevelCapacity(double value) { midLevelCapacity = value; }
        public double getJuniorDemand() { return juniorDemand; }
        public void setJuniorDemand(double value) { juniorDemand = value; }
        public double getOnboardingDemand() { return onboardingDemand; }
        public void setOnboardingDemand(double value) { onboardingDemand = value; }
        public double getMinimumProgressFactor() { return minimumProgressFactor; }
        public void setMinimumProgressFactor(double value) { minimumProgressFactor = value; }
        public double getMaximumDirectProductivityLoss() { return maximumDirectProductivityLoss; }
        public void setMaximumDirectProductivityLoss(double value) { maximumDirectProductivityLoss = value; }
    }

    public static class Coordination {
        private double pairScale = 0.011;
        private double projectManagerReduction = 0.18;
        private double seniorReduction = 0.025;
        private double maximumPenalty = 0.45;

        public double getPairScale() { return pairScale; }
        public void setPairScale(double value) { pairScale = value; }
        public double getProjectManagerReduction() { return projectManagerReduction; }
        public void setProjectManagerReduction(double value) { projectManagerReduction = value; }
        public double getSeniorReduction() { return seniorReduction; }
        public void setSeniorReduction(double value) { seniorReduction = value; }
        public double getMaximumPenalty() { return maximumPenalty; }
        public void setMaximumPenalty(double value) { maximumPenalty = value; }
    }

    public static class WorkAllocation {
        private double knownReworkCapacityShare = 0.35;
        public double getKnownReworkCapacityShare() { return knownReworkCapacityShare; }
        public void setKnownReworkCapacityShare(double value) { knownReworkCapacityShare = value; }
    }

    public static class PhaseReadiness {
        private double minimumOverlap = 0.1;
        public double getMinimumOverlap() { return minimumOverlap; }
        public void setMinimumOverlap(double value) { minimumOverlap = value; }
    }

    public static class Testing {
        private double lowCapacityFactor = 0.65;
        private double normalCapacityFactor = 1.0;
        private double highCapacityFactor = 1.3;
        private double lowDiscoveryFactor = 0.65;
        private double normalDiscoveryFactor = 1.0;
        private double highDiscoveryFactor = 1.25;
        private double moderateBacklogThreshold = 250.0;
        private double highBacklogThreshold = 650.0;
        private double criticalBacklogThreshold = 1200.0;
        private double concerningKnownRework = 30.0;
        private double atRiskKnownRework = 100.0;

        public double capacityFactor(edu.simulator.model.TestingPriority priority) {
            return switch (priority) {
                case LOW -> lowCapacityFactor;
                case NORMAL -> normalCapacityFactor;
                case HIGH -> highCapacityFactor;
            };
        }
        public double discoveryFactor(edu.simulator.model.TestingPriority priority) {
            return switch (priority) {
                case LOW -> lowDiscoveryFactor;
                case NORMAL -> normalDiscoveryFactor;
                case HIGH -> highDiscoveryFactor;
            };
        }
        public double getLowCapacityFactor() { return lowCapacityFactor; }
        public void setLowCapacityFactor(double value) { lowCapacityFactor = value; }
        public double getNormalCapacityFactor() { return normalCapacityFactor; }
        public void setNormalCapacityFactor(double value) { normalCapacityFactor = value; }
        public double getHighCapacityFactor() { return highCapacityFactor; }
        public void setHighCapacityFactor(double value) { highCapacityFactor = value; }
        public double getLowDiscoveryFactor() { return lowDiscoveryFactor; }
        public void setLowDiscoveryFactor(double value) { lowDiscoveryFactor = value; }
        public double getNormalDiscoveryFactor() { return normalDiscoveryFactor; }
        public void setNormalDiscoveryFactor(double value) { normalDiscoveryFactor = value; }
        public double getHighDiscoveryFactor() { return highDiscoveryFactor; }
        public void setHighDiscoveryFactor(double value) { highDiscoveryFactor = value; }
        public double getModerateBacklogThreshold() { return moderateBacklogThreshold; }
        public void setModerateBacklogThreshold(double value) { moderateBacklogThreshold = value; }
        public double getHighBacklogThreshold() { return highBacklogThreshold; }
        public void setHighBacklogThreshold(double value) { highBacklogThreshold = value; }
        public double getCriticalBacklogThreshold() { return criticalBacklogThreshold; }
        public void setCriticalBacklogThreshold(double value) { criticalBacklogThreshold = value; }
        public double getConcerningKnownRework() { return concerningKnownRework; }
        public void setConcerningKnownRework(double value) { concerningKnownRework = value; }
        public double getAtRiskKnownRework() { return atRiskKnownRework; }
        public void setAtRiskKnownRework(double value) { atRiskKnownRework = value; }
    }

    public static class InitialTeamExperience {
        private edu.simulator.model.ExperienceLevel developer = edu.simulator.model.ExperienceLevel.MID_LEVEL;
        private edu.simulator.model.ExperienceLevel qaEngineer = edu.simulator.model.ExperienceLevel.MID_LEVEL;
        private edu.simulator.model.ExperienceLevel devopsEngineer = edu.simulator.model.ExperienceLevel.MID_LEVEL;
        private edu.simulator.model.ExperienceLevel projectManager = edu.simulator.model.ExperienceLevel.SENIOR;

        public edu.simulator.model.ExperienceLevel getDeveloper() { return developer; }
        public void setDeveloper(edu.simulator.model.ExperienceLevel value) { developer = value; }
        public edu.simulator.model.ExperienceLevel getQaEngineer() { return qaEngineer; }
        public void setQaEngineer(edu.simulator.model.ExperienceLevel value) { qaEngineer = value; }
        public edu.simulator.model.ExperienceLevel getDevopsEngineer() { return devopsEngineer; }
        public void setDevopsEngineer(edu.simulator.model.ExperienceLevel value) { devopsEngineer = value; }
        public edu.simulator.model.ExperienceLevel getProjectManager() { return projectManager; }
        public void setProjectManager(edu.simulator.model.ExperienceLevel value) { projectManager = value; }

        public edu.simulator.model.ExperienceLevel forRole(edu.simulator.model.Role role) {
            return switch (role) {
                case DEVELOPER -> developer;
                case QA_ENGINEER -> qaEngineer;
                case DEVOPS_ENGINEER -> devopsEngineer;
                case PROJECT_MANAGER -> projectManager;
            };
        }
    }

    public static class Costs {
        private double juniorDeveloperWeekly = 1500.0;
        private double midDeveloperWeekly = 2100.0;
        private double seniorDeveloperWeekly = 2900.0;
        private double juniorQaWeekly = 1400.0;
        private double midQaWeekly = 1900.0;
        private double seniorQaWeekly = 2500.0;
        private double juniorDevopsWeekly = 1700.0;
        private double midDevopsWeekly = 2400.0;
        private double seniorDevopsWeekly = 3100.0;
        private double juniorProjectManagerWeekly = 1800.0;
        private double midProjectManagerWeekly = 2600.0;
        private double seniorProjectManagerWeekly = 3400.0;
        private int hiringDelayWeeks = 1;
        private int juniorHiringDelayWeeks = 1;
        private int midHiringDelayWeeks = 2;
        private int seniorHiringDelayWeeks = 3;
        private double hiringCostJunior = 1000.0;
        private double hiringCostMidLevel = 1500.0;
        private double hiringCostSenior = 2500.0;
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

        public double getJuniorQaWeekly() { return juniorQaWeekly; }
        public void setJuniorQaWeekly(double value) { juniorQaWeekly = value; }
        public double getMidQaWeekly() { return midQaWeekly; }
        public void setMidQaWeekly(double value) { midQaWeekly = value; }
        public double getSeniorQaWeekly() { return seniorQaWeekly; }
        public void setSeniorQaWeekly(double value) { seniorQaWeekly = value; }
        public double getJuniorDevopsWeekly() { return juniorDevopsWeekly; }
        public void setJuniorDevopsWeekly(double value) { juniorDevopsWeekly = value; }
        public double getMidDevopsWeekly() { return midDevopsWeekly; }
        public void setMidDevopsWeekly(double value) { midDevopsWeekly = value; }
        public double getSeniorDevopsWeekly() { return seniorDevopsWeekly; }
        public void setSeniorDevopsWeekly(double value) { seniorDevopsWeekly = value; }
        public double getJuniorProjectManagerWeekly() { return juniorProjectManagerWeekly; }
        public void setJuniorProjectManagerWeekly(double value) { juniorProjectManagerWeekly = value; }
        public double getMidProjectManagerWeekly() { return midProjectManagerWeekly; }
        public void setMidProjectManagerWeekly(double value) { midProjectManagerWeekly = value; }
        public double getSeniorProjectManagerWeekly() { return seniorProjectManagerWeekly; }
        public void setSeniorProjectManagerWeekly(double value) { seniorProjectManagerWeekly = value; }

        public double getQaWeekly() {
            return midQaWeekly;
        }

        public void setQaWeekly(double qaWeekly) {
            this.midQaWeekly = qaWeekly;
        }

        public double getDevopsWeekly() {
            return midDevopsWeekly;
        }

        public void setDevopsWeekly(double devopsWeekly) {
            this.midDevopsWeekly = devopsWeekly;
        }

        public double getProjectManagerWeekly() {
            return midProjectManagerWeekly;
        }

        public void setProjectManagerWeekly(double projectManagerWeekly) {
            this.midProjectManagerWeekly = projectManagerWeekly;
        }

        public int getHiringDelayWeeks() {
            return hiringDelayWeeks;
        }

        public void setHiringDelayWeeks(int hiringDelayWeeks) {
            this.hiringDelayWeeks = hiringDelayWeeks;
        }

        public int getJuniorHiringDelayWeeks() { return juniorHiringDelayWeeks; }
        public void setJuniorHiringDelayWeeks(int value) { juniorHiringDelayWeeks = value; }
        public int getMidHiringDelayWeeks() { return midHiringDelayWeeks; }
        public void setMidHiringDelayWeeks(int value) { midHiringDelayWeeks = value; }
        public int getSeniorHiringDelayWeeks() { return seniorHiringDelayWeeks; }
        public void setSeniorHiringDelayWeeks(int value) { seniorHiringDelayWeeks = value; }
        public double getHiringCostJunior() { return hiringCostJunior; }
        public void setHiringCostJunior(double value) { hiringCostJunior = value; }
        public double getHiringCostMidLevel() { return hiringCostMidLevel; }
        public void setHiringCostMidLevel(double value) { hiringCostMidLevel = value; }
        public double getHiringCostSenior() { return hiringCostSenior; }
        public void setHiringCostSenior(double value) { hiringCostSenior = value; }

        public double weeklySalary(edu.simulator.model.Role role, edu.simulator.model.ExperienceLevel level) {
            return switch (role) {
                case DEVELOPER -> switch (level) {
                    case JUNIOR -> juniorDeveloperWeekly;
                    case MID_LEVEL -> midDeveloperWeekly;
                    case SENIOR -> seniorDeveloperWeekly;
                };
                case QA_ENGINEER -> switch (level) {
                    case JUNIOR -> juniorQaWeekly;
                    case MID_LEVEL -> midQaWeekly;
                    case SENIOR -> seniorQaWeekly;
                };
                case DEVOPS_ENGINEER -> switch (level) {
                    case JUNIOR -> juniorDevopsWeekly;
                    case MID_LEVEL -> midDevopsWeekly;
                    case SENIOR -> seniorDevopsWeekly;
                };
                case PROJECT_MANAGER -> switch (level) {
                    case JUNIOR -> juniorProjectManagerWeekly;
                    case MID_LEVEL -> midProjectManagerWeekly;
                    case SENIOR -> seniorProjectManagerWeekly;
                };
            };
        }

        public int hiringDelayWeeks(edu.simulator.model.ExperienceLevel level) {
            return switch (level) {
                case JUNIOR -> juniorHiringDelayWeeks;
                case MID_LEVEL -> midHiringDelayWeeks;
                case SENIOR -> seniorHiringDelayWeeks;
            };
        }

        public double hiringCost(edu.simulator.model.ExperienceLevel level) {
            return switch (level) {
                case JUNIOR -> hiringCostJunior;
                case MID_LEVEL -> hiringCostMidLevel;
                case SENIOR -> hiringCostSenior;
            };
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
