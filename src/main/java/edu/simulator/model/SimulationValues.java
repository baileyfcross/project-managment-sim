package edu.simulator.model;

public final class SimulationValues {
    private SimulationValues() {
    }

    public static double finiteOrZero(double value) {
        return Double.isFinite(value) ? value : 0.0;
    }

    public static double nonNegativeFinite(double value) {
        return Math.max(0.0, finiteOrZero(value));
    }

    public static double unitInterval(double value) {
        return Math.max(0.0, Math.min(1.0, finiteOrZero(value)));
    }
}
