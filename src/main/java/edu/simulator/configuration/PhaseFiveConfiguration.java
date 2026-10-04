package edu.simulator.configuration;

import edu.simulator.event.EventType;
import edu.simulator.model.ProjectPhase;

import java.util.HashMap;
import java.util.Map;

public class PhaseFiveConfiguration {
    private Events events = new Events();
    private Scope scope = new Scope();
    private TechnicalDebt technicalDebt = new TechnicalDebt();
    private EngineeringApproachSettings engineeringApproach = new EngineeringApproachSettings();
    private Concurrency concurrency = new Concurrency();

    public Events getEvents() { return events; }
    public void setEvents(Events value) { events = value; }
    public Scope getScope() { return scope; }
    public void setScope(Scope value) { scope = value; }
    public TechnicalDebt getTechnicalDebt() { return technicalDebt; }
    public void setTechnicalDebt(TechnicalDebt value) { technicalDebt = value; }
    public EngineeringApproachSettings getEngineeringApproach() { return engineeringApproach; }
    public void setEngineeringApproach(EngineeringApproachSettings value) { engineeringApproach = value; }
    public Concurrency getConcurrency() { return concurrency; }
    public void setConcurrency(Concurrency value) { concurrency = value; }

    public void validate() {
        if (events == null || scope == null || technicalDebt == null
                || engineeringApproach == null || concurrency == null) {
            throw new IllegalArgumentException("Phase 5 configuration sections cannot be null");
        }
        events.validate();
        scope.validate();
        technicalDebt.validate();
        engineeringApproach.validate();
        concurrency.validate();
    }

