package edu.simulator.simulation;

import edu.simulator.model.Project;

public class ForecastModel {
    public int estimateCompletionWeek(Project project, double weeklyWorkCapacity) {
        double remaining = project.getWorkState().totalRemainingWork();
        if (remaining <= 0.0) {
            return project.getCurrentWeek();
        }
        if (!Double.isFinite(weeklyWorkCapacity) || weeklyWorkCapacity <= 0.0) {
            return -1;
        }
        int projectedWeeks = (int) Math.ceil(remaining / weeklyWorkCapacity);
        return project.getCurrentWeek() + Math.max(1, projectedWeeks);
    }
}
