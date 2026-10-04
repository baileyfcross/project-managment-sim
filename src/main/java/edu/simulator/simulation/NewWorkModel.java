package edu.simulator.simulation;

import edu.simulator.configuration.SimulationConfiguration;
import edu.simulator.model.ProjectPhase;
import edu.simulator.model.WorkState;
import edu.simulator.model.WorkIntensity;
import edu.simulator.model.ConcurrencyPolicy;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

public class NewWorkModel {
    private final DefectModel defectModel = new DefectModel();

    public NewWorkResult performDevelopment(WorkState work, double capacity,
                                           double experienceModifier,
                                           double onboardingDeficit, double mentoringCoverage, double fatigue,
                                           double coordinationPenalty, double schedulePressure,
                                           WorkIntensity intensity,
                                           SimulationConfiguration configuration,
                                           Random random) {
        return performDevelopment(work, capacity, experienceModifier, onboardingDeficit,
                mentoringCoverage, fatigue, coordinationPenalty, schedulePressure, intensity,
                ConcurrencyPolicy.MODERATE, 1.0, 1.0, 0.0, configuration, random);
    }

    public NewWorkResult performDevelopment(WorkState work, double capacity,
                                           double experienceModifier,
                                           double onboardingDeficit, double mentoringCoverage, double fatigue,
                                           double coordinationPenalty, double schedulePressure,
                                           WorkIntensity intensity, ConcurrencyPolicy concurrency,
                                           double technicalDebtModifier, double engineeringDefectModifier,
                                           double phaseFiveCoordinationPenalty,
                                           SimulationConfiguration configuration, Random random) {
        double remainingCapacity = finiteNonNegative(capacity);
        Map<ProjectPhase, Double> attemptedByPhase = new EnumMap<>(ProjectPhase.class);
        Map<ProjectPhase, Double> defectsByPhase = new EnumMap<>(ProjectPhase.class);
        double outOfSequenceWork = 0.0;
        for (ProjectPhase phase : new ProjectPhase[]{
                ProjectPhase.REQUIREMENTS, ProjectPhase.DESIGN, ProjectPhase.DEVELOPMENT}) {
            double readiness = new ConcurrencyModel().readiness(
                    phase, work, concurrency, configuration.getPhaseFive());
            double uncertainty = new ConcurrencyModel().dependencyUncertainty(
                    phase, work, concurrency, phaseFiveCoordinationPenalty,
                    configuration.getPhaseFive());
            double attempted = Math.min(remainingCapacity,
                    availableWork(phase, work, readiness));
            if (attempted <= 0.0) {
                attemptedByPhase.put(phase, 0.0);
                defectsByPhase.put(phase, 0.0);
                continue;
            }
            double probability = defectModel.defectProbability(phase, experienceModifier,
                    onboardingDeficit, mentoringCoverage, fatigue, coordinationPenalty, schedulePressure,
                    intensity, work, technicalDebtModifier, engineeringDefectModifier,
                    uncertainty, configuration);
            double defective = defectModel.sampleDefectiveWork(attempted, probability, random);
            double actualAttempted = work.recordNewWork(phase, attempted, defective);
            attemptedByPhase.put(phase, actualAttempted);
            defectsByPhase.put(phase, defective);
            outOfSequenceWork += actualAttempted * uncertainty;
            if (phase == ProjectPhase.DEVELOPMENT) {
                double devScope = work.getTotalWork(ProjectPhase.DEVELOPMENT);
                double testScopeRemaining = Math.max(0.0,
                        work.getBaseWorkRemaining(ProjectPhase.TESTING)
                                - work.getTestingBacklog(ProjectPhase.DEVELOPMENT));
                if (devScope > 0.0 && testScopeRemaining > 0.0) {
                    double testable = Math.min(testScopeRemaining,
                            actualAttempted * work.getTotalWork(ProjectPhase.TESTING) / devScope);
                    work.addTestableWork(ProjectPhase.DEVELOPMENT, testable);
                }
            }
            remainingCapacity -= actualAttempted;
        }
        return new NewWorkResult(Map.copyOf(attemptedByPhase), Map.copyOf(defectsByPhase),
                Math.max(0.0, finiteNonNegative(capacity) - remainingCapacity),
                Math.min(finiteNonNegative(capacity), outOfSequenceWork));
    }

    public NewWorkResult performDeployment(WorkState work, double capacity,
                                           double experienceModifier, double mentoringCoverage, double fatigue,
                                           double coordinationPenalty, double schedulePressure,
                                           WorkIntensity intensity,
                                           SimulationConfiguration configuration,
                                           Random random) {
        return performDeployment(work, capacity, experienceModifier, mentoringCoverage, fatigue,
                coordinationPenalty, schedulePressure, intensity, ConcurrencyPolicy.MODERATE,
                0.0, 1.0, configuration, random);
    }

    public NewWorkResult performDeployment(WorkState work, double capacity,
                                           double experienceModifier, double mentoringCoverage, double fatigue,
                                           double coordinationPenalty, double schedulePressure,
                                           WorkIntensity intensity, ConcurrencyPolicy concurrency,
                                           double phaseFiveCoordinationPenalty,
                                           double technicalDebtModifier,
                                           SimulationConfiguration configuration, Random random) {
        ProjectPhase phase = ProjectPhase.DEPLOYMENT;
        double uncertainty = new ConcurrencyModel().dependencyUncertainty(
                phase, work, concurrency, phaseFiveCoordinationPenalty,
                configuration.getPhaseFive());
        double attempted = Math.min(finiteNonNegative(capacity),
                availableWork(phase, work, new ConcurrencyModel().readiness(
                        phase, work, concurrency, configuration.getPhaseFive())));
        if (attempted <= 0.0) {
            return new NewWorkResult(Map.of(phase, 0.0), Map.of(phase, 0.0), 0.0, 0.0);
        }
        double probability = defectModel.defectProbability(phase, experienceModifier,
                0.0, mentoringCoverage, fatigue, coordinationPenalty, schedulePressure, intensity,
                work, technicalDebtModifier, 1.0, uncertainty, configuration);
        probability *= 0.7;
        double defective = defectModel.sampleDefectiveWork(attempted, probability, random);
        double actualAttempted = work.recordNewWork(phase, attempted, defective);
        work.addTestableWork(phase, actualAttempted);
        return new NewWorkResult(Map.of(phase, actualAttempted),
                Map.of(phase, defective), actualAttempted, actualAttempted * uncertainty);
    }

    private double availableWork(ProjectPhase phase, WorkState work, double readiness) {
        double target = work.getTotalWork(phase) * readiness;
        double alreadyAttempted = work.getTotalWork(phase) - work.getBaseWorkRemaining(phase);
        return Math.max(0.0, target - alreadyAttempted);
    }

    private double finiteNonNegative(double value) {
        return Double.isFinite(value) ? Math.max(0.0, value) : 0.0;
    }

    public record NewWorkResult(Map<ProjectPhase, Double> attemptedByPhase,
                                Map<ProjectPhase, Double> defectsByPhase,
                                double totalAttempted, double outOfSequenceWork) {
    }
}
