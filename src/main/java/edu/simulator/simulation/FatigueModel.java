package edu.simulator.simulation;

import edu.simulator.configuration.SimulationConfiguration;

public class FatigueModel {
    public double updateFatigue(double currentFatigue, WorkIntensity intensity, SimulationConfiguration config) {
        double recovery = config.getFatigue().getRecoveryRate();
        double increase = switch (intensity) {
            case SUSTAINABLE -> -recovery;
            case INCREASED -> config.getFatigue().getIncreasedWorkRate();
            case CRUNCH -> config.getFatigue().getCrunchRate();
        };
        double next = currentFatigue + increase;
        if (intensity == WorkIntensity.SUSTAINABLE) {
            next = Math.max(0.0, next);
        }
        return clamp(next);
    }

    private double clamp(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, value));
    }
}
