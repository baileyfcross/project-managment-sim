package edu.simulator.simulation;

import edu.simulator.configuration.SimulationConfiguration;
import edu.simulator.model.Employee;
import edu.simulator.model.WorkIntensity;

public class FatigueModel {
    public double updateFatigue(Employee employee, WorkIntensity intensity, double schedulePressure,
                                SimulationConfiguration config) {
        return update(employee.getFatigue(), intensity, schedulePressure,
                employee.getConsecutiveOvertimeWeeks(), config);
    }

    public double updateFatigue(double currentFatigue, WorkIntensity intensity,
                                SimulationConfiguration config) {
        return update(currentFatigue, intensity, 0.0, 0, config);
    }

    private double update(double current, WorkIntensity intensity, double schedulePressure,
                          int overtimeStreak, SimulationConfiguration config) {
        var settings = config.getFatigue();
        double pressure = clamp(schedulePressure);
        if (intensity == WorkIntensity.SUSTAINABLE) {
            double recovery = settings.getRecoveryRate()
                    * Math.max(0.25, 1.0 - pressure * 0.5);
            return clamp(current - recovery, settings.getMaxValue());
        }
        double baseIncrease = intensity == WorkIntensity.CRUNCH
                ? settings.getCrunchRate() : settings.getIncreasedWorkRate();
        double streakIncrease = overtimeStreak
                * settings.getOvertimeStreakAccumulationRate();
        double pressureIncrease = pressure * settings.getPressureAccumulationRate();
        double nonlinearIncrease = 1.0 + current * settings.getFatigueAccumulationCurve();
        return clamp(current + (baseIncrease + streakIncrease + pressureIncrease) * nonlinearIncrease,
                settings.getMaxValue());
    }

    public double productivityModifier(double fatigue, SimulationConfiguration config) {
        double normalized = clamp(fatigue);
        return Math.max(0.0, 1.0 - config.getFatigue().getProductivityMaxPenalty()
                * normalized * normalized);
    }

    public double defectModifier(double fatigue, SimulationConfiguration config) {
        double normalized = clamp(fatigue);
        return 1.0 + config.getFatigue().getDefectMaxIncrease() * normalized * normalized;
    }

    public double qaEffectivenessModifier(double fatigue, SimulationConfiguration config) {
        double normalized = clamp(fatigue);
        return Math.max(0.0, 1.0 - config.getFatigue().getQaEffectivenessMaxPenalty()
                * normalized * normalized);
    }

    public double mentoringModifier(double fatigue, SimulationConfiguration config) {
        double normalized = clamp(fatigue);
        return Math.max(0.0, 1.0 - config.getFatigue().getMentoringMaxPenalty()
                * normalized * normalized);
    }

    public double coordinationModifier(double fatigue, SimulationConfiguration config) {
        double normalized = clamp(fatigue);
        return Math.max(0.0, 1.0 - config.getFatigue().getCoordinationMaxPenalty()
                * normalized * normalized);
    }

    public String category(double fatigue) {
        double value = clamp(fatigue);
        if (value <= 0.20) return "Normal";
        if (value <= 0.40) return "Tired";
        if (value <= 0.60) return "Fatigued";
        if (value <= 0.80) return "Burnout Risk";
        return "Severe Burnout";
    }

    private double clamp(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, value));
    }

    private double clamp(double value, double maximum) {
        return Math.max(0.0, Math.min(maximum, Double.isFinite(value) ? value : 0.0));
    }
}
