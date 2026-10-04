package edu.simulator.simulation;

import edu.simulator.configuration.SimulationConfiguration;
import edu.simulator.model.Project;

public class SchedulePressureModel {
    public double calculate(Project project, double remainingWork, double intendedCapacity, double knownRework,
                            SimulationConfiguration config) {
        double weeksRemaining = Math.max(1.0, project.getDeadlineWeeks() - project.getCurrentWeek());
        double workRatio = remainingWork / Math.max(1.0, intendedCapacity * weeksRemaining);
        double reworkFactor = knownRework / Math.max(1.0, remainingWork + knownRework);
        double pressure = (workRatio * config.getSchedule().getPressureWeight()) +
                (reworkFactor * config.getSchedule().getDeadlineUrgencyWeight());
        return clamp(pressure * 2.0);
    }

    private double clamp(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, value));
    }
}
