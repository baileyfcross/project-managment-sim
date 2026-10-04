package edu.simulator.simulation;

import edu.simulator.configuration.SimulationConfiguration;
import edu.simulator.model.ProjectPhase;
import edu.simulator.model.WorkState;
import edu.simulator.model.WorkIntensity;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

public class NewWorkModel {
    private final PhaseReadinessModel readinessModel = new PhaseReadinessModel();
    private final DefectModel defectModel = new DefectModel();

    public NewWorkResult performDevelopment(WorkState work, double capacity,
                                           double experienceModifier,
                                           double onboardingDeficit, double mentoringCoverage, double fatigue,
                                           double coordinationPenalty, double schedulePressure,
                                           WorkIntensity intensity,
                                           SimulationConfiguration configuration,
                                           Random random) {
        double remainingCapacity = finiteNonNegative(capacity);
        Map<ProjectPhase, Double> attemptedByPhase = new EnumMap<>(ProjectPhase.class);
        Map<ProjectPhase, Double> defectsByPhase = new EnumMap<>(ProjectPhase.class);
        for (ProjectPhase phase : new ProjectPhase[]{
                ProjectPhase.REQUIREMENTS, ProjectPhase.DESIGN, ProjectPhase.DEVELOPMENT}) {
            double attempted = Math.min(remainingCapacity, availableWork(phase, work, configuration));
            if (attempted <= 0.0) {
                attemptedByPhase.put(phase, 0.0);
                defectsByPhase.put(phase, 0.0);
                continue;
            }
            double probability = defectModel.defectProbability(phase, experienceModifier,
                    onboardingDeficit, mentoringCoverage, fatigue, coordinationPenalty, schedulePressure,
                    intensity, work, configuration);
            double defective = defectModel.sampleDefectiveWork(attempted, probability, random);
            double actualAttempted = work.recordNewWork(phase, attempted, defective);
            attemptedByPhase.put(phase, actualAttempted);
            defectsByPhase.put(phase, defective);
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
                Math.max(0.0, finiteNonNegative(capacity) - remainingCapacity));
    }

    public NewWorkResult performDeployment(WorkState work, double capacity,
                                           double experienceModifier, double mentoringCoverage, double fatigue,
                                           double coordinationPenalty, double schedulePressure,
                                           WorkIntensity intensity,
                                           SimulationConfiguration configuration,
                                           Random random) {
        ProjectPhase phase = ProjectPhase.DEPLOYMENT;
        double attempted = Math.min(finiteNonNegative(capacity),
                availableWork(phase, work, configuration));
        if (attempted <= 0.0) {
            return new NewWorkResult(Map.of(phase, 0.0), Map.of(phase, 0.0), 0.0);
        }
        double probability = defectModel.defectProbability(phase, experienceModifier,
                0.0, mentoringCoverage, fatigue, coordinationPenalty, schedulePressure, intensity,
                work, configuration);
        probability *= 0.7;
        double defective = defectModel.sampleDefectiveWork(attempted, probability, random);
        double actualAttempted = work.recordNewWork(phase, attempted, defective);
        work.addTestableWork(phase, actualAttempted);
        return new NewWorkResult(Map.of(phase, actualAttempted),
                Map.of(phase, defective), actualAttempted);
    }

    private double availableWork(ProjectPhase phase, WorkState work,
                                 SimulationConfiguration configuration) {
        double target = work.getTotalWork(phase)
                * readinessModel.readiness(phase, work, configuration);
        double alreadyAttempted = work.getTotalWork(phase) - work.getBaseWorkRemaining(phase);
        return Math.max(0.0, target - alreadyAttempted);
    }

    private double finiteNonNegative(double value) {
        return Double.isFinite(value) ? Math.max(0.0, value) : 0.0;
    }

    public record NewWorkResult(Map<ProjectPhase, Double> attemptedByPhase,
                                Map<ProjectPhase, Double> defectsByPhase,
                                double totalAttempted) {
    }
}
