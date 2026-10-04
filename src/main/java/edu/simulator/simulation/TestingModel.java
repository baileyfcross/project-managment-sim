package edu.simulator.simulation;

import edu.simulator.configuration.SimulationConfiguration;
import edu.simulator.model.ProjectPhase;
import edu.simulator.model.TestingPriority;
import edu.simulator.model.WorkState;

import java.util.Random;

public class TestingModel {
    public String backlogStatus(double backlog, SimulationConfiguration configuration) {
        if (backlog < configuration.getTesting().getModerateBacklogThreshold()) {
            return "Healthy";
        }
        if (backlog < configuration.getTesting().getHighBacklogThreshold()) {
            return "Moderate Backlog";
        }
        if (backlog < configuration.getTesting().getCriticalBacklogThreshold()) {
            return "High Backlog";
        }
        return "Critical Backlog";
    }

    public TestingResult perform(WorkState work, double qaCapacity, double fatigue,
                                 TestingPriority priority,
                                 SimulationConfiguration configuration, Random random) {
        double priorityEffort = configuration.getTesting().capacityFactor(priority);
        double inspectionCapacity = Math.max(0.0, finite(qaCapacity))
                * configuration.getQa().getThroughputPerCapacity() * priorityEffort;
        double inspected = 0.0;
        double developmentInspected = 0.0;
        for (ProjectPhase phase : ProjectPhase.values()) {
            double amount = work.inspectTestableWork(phase, inspectionCapacity - inspected);
            inspected += amount;
            if (phase == ProjectPhase.DEVELOPMENT) {
                developmentInspected += amount;
            }
            if (inspected >= inspectionCapacity) {
                break;
            }
        }

        double probability = new DefectModel().discoveryProbability(
                qaCapacity * priorityEffort, priority, fatigue, configuration);
        double expectedDiscovery = Math.min(work.totalUnknownRework(), inspected * probability);
        double discoveries = sample(expectedDiscovery, random);
        double remaining = discoveries;
        for (ProjectPhase phase : ProjectPhase.values()) {
            if (remaining <= 0.0) {
                break;
            }
            remaining -= work.discoverDefects(phase, remaining);
        }
        double backlog = work.getTotalTestingBacklog();
        if (developmentInspected > 0.0) {
            work.recordNewWork(ProjectPhase.TESTING,
                    Math.min(developmentInspected,
                            work.getBaseWorkRemaining(ProjectPhase.TESTING)), 0.0);
        }
        String status = backlogStatus(backlog, configuration);
        return new TestingResult(inspectionCapacity, inspected, discoveries - remaining,
                backlog, status);
    }

    private double sample(double expected, Random random) {
        if (expected <= 0.0) {
            return 0.0;
        }
        double deviation = Math.sqrt(expected * 0.7);
        return Math.max(0.0, Math.rint(expected + random.nextGaussian() * deviation));
    }

    private double finite(double value) {
        return Double.isFinite(value) ? value : 0.0;
    }

    public record TestingResult(double qaCapacity, double workInspected,
                                double defectsDiscovered, double backlog,
                                String backlogStatus) {
    }
}