    public static class Events {
        private double baseProbability = 0.34;
        private int minimumSpacingWeeks = 2;
        private int cooldownWeeks = 2;
        private int maximumUnresolvedEvents = 1;
        private double featureRequestRate = 1.0;
        private double stakeholderVolatility = 1.0;
        private double dependencyRisk = 1.0;
        private double technicalDebtSensitivity = 1.0;
        private double projectComplexity = 1.0;
        private double eventWorkFraction = 0.025;
        private double debtIssueIncrement = 0.025;
        private double featureTimingFloor = 0.45;
        private double requirementProgressWeight = 1.0;
        private double requirementComplexityFloor = 0.35;
        private double lateRequirementWeight = 0.2;
        private double dependencyConcurrencyWeight = 0.6;
        private double dependencyInstabilityWeight = 1.0;
        private double dependencyPressureWeight = 0.5;
        private double integrationProgressFloor = 0.2;
        private double integrationDebtWeight = 1.0;
        private double integrationConcurrencyWeight = 0.35;
        private double integrationHiddenDefectWeight = 1.0;
        private double devopsCoverageWeight = 0.3;
        private double securityBaseWeight = 0.35;
        private double securityDebtWeight = 1.0;
        private double securityCutCornersWeight = 0.45;
        private double securityFatigueWeight = 0.25;
        private double securityCrunchWeight = 0.15;
        private double debtIssueFloor = 0.2;
        private double debtIssueWeight = 1.5;
        private double ignoredDebtWeight = 0.2;
        private Map<String, Double> eventWeights = defaultEventWeights();
        public double getBaseProbability() { return baseProbability; }
        public void setBaseProbability(double value) { baseProbability = value; }
        public int getMinimumSpacingWeeks() { return minimumSpacingWeeks; }
        public void setMinimumSpacingWeeks(int value) { minimumSpacingWeeks = value; }
        public int getCooldownWeeks() { return cooldownWeeks; }
        public void setCooldownWeeks(int value) { cooldownWeeks = value; }
        public int getMaximumUnresolvedEvents() { return maximumUnresolvedEvents; }
        public void setMaximumUnresolvedEvents(int value) { maximumUnresolvedEvents = value; }
        public double getFeatureRequestRate() { return featureRequestRate; }
        public void setFeatureRequestRate(double value) { featureRequestRate = value; }
        public double getStakeholderVolatility() { return stakeholderVolatility; }
        public void setStakeholderVolatility(double value) { stakeholderVolatility = value; }
        public double getDependencyRisk() { return dependencyRisk; }
        public void setDependencyRisk(double value) { dependencyRisk = value; }
        public double getTechnicalDebtSensitivity() { return technicalDebtSensitivity; }
        public void setTechnicalDebtSensitivity(double value) { technicalDebtSensitivity = value; }
        public double getProjectComplexity() { return projectComplexity; }
        public void setProjectComplexity(double value) { projectComplexity = value; }
        public double getEventWorkFraction() { return eventWorkFraction; }
        public void setEventWorkFraction(double value) { eventWorkFraction = value; }
        public double getDebtIssueIncrement() { return debtIssueIncrement; }
        public void setDebtIssueIncrement(double value) { debtIssueIncrement = value; }
        public double getFeatureTimingFloor() { return featureTimingFloor; }
        public void setFeatureTimingFloor(double value) { featureTimingFloor = value; }
        public double getRequirementProgressWeight() { return requirementProgressWeight; }
        public void setRequirementProgressWeight(double value) { requirementProgressWeight = value; }
        public double getRequirementComplexityFloor() { return requirementComplexityFloor; }
        public void setRequirementComplexityFloor(double value) { requirementComplexityFloor = value; }
        public double getLateRequirementWeight() { return lateRequirementWeight; }
        public void setLateRequirementWeight(double value) { lateRequirementWeight = value; }
        public double getDependencyConcurrencyWeight() { return dependencyConcurrencyWeight; }
        public void setDependencyConcurrencyWeight(double value) { dependencyConcurrencyWeight = value; }
        public double getDependencyInstabilityWeight() { return dependencyInstabilityWeight; }
        public void setDependencyInstabilityWeight(double value) { dependencyInstabilityWeight = value; }
        public double getDependencyPressureWeight() { return dependencyPressureWeight; }
        public void setDependencyPressureWeight(double value) { dependencyPressureWeight = value; }
        public double getIntegrationProgressFloor() { return integrationProgressFloor; }
        public void setIntegrationProgressFloor(double value) { integrationProgressFloor = value; }
        public double getIntegrationDebtWeight() { return integrationDebtWeight; }
        public void setIntegrationDebtWeight(double value) { integrationDebtWeight = value; }
        public double getIntegrationConcurrencyWeight() { return integrationConcurrencyWeight; }
        public void setIntegrationConcurrencyWeight(double value) { integrationConcurrencyWeight = value; }
        public double getIntegrationHiddenDefectWeight() { return integrationHiddenDefectWeight; }
        public void setIntegrationHiddenDefectWeight(double value) { integrationHiddenDefectWeight = value; }
        public double getDevopsCoverageWeight() { return devopsCoverageWeight; }
        public void setDevopsCoverageWeight(double value) { devopsCoverageWeight = value; }
        public double getSecurityBaseWeight() { return securityBaseWeight; }
        public void setSecurityBaseWeight(double value) { securityBaseWeight = value; }
        public double getSecurityDebtWeight() { return securityDebtWeight; }
        public void setSecurityDebtWeight(double value) { securityDebtWeight = value; }
        public double getSecurityCutCornersWeight() { return securityCutCornersWeight; }
        public void setSecurityCutCornersWeight(double value) { securityCutCornersWeight = value; }
        public double getSecurityFatigueWeight() { return securityFatigueWeight; }
        public void setSecurityFatigueWeight(double value) { securityFatigueWeight = value; }
        public double getSecurityCrunchWeight() { return securityCrunchWeight; }
        public void setSecurityCrunchWeight(double value) { securityCrunchWeight = value; }
        public double getDebtIssueFloor() { return debtIssueFloor; }
        public void setDebtIssueFloor(double value) { debtIssueFloor = value; }
        public double getDebtIssueWeight() { return debtIssueWeight; }
        public void setDebtIssueWeight(double value) { debtIssueWeight = value; }
        public double getIgnoredDebtWeight() { return ignoredDebtWeight; }
        public void setIgnoredDebtWeight(double value) { ignoredDebtWeight = value; }
        public Map<String, Double> getEventWeights() { return eventWeights; }
        public void setEventWeights(Map<String, Double> value) { eventWeights = value; }
        private void validate() {
            validateUnit(baseProbability, "events.baseProbability");
            validateNonNegative(featureRequestRate, "events.featureRequestRate");
            validateNonNegative(stakeholderVolatility, "events.stakeholderVolatility");
            validateNonNegative(dependencyRisk, "events.dependencyRisk");
            validateNonNegative(technicalDebtSensitivity, "events.technicalDebtSensitivity");
            validateNonNegative(projectComplexity, "events.projectComplexity");
            validateUnit(eventWorkFraction, "events.eventWorkFraction");
            validateUnit(debtIssueIncrement, "events.debtIssueIncrement");
            validateUnit(featureTimingFloor, "events.featureTimingFloor");
            validateNonNegative(requirementProgressWeight, "events.requirementProgressWeight");
            validateUnit(requirementComplexityFloor, "events.requirementComplexityFloor");
            validateNonNegative(lateRequirementWeight, "events.lateRequirementWeight");
            validateNonNegative(dependencyConcurrencyWeight, "events.dependencyConcurrencyWeight");
            validateNonNegative(dependencyInstabilityWeight, "events.dependencyInstabilityWeight");
            validateNonNegative(dependencyPressureWeight, "events.dependencyPressureWeight");
            validateUnit(integrationProgressFloor, "events.integrationProgressFloor");
            validateNonNegative(integrationDebtWeight, "events.integrationDebtWeight");
            validateNonNegative(integrationConcurrencyWeight, "events.integrationConcurrencyWeight");
            validateNonNegative(integrationHiddenDefectWeight, "events.integrationHiddenDefectWeight");
            validateNonNegative(devopsCoverageWeight, "events.devopsCoverageWeight");
            validateUnit(securityBaseWeight, "events.securityBaseWeight");
            validateNonNegative(securityDebtWeight, "events.securityDebtWeight");
            validateNonNegative(securityCutCornersWeight, "events.securityCutCornersWeight");
            validateNonNegative(securityFatigueWeight, "events.securityFatigueWeight");
            validateNonNegative(securityCrunchWeight, "events.securityCrunchWeight");
            validateUnit(debtIssueFloor, "events.debtIssueFloor");
            validateNonNegative(debtIssueWeight, "events.debtIssueWeight");
            validateNonNegative(ignoredDebtWeight, "events.ignoredDebtWeight");
            if (minimumSpacingWeeks < 1 || cooldownWeeks < 0
                    || maximumUnresolvedEvents < 1 || maximumUnresolvedEvents > 10) {
                throw new IllegalArgumentException("Event spacing and unresolved limits must be valid");
            }
            if (eventWeights == null || eventWeights.isEmpty()) {
                throw new IllegalArgumentException("At least one project event weight is required");
            }
            for (Map.Entry<String, Double> entry : eventWeights.entrySet()) {
                try {
                    EventType.valueOf(entry.getKey().toUpperCase());
                } catch (IllegalArgumentException | NullPointerException exception) {
                    throw new IllegalArgumentException("Unknown project event type: " + entry.getKey(), exception);
                }
                if (entry.getValue() == null || !Double.isFinite(entry.getValue())
                        || entry.getValue() < 0.0 || entry.getValue() > 10.0) {
                    throw new IllegalArgumentException("Event weights must be finite and between 0 and 10");
                }
            }
        }
        private static Map<String, Double> defaultEventWeights() {
            Map<String, Double> weights = new HashMap<>();
            weights.put("CUSTOMER_FEATURE_REQUEST", 1.0);
            weights.put("REQUIREMENTS_MISUNDERSTANDING", 0.7);
            weights.put("DEPENDENCY_PROBLEM", 0.7);
            weights.put("FAILED_INTEGRATION", 0.6);
            weights.put("SECURITY_VULNERABILITY", 0.45);
            weights.put("TECHNICAL_DEBT_ISSUE", 0.5);
            return weights;
        }
    }

