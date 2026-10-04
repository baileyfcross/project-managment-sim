package edu.simulator.simulation;

import edu.simulator.configuration.SimulationConfiguration;

public class WorkAllocationModel {
    public Allocation allocate(double developerCapacity, double knownRework,
                               SimulationConfiguration configuration) {
        double capacity = Math.max(0.0, finite(developerCapacity));
        double reworkShare = knownRework > 0.0
                ? configuration.getWorkAllocation().getKnownReworkCapacityShare()
                : 0.0;
        return new Allocation(capacity * reworkShare, capacity * (1.0 - reworkShare));
    }

    private double finite(double value) {
        return Double.isFinite(value) ? value : 0.0;
    }

    public record Allocation(double reworkCapacity, double newWorkCapacity) {
    }
}
