package edu.simulator.simulation;

import edu.simulator.model.Project;
import edu.simulator.model.ProjectPhase;

public class ForecastModel {
    public int estimateCompletionWeek(Project project, double weeklyWorkCapacity) {
        double remaining = project.getWorkState().perceivedRemainingWork();
        if (remaining <= 0.0) {
            return project.getCurrentWeek();
        }
        if (!Double.isFinite(weeklyWorkCapacity) || weeklyWorkCapacity <= 0.0) {
            return -1;
        }
        int projectedWeeks = (int) Math.ceil(remaining / weeklyWorkCapacity);
        return project.getCurrentWeek() + Math.max(1, projectedWeeks);
    }

    public int estimateCompletionWeek(Project project, double developerCapacity,
                                      double qaCapacity, double devopsCapacity,
                                      double testingBacklog) {
        var work = project.getWorkState();
        double developerWork = work.getKnownRework(ProjectPhase.REQUIREMENTS)
                + work.getKnownRework(ProjectPhase.DESIGN)
                + work.getKnownRework(ProjectPhase.DEVELOPMENT)
                + work.getKnownRework(ProjectPhase.TESTING)
                + work.getKnownRework(ProjectPhase.DEPLOYMENT)
                + work.getBaseWorkRemaining(ProjectPhase.REQUIREMENTS)
                + work.getBaseWorkRemaining(ProjectPhase.DESIGN)
                + work.getBaseWorkRemaining(ProjectPhase.DEVELOPMENT);
        double regularTestingBacklog = work.getTestingBacklog(ProjectPhase.DEVELOPMENT);
        double regressionTestingBacklog = Math.max(0.0, testingBacklog - regularTestingBacklog);
        double testingWork = work.getBaseWorkRemaining(ProjectPhase.TESTING)
                + regressionTestingBacklog;
        double deploymentWork = work.getBaseWorkRemaining(ProjectPhase.DEPLOYMENT);
        if (developerWork + testingWork + deploymentWork <= 0.0) {
            return project.getCurrentWeek();
        }
        double devWeeks = weeks(developerWork, developerCapacity * 180.0);
        double qaWeeks = weeks(testingWork, qaCapacity * 180.0);
        double devopsWeeks = weeks(deploymentWork, devopsCapacity * 180.0);
        double estimate = Math.max(devWeeks, Math.max(qaWeeks, devopsWeeks));
        if (!Double.isFinite(estimate)) {
            return -1;
        }
        return project.getCurrentWeek() + Math.max(1, (int) Math.ceil(estimate));
    }

    private double weeks(double work, double capacity) {
        if (work <= 0.0) {
            return 0.0;
        }
        if (!Double.isFinite(capacity) || capacity <= 0.0) {
            return Double.POSITIVE_INFINITY;
        }
        return work / capacity;
    }
}