    public static class Scope {
        private Map<String, Double> featureWorkFractions = defaultFeatureWorkFractions();
        private Map<String, Double> rippleFactors = defaultRippleFactors();
        private double downstreamRipple = 0.45;
        private double regressionDemandFactor = 0.25;
        public Map<String, Double> getFeatureWorkFractions() { return featureWorkFractions; }
        public void setFeatureWorkFractions(Map<String, Double> value) { featureWorkFractions = value; }
        public Map<String, Double> getRippleFactors() { return rippleFactors; }
        public void setRippleFactors(Map<String, Double> value) { rippleFactors = value; }
        public double getDownstreamRipple() { return downstreamRipple; }
        public void setDownstreamRipple(double value) { downstreamRipple = value; }
        public double getRegressionDemandFactor() { return regressionDemandFactor; }
        public void setRegressionDemandFactor(double value) { regressionDemandFactor = value; }
        private void validate() {
            validatePhaseMap(featureWorkFractions, "Feature work fractions", false);
            validatePhaseMap(rippleFactors, "Scope ripple factors", false);
            if (featureWorkFractions.values().stream().mapToDouble(Double::doubleValue).sum() <= 0.0) {
                throw new IllegalArgumentException("At least one feature work fraction must be positive");
            }
            validateUnit(downstreamRipple, "scope.downstreamRipple");
            validateUnit(regressionDemandFactor, "scope.regressionDemandFactor");
        }
        private static Map<String, Double> defaultFeatureWorkFractions() {
            Map<String, Double> fractions = new HashMap<>();
            fractions.put("REQUIREMENTS", 0.04);
            fractions.put("DESIGN", 0.05);
            fractions.put("DEVELOPMENT", 0.04);
            fractions.put("TESTING", 0.04);
            fractions.put("DEPLOYMENT", 0.02);
            return fractions;
        }
        private static Map<String, Double> defaultRippleFactors() {
            Map<String, Double> factors = new HashMap<>();
            factors.put("REQUIREMENTS", 0.25);
            factors.put("DESIGN", 0.22);
            factors.put("DEVELOPMENT", 0.16);
            factors.put("TESTING", 0.10);
            factors.put("DEPLOYMENT", 0.05);
            return factors;
        }
    }

