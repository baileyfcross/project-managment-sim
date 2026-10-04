package edu.simulator.simulation;

import edu.simulator.configuration.SimulationConfiguration;
import edu.simulator.model.ProjectPhase;
import edu.simulator.model.TestingPriority;
import edu.simulator.model.WorkIntensity;
import edu.simulator.model.WorkState;

import java.util.Random;

public class DefectModel {
    public double defectProbability(double baseRate, double fatigue, double schedulePressure,
                                  double coordinationPenalty, double onboardingFactor,
                                  SimulationConfiguration config) {
        double fatigueImpact = new FatigueModel().defectModifier(fatigue, config);
        double pressureImpact = 1.0 + (schedulePressure * 1.4);
        double coordinationImpact = 1.0 + (coordinationPenalty * 2.5);
        double onboardingImpact = 1.0 + Math.max(0.0, onboardingFactor * 0.7);
        double probability = baseRate * fatigueImpact * pressureImpact * coordinationImpact * onboardingImpact;
        return clamp(probability, config.getQuality().getDefectCap());
    }

    public double defectProbability(ProjectPhase phase, double experienceModifier,
                                    double onboardingDeficit, double mentoringCoverage, double fatigue,
                                    double coordinationPenalty, double schedulePressure,
                                    WorkIntensity intensity, WorkState work,
                                    SimulationConfiguration config) {
        return defectProbability(phase, experienceModifier, onboardingDeficit, mentoringCoverage,
                fatigue, coordinationPenalty, schedulePressure, intensity, work,
                new TechnicalDebtModel().defectModifier(0.0, config.getPhaseFive()),
                1.0, 0.0, config);
    }

    public double defectProbability(ProjectPhase phase, double experienceModifier,
                                    double onboardingDeficit, double mentoringCoverage, double fatigue,
                                    double coordinationPenalty, double schedulePressure,
                                    WorkIntensity intensity, WorkState work,
                                    double technicalDebtModifier, double engineeringModifier,
                                    double dependencyUncertainty,
                                    SimulationConfiguration config) {
        double propagation = 0.0;
        for (ProjectPhase upstream : ProjectPhase.values()) {
            if (upstream == phase) {
                break;
            }
            double scope = work.getTotalWork(upstream);
            if (scope > 0.0) {
                propagation += work.getUnknownRework(upstream) / scope;
            }
        }
        double intensityModifier = switch (intensity) {
            case SUSTAINABLE -> 1.0;
            case INCREASED -> config.getWorkIntensity().getIncreasedDefectMultiplier();
            case CRUNCH -> config.getWorkIntensity().getCrunchDefectMultiplier();
        };
        double mentoringModifier = 1.0
                + (1.0 - Math.max(0.0, Math.min(1.0, mentoringCoverage))) * 0.1;
        double probability = config.getQuality().getBaseDefectRate()
                * Math.max(0.0, experienceModifier)
                * (1.0 + Math.max(0.0, onboardingDeficit) * 0.7)
                * new FatigueModel().defectModifier(fatigue, config)
                * (1.0 + Math.max(0.0, coordinationPenalty) * 2.5)
                * (1.0 + Math.max(0.0, schedulePressure) * 1.4)
                * intensityModifier * mentoringModifier
                * Math.max(0.0, technicalDebtModifier)
                * Math.max(0.0, engineeringModifier)
                * new ConcurrencyModel().defectModifier(dependencyUncertainty,
                        config.getPhaseFive())
                * (1.0 + propagation * config.getQuality().getPropagationFactor());
        return clamp(probability, config.getQuality().getDefectCap());
    }

    public double sampleDefectiveWork(double attempted, double probability, Random random) {
        double work = Double.isFinite(attempted) ? Math.max(0.0, attempted) : 0.0;
        double p = Double.isFinite(probability) ? Math.max(0.0, Math.min(1.0, probability)) : 0.0;
        if (work == 0.0 || p == 0.0) {
            return 0.0;
        }
        if (p == 1.0) {
            return work;
        }
        double mean = work * p;
        double standardDeviation = Math.sqrt(work * p * (1.0 - p));
        return Math.max(0.0, Math.min(work,
                Math.rint(mean + random.nextGaussian() * standardDeviation)));
    }

    public double discoveryProbability(double qaCapacity, TestingPriority priority,
                                      double fatigue, SimulationConfiguration config) {
        double normalizedCapacity = Math.max(0.0, Double.isFinite(qaCapacity) ? qaCapacity : 0.0);
        return clamp(config.getQa().getBaseDetectionRate()
                * config.getQa().getCapacityMultiplier()
                * config.getTesting().discoveryFactor(priority)
                * new FatigueModel().qaEffectivenessModifier(fatigue, config)
                * Math.min(1.0, normalizedCapacity), 1.0);
    }

    private double clamp(double value, double maximum) {
        if (!Double.isFinite(value)) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(maximum, value));
    }
}
