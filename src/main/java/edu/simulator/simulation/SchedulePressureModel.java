package edu.simulator.simulation;

import edu.simulator.configuration.SimulationConfiguration;
import edu.simulator.model.Project;
import edu.simulator.model.ProjectPhase;

public class SchedulePressureModel {
    public double calculate(Project project, double remainingWork, double intendedCapacity, double knownRework,
                            SimulationConfiguration config) {
        return calculate(project, remainingWork, intendedCapacity, knownRework, 0.0, config);
    }

    public double calculate(Project project, double remainingWork, double intendedCapacity, double knownRework,
                            double testingBacklog, SimulationConfiguration config) {
        double weeksRemaining = Math.max(1.0, project.getDeadlineWeeks() - project.getCurrentWeek());
        double workRatio = remainingWork / Math.max(1.0, intendedCapacity * weeksRemaining);
        double testingRatio = Math.max(0.0, testingBacklog)
                / Math.max(1.0, intendedCapacity * weeksRemaining);
        double reworkFactor = knownRework / Math.max(1.0, remainingWork + knownRework);
        double pressure = ((workRatio + testingRatio) * config.getSchedule().getPressureWeight()) +
                (reworkFactor * config.getSchedule().getDeadlineUrgencyWeight());
        return clamp(pressure * 2.0);
    }

    public double calculate(Project project, double developerCapacity, double qaCapacity,
                            double devopsCapacity, double knownRework, double testingBacklog,
                            SimulationConfiguration config) {
        var work = project.getWorkState();
        double developerWork = work.getBaseWorkRemaining(ProjectPhase.REQUIREMENTS)
                + work.getBaseWorkRemaining(ProjectPhase.DESIGN)
                + work.getBaseWorkRemaining(ProjectPhase.DEVELOPMENT)
                + knownRework;
        double qaWork = work.getBaseWorkRemaining(ProjectPhase.TESTING)
                + Math.max(0.0, testingBacklog);
        double devopsWork = work.getBaseWorkRemaining(ProjectPhase.DEPLOYMENT);
        double weeksRemaining = Math.max(1.0,
                project.getDeadlineWeeks() - project.getCurrentWeek());
        double developerRatio = requiredCapacityRatio(
                developerWork, developerCapacity * ProductivityModel.WORK_UNITS_PER_CAPACITY,
                weeksRemaining);
        double qaRatio = requiredCapacityRatio(qaWork,
                qaCapacity * config.getQa().getThroughputPerCapacity(), weeksRemaining);
        double devopsRatio = requiredCapacityRatio(
                devopsWork, devopsCapacity * ProductivityModel.WORK_UNITS_PER_CAPACITY,
                weeksRemaining);
        double workRatio = Math.max(developerRatio, Math.max(qaRatio, devopsRatio));
        double reworkFactor = knownRework / Math.max(1.0,
                work.perceivedRemainingWork() + knownRework);
        double pressure = (workRatio * config.getSchedule().getPressureWeight())
                + (reworkFactor * config.getSchedule().getDeadlineUrgencyWeight());
        return clamp(pressure * 2.0);
    }

    private double requiredCapacityRatio(double work, double weeklyCapacity, double weeksRemaining) {
        if (work <= 0.0) {
            return 0.0;
        }
        return work / Math.max(1.0, weeklyCapacity * weeksRemaining);
    }

    public String category(double pressure) {
        double normalized = Double.isFinite(pressure) ? Math.max(0.0, Math.min(1.0, pressure)) : 0.0;
        if (normalized < 0.15) return "Low";
        if (normalized < 0.35) return "Manageable";
        if (normalized < 0.60) return "High";
        if (normalized < 0.80) return "Severe";
        return "Critical";
    }

    private double clamp(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, value));
    }
}