    public static class TechnicalDebt {
        private double baseAccumulation = 0.001;
        private double pressureAccumulation = 0.004;
        private double crunchAccumulation = 0.004;
        private double concurrencyAccumulation = 0.008;
        private double productivityMaxPenalty = 0.22;
        private double defectMaxIncrease = 0.75;
        private double reworkDifficultyMaxIncrease = 0.35;
        private double payDownCapacityFraction = 0.15;
        private double payDownEffectiveness = 0.55;
        public double getBaseAccumulation() { return baseAccumulation; }
        public void setBaseAccumulation(double value) { baseAccumulation = value; }
        public double getPressureAccumulation() { return pressureAccumulation; }
        public void setPressureAccumulation(double value) { pressureAccumulation = value; }
        public double getCrunchAccumulation() { return crunchAccumulation; }
        public void setCrunchAccumulation(double value) { crunchAccumulation = value; }
        public double getConcurrencyAccumulation() { return concurrencyAccumulation; }
        public void setConcurrencyAccumulation(double value) { concurrencyAccumulation = value; }
        public double getProductivityMaxPenalty() { return productivityMaxPenalty; }
        public void setProductivityMaxPenalty(double value) { productivityMaxPenalty = value; }
        public double getDefectMaxIncrease() { return defectMaxIncrease; }
        public void setDefectMaxIncrease(double value) { defectMaxIncrease = value; }
        public double getReworkDifficultyMaxIncrease() { return reworkDifficultyMaxIncrease; }
        public void setReworkDifficultyMaxIncrease(double value) { reworkDifficultyMaxIncrease = value; }
        public double getPayDownCapacityFraction() { return payDownCapacityFraction; }
        public void setPayDownCapacityFraction(double value) { payDownCapacityFraction = value; }
        public double getPayDownEffectiveness() { return payDownEffectiveness; }
        public void setPayDownEffectiveness(double value) { payDownEffectiveness = value; }
        private void validate() {
            validateUnit(productivityMaxPenalty, "technicalDebt.productivityMaxPenalty");
            validateUnit(defectMaxIncrease, "technicalDebt.defectMaxIncrease");
            validateUnit(reworkDifficultyMaxIncrease, "technicalDebt.reworkDifficultyMaxIncrease");
            validateUnit(payDownCapacityFraction, "technicalDebt.payDownCapacityFraction");
            validateUnit(payDownEffectiveness, "technicalDebt.payDownEffectiveness");
            validateNonNegative(baseAccumulation, "technicalDebt.baseAccumulation");
            validateNonNegative(pressureAccumulation, "technicalDebt.pressureAccumulation");
            validateNonNegative(crunchAccumulation, "technicalDebt.crunchAccumulation");
            validateNonNegative(concurrencyAccumulation, "technicalDebt.concurrencyAccumulation");
            if (payDownCapacityFraction <= 0.0) {
                throw new IllegalArgumentException("Debt pay-down capacity fraction must be positive");
            }
        }
    }

