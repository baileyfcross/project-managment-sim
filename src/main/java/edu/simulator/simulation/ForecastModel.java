package edu.simulator.simulation;

import edu.simulator.model.Project;

public class ForecastModel {
    public int estimateCompletionWeek(Project project, double averageProductivity, double knownRework) {
        double remaining = project.getWorkState().totalRemainingWork();
        if (remaining <= 0.0) {
            return project.getCurrentWeek();
        }
        double effort = Math.max(1.0, averageProductivity);
        int projectedWeeks = (int) Math.ceil((remaining + knownRework) / effort);
        return project.getCurrentWeek() + Math.max(1, projectedWeeks);
    }
}
