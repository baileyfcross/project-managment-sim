package edu.simulator.simulation;

import edu.simulator.configuration.SimulationConfiguration;
import edu.simulator.model.Employee;
import edu.simulator.model.WorkIntensity;

public class MoraleModel {
    public double updateMorale(Employee employee, WorkIntensity intensity,
                               double schedulePressure, SimulationConfiguration configuration) {
        var settings = configuration.getMorale();
        double overtime = switch (intensity) {
            case SUSTAINABLE -> 0.0;
            case INCREASED -> 0.5;
            case CRUNCH -> 1.0;
        };
        double recovery = intensity == WorkIntensity.SUSTAINABLE && schedulePressure < 0.4
                ? settings.getRecoveryRate() : 0.0;
        double stress = employee.getFatigue() * settings.getFatigueWeight()
                + overtime * settings.getOvertimeWeight()
                + clamp(schedulePressure) * settings.getPressureWeight()
                + employee.getConsecutiveOvertimeWeeks() * settings.getOvertimeStreakWeight();
        return clamp(employee.getMorale() + recovery - stress);
    }

    public String healthLabel(double averageMorale) {
        if (averageMorale >= 0.75) return "Good";
        if (averageMorale >= 0.55) return "Stable";
        if (averageMorale >= 0.35) return "Strained";
        if (averageMorale >= 0.15) return "Poor";
        return "Critical";
    }

    private double clamp(double value) {
        return Double.isFinite(value) ? Math.max(0.0, Math.min(1.0, value)) : 0.0;
    }
}