    public static class EngineeringApproachSettings {
        private double carefulThroughput = 0.96;
        private double balancedThroughput = 1.0;
        private double cutCornersThroughput = 1.08;
        private double carefulDefectMultiplier = 0.88;
        private double balancedDefectMultiplier = 1.0;
        private double cutCornersDefectMultiplier = 1.18;
        private double carefulDebtRate;
        private double balancedDebtRate = 0.001;
        private double cutCornersDebtRate = 0.012;
        public double getCarefulThroughput() { return carefulThroughput; }
        public void setCarefulThroughput(double value) { carefulThroughput = value; }
        public double getBalancedThroughput() { return balancedThroughput; }
        public void setBalancedThroughput(double value) { balancedThroughput = value; }
        public double getCutCornersThroughput() { return cutCornersThroughput; }
        public void setCutCornersThroughput(double value) { cutCornersThroughput = value; }
        public double getCarefulDefectMultiplier() { return carefulDefectMultiplier; }
        public void setCarefulDefectMultiplier(double value) { carefulDefectMultiplier = value; }
        public double getBalancedDefectMultiplier() { return balancedDefectMultiplier; }
        public void setBalancedDefectMultiplier(double value) { balancedDefectMultiplier = value; }
        public double getCutCornersDefectMultiplier() { return cutCornersDefectMultiplier; }
        public void setCutCornersDefectMultiplier(double value) { cutCornersDefectMultiplier = value; }
        public double getCarefulDebtRate() { return carefulDebtRate; }
        public void setCarefulDebtRate(double value) { carefulDebtRate = value; }
        public double getBalancedDebtRate() { return balancedDebtRate; }
        public void setBalancedDebtRate(double value) { balancedDebtRate = value; }
        public double getCutCornersDebtRate() { return cutCornersDebtRate; }
        public void setCutCornersDebtRate(double value) { cutCornersDebtRate = value; }
        public double throughput(edu.simulator.model.EngineeringApproach value) {
            return switch (value) {
                case CAREFUL -> carefulThroughput;
                case BALANCED -> balancedThroughput;
                case CUT_CORNERS -> cutCornersThroughput;
            };
        }
        public double defectMultiplier(edu.simulator.model.EngineeringApproach value) {
            return switch (value) {
                case CAREFUL -> carefulDefectMultiplier;
                case BALANCED -> balancedDefectMultiplier;
                case CUT_CORNERS -> cutCornersDefectMultiplier;
            };
        }
        public double debtRate(edu.simulator.model.EngineeringApproach value) {
            return switch (value) {
                case CAREFUL -> carefulDebtRate;
                case BALANCED -> balancedDebtRate;
                case CUT_CORNERS -> cutCornersDebtRate;
            };
        }
        private void validate() {
            validatePositive(carefulThroughput, "engineeringApproach.carefulThroughput");
            validatePositive(balancedThroughput, "engineeringApproach.balancedThroughput");
            validatePositive(cutCornersThroughput, "engineeringApproach.cutCornersThroughput");
            validatePositive(carefulDefectMultiplier, "engineeringApproach.carefulDefectMultiplier");
            validatePositive(balancedDefectMultiplier, "engineeringApproach.balancedDefectMultiplier");
            validatePositive(cutCornersDefectMultiplier, "engineeringApproach.cutCornersDefectMultiplier");
            validateNonNegative(carefulDebtRate, "engineeringApproach.carefulDebtRate");
            validateNonNegative(balancedDebtRate, "engineeringApproach.balancedDebtRate");
            validateNonNegative(cutCornersDebtRate, "engineeringApproach.cutCornersDebtRate");
            if (carefulThroughput > balancedThroughput
                    || cutCornersThroughput < balancedThroughput
                    || carefulDefectMultiplier > balancedDefectMultiplier
                    || cutCornersDefectMultiplier < balancedDefectMultiplier
                    || carefulDebtRate > balancedDebtRate
                    || cutCornersDebtRate < balancedDebtRate) {
                throw new IllegalArgumentException("Engineering approach effects must reflect their policy names");
            }
        }
    }

