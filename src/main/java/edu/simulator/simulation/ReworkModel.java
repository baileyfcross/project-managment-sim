package edu.simulator.simulation;

import edu.simulator.configuration.SimulationConfiguration;
import edu.simulator.model.ProjectPhase;
import edu.simulator.model.WorkState;

import java.util.Random;

public class ReworkModel {
    public ReworkResult perform(WorkState work, double capacity, double defectProbability,
                                SimulationConfiguration configuration, Random random) {
        double remainingCapacity = Math.max(0.0, finite(capacity));
        double attemptedTotal = 0.0;
        double fixedTotal = 0.0;
        double defectiveTotal = 0.0;
        for (ProjectPhase phase : ProjectPhase.values()) {
            double attempted = Math.min(work.getKnownRework(phase), remainingCapacity);
            if (attempted <= 0.0) {
                continue;
            }
            double phaseProbability = Math.min(1.0, Math.max(0.0,
                    defectProbability * configuration.getQuality().getReworkDefectMultiplier()
                            * configuration.getQuality().getReworkCreationRate()));
            double defective = sampleDefects(attempted, phaseProbability, random);
            work.recordRework(phase, attempted, defective);
            work.addTestableWork(ProjectPhase.TESTING,
                    (attempted - defective) * configuration.getQuality().getRegressionTestingFactor());
            attemptedTotal += attempted;
            fixedTotal += attempted - defective;
            defectiveTotal += defective;
            remainingCapacity -= attempted;
            if (remainingCapacity <= 1.0e-9) {
                break;
            }
        }
        return new ReworkResult(attemptedTotal, fixedTotal, defectiveTotal,
                Math.max(0.0, remainingCapacity));
    }

    private double sampleDefects(double attempted, double probability, Random random) {
        if (probability <= 0.0 || attempted <= 0.0) {
            return 0.0;
        }
        if (probability >= 1.0) {
            return attempted;
        }
        double mean = attempted * probability;
        double deviation = Math.sqrt(attempted * probability * (1.0 - probability));
        return Math.max(0.0, Math.min(attempted,
                Math.rint(mean + random.nextGaussian() * deviation)));
    }

    private double finite(double value) {
        return Double.isFinite(value) ? value : 0.0;
    }

    public record ReworkResult(double attempted, double fixedCorrectly,
                               double defectiveAgain, double unusedCapacity) {
    }
}
