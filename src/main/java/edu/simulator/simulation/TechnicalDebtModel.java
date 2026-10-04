package edu.simulator.simulation;

import edu.simulator.configuration.PhaseFiveConfiguration;
import edu.simulator.model.ConcurrencyPolicy;
import edu.simulator.model.EngineeringApproach;
import edu.simulator.model.Project;
import edu.simulator.model.SimulationValues;
import edu.simulator.model.TechnicalDebtPriority;
import edu.simulator.model.WorkIntensity;

public class TechnicalDebtModel {
    public double productivityModifier(double debt, PhaseFiveConfiguration configuration) {
        double normalized = SimulationValues.unitInterval(debt);
        return 1.0 - configuration.getTechnicalDebt().getProductivityMaxPenalty()
                * normalized * normalized;
    }

    public double defectModifier(double debt, PhaseFiveConfiguration configuration) {
        double normalized = SimulationValues.unitInterval(debt);
        return 1.0 + configuration.getTechnicalDebt().getDefectMaxIncrease()
                * normalized * normalized;
    }

    public double reworkModifier(double debt, PhaseFiveConfiguration configuration) {
        double normalized = SimulationValues.unitInterval(debt);
        return 1.0 + configuration.getTechnicalDebt().getReworkDifficultyMaxIncrease()
                * normalized * normalized;
    }

    public PayDownPlan planPayDown(Project project, double availableDeveloperWork,
                                   TechnicalDebtPriority priority,
                                   PhaseFiveConfiguration configuration) {
        if (priority != TechnicalDebtPriority.PAY_DOWN || project.getTechnicalDebt() <= 0.0) {
            return new PayDownPlan(0.0, 0.0);
        }
        double scope = Math.max(1.0, project.getWorkState().totalScope());
        double workInvested = Math.max(0.0, availableDeveloperWork)
                * configuration.getTechnicalDebt().getPayDownCapacityFraction();
        double reduction = workInvested / scope
                * configuration.getTechnicalDebt().getPayDownEffectiveness();
        double actualReduction = Math.min(project.getTechnicalDebt(), reduction);
        double capacitySpent = Math.min(Math.max(0.0, availableDeveloperWork), workInvested);
        return new PayDownPlan(capacitySpent, actualReduction);
    }

    public double updateAtWeekEnd(Project project, EngineeringApproach approach,
                                  WorkIntensity intensity, ConcurrencyPolicy concurrency,
                                  double schedulePressure, double dependencyUncertainty,
                                  double technicalDebtSensitivity,
                                  PhaseFiveConfiguration configuration) {
        return updateAtWeekEnd(project, approach, intensity, concurrency, schedulePressure,
                dependencyUncertainty, technicalDebtSensitivity, 0.0, configuration);
    }

    public double updateAtWeekEnd(Project project, EngineeringApproach approach,
                                  WorkIntensity intensity, ConcurrencyPolicy concurrency,
                                  double schedulePressure, double dependencyUncertainty,
                                  double technicalDebtSensitivity, double debtPaidDown,
                                  PhaseFiveConfiguration configuration) {
        var settings = configuration.getTechnicalDebt();
        double change = configuration.getEngineeringApproach().debtRate(approach)
                + settings.getBaseAccumulation()
                + settings.getPressureAccumulation() * SimulationValues.unitInterval(schedulePressure)
                + (intensity == WorkIntensity.CRUNCH ? settings.getCrunchAccumulation() : 0.0)
                + settings.getConcurrencyAccumulation()
                    * SimulationValues.unitInterval(dependencyUncertainty)
                    * (concurrency == ConcurrencyPolicy.AGGRESSIVE ? 1.0 : 0.5);
        double before = project.getTechnicalDebt();
        double sensitivity = Double.isFinite(technicalDebtSensitivity)
                ? Math.max(0.0, Math.min(5.0, technicalDebtSensitivity)) : 1.0;
        project.setTechnicalDebt(before + change * sensitivity
                - Math.max(0.0, Double.isFinite(debtPaidDown) ? debtPaidDown : 0.0));
        return project.getTechnicalDebt() - before;
    }

    public String category(double debt) {
        double value = SimulationValues.unitInterval(debt);
        if (value < 0.2) return "Low";
        if (value < 0.45) return "Moderate";
        if (value < 0.72) return "High";
        return "Severe";
    }

    public record PayDownPlan(double capacitySpent, double debtReduction) {
        public PayDownPlan {
            if (!Double.isFinite(capacitySpent) || capacitySpent < 0.0
                    || !Double.isFinite(debtReduction) || debtReduction < 0.0) {
                throw new IllegalArgumentException("Debt pay-down plan must be finite and non-negative");
            }
        }
    }
}