    public static class Concurrency {
        private double sequentialReadiness = 0.04;
        private double moderateReadiness = 0.10;
        private double aggressiveReadiness = 0.30;
        private double sequentialCapacity = 0.96;
        private double moderateCapacity = 1.0;
        private double aggressiveCapacity = 1.04;
        private double aggressiveUncertainty = 0.75;
        private double moderateUncertainty = 0.35;
        private double sequentialUncertainty = 0.10;
        private double coordinationReduction = 0.35;
        private double dependencyDefectSensitivity = 0.8;
        private double coordinationPenaltyScale = 0.45;
        public double getSequentialReadiness() { return sequentialReadiness; }
        public void setSequentialReadiness(double value) { sequentialReadiness = value; }
        public double getModerateReadiness() { return moderateReadiness; }
        public void setModerateReadiness(double value) { moderateReadiness = value; }
        public double getAggressiveReadiness() { return aggressiveReadiness; }
        public void setAggressiveReadiness(double value) { aggressiveReadiness = value; }
        public double getSequentialCapacity() { return sequentialCapacity; }
        public void setSequentialCapacity(double value) { sequentialCapacity = value; }
        public double getModerateCapacity() { return moderateCapacity; }
        public void setModerateCapacity(double value) { moderateCapacity = value; }
        public double getAggressiveCapacity() { return aggressiveCapacity; }
        public void setAggressiveCapacity(double value) { aggressiveCapacity = value; }
        public double getAggressiveUncertainty() { return aggressiveUncertainty; }
        public void setAggressiveUncertainty(double value) { aggressiveUncertainty = value; }
        public double getModerateUncertainty() { return moderateUncertainty; }
        public void setModerateUncertainty(double value) { moderateUncertainty = value; }
        public double getSequentialUncertainty() { return sequentialUncertainty; }
        public void setSequentialUncertainty(double value) { sequentialUncertainty = value; }
        public double getCoordinationReduction() { return coordinationReduction; }
        public void setCoordinationReduction(double value) { coordinationReduction = value; }
        public double getDependencyDefectSensitivity() { return dependencyDefectSensitivity; }
        public void setDependencyDefectSensitivity(double value) { dependencyDefectSensitivity = value; }
        public double getCoordinationPenaltyScale() { return coordinationPenaltyScale; }
        public void setCoordinationPenaltyScale(double value) { coordinationPenaltyScale = value; }
        public double readiness(edu.simulator.model.ConcurrencyPolicy value) {
            return switch (value) {
                case SEQUENTIAL -> sequentialReadiness;
                case MODERATE -> moderateReadiness;
                case AGGRESSIVE -> aggressiveReadiness;
            };
        }
        public double capacity(edu.simulator.model.ConcurrencyPolicy value) {
            return switch (value) {
                case SEQUENTIAL -> sequentialCapacity;
                case MODERATE -> moderateCapacity;
                case AGGRESSIVE -> aggressiveCapacity;
            };
        }
        public double uncertainty(edu.simulator.model.ConcurrencyPolicy value) {
            return switch (value) {
                case SEQUENTIAL -> sequentialUncertainty;
                case MODERATE -> moderateUncertainty;
                case AGGRESSIVE -> aggressiveUncertainty;
            };
        }
        private void validate() {
            validateUnit(sequentialReadiness, "concurrency.sequentialReadiness");
            validateUnit(moderateReadiness, "concurrency.moderateReadiness");
            validateUnit(aggressiveReadiness, "concurrency.aggressiveReadiness");
            validatePositive(sequentialCapacity, "concurrency.sequentialCapacity");
            validatePositive(moderateCapacity, "concurrency.moderateCapacity");
            validatePositive(aggressiveCapacity, "concurrency.aggressiveCapacity");
            validateUnit(aggressiveUncertainty, "concurrency.aggressiveUncertainty");
            validateUnit(moderateUncertainty, "concurrency.moderateUncertainty");
            validateUnit(sequentialUncertainty, "concurrency.sequentialUncertainty");
            validateUnit(coordinationReduction, "concurrency.coordinationReduction");
            validatePositive(dependencyDefectSensitivity, "concurrency.dependencyDefectSensitivity");
            validatePositive(coordinationPenaltyScale, "concurrency.coordinationPenaltyScale");
            if (sequentialReadiness > moderateReadiness
                    || moderateReadiness > aggressiveReadiness
                    || sequentialCapacity > moderateCapacity
                    || moderateCapacity > aggressiveCapacity) {
                throw new IllegalArgumentException("Concurrency benefits must increase by policy level");
            }
        }
    }

    private static void validatePhaseMap(Map<String, Double> values, String label, boolean positive) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException(label + " cannot be empty");
        }
        for (Map.Entry<String, Double> entry : values.entrySet()) {
            try {
                ProjectPhase.valueOf(entry.getKey().toUpperCase());
            } catch (IllegalArgumentException | NullPointerException exception) {
                throw new IllegalArgumentException(label + " contains unknown phase " + entry.getKey(), exception);
            }
            if (entry.getValue() == null || !Double.isFinite(entry.getValue())
                    || entry.getValue() < 0.0 || entry.getValue() > 1.0
                    || (positive && entry.getValue() <= 0.0)) {
                throw new IllegalArgumentException(label + " must contain valid values between 0 and 1");
            }
        }
    }

    private static void validateUnit(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException(name + " must be between 0 and 1");
        }
    }

    private static void validatePositive(double value, String name) {
        if (!Double.isFinite(value) || value <= 0.0 || value > 5.0) {
            throw new IllegalArgumentException(name + " must be finite, positive, and at most 5");
        }
    }

    private static void validateNonNegative(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0 || value > 5.0) {
            throw new IllegalArgumentException(name + " must be between 0 and 5");
        }
    }
}
