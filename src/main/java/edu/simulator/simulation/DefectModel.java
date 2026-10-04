package edu.simulator.simulation;

import edu.simulator.configuration.SimulationConfiguration;

public class DefectModel {
    public double defectProbability(double baseRate, double fatigue, double schedulePressure,
                                  double coordinationPenalty, double onboardingFactor,
                                  SimulationConfiguration config) {
        double fatigueImpact = 1.0 + (fatigue * 1.8);
        double pressureImpact = 1.0 + (schedulePressure * 1.4);
        double coordinationImpact = 1.0 + (coordinationPenalty * 2.5);
        double onboardingImpact = 1.0 + Math.max(0.0, onboardingFactor * 0.7);
        double probability = baseRate * fatigueImpact * pressureImpact * coordinationImpact * onboardingImpact;
        return Math.max(0.0, Math.min(config.getQuality().getDefectCap(), probability));
    }
}
