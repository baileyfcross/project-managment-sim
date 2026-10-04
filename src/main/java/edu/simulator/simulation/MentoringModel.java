package edu.simulator.simulation;

import edu.simulator.configuration.SimulationConfiguration;
import edu.simulator.model.Employee;
import edu.simulator.model.ExperienceLevel;
import edu.simulator.model.OnboardingState;
import edu.simulator.model.Team;

public class MentoringModel {
    public MentoringResult calculate(Team team, int pendingHires,
                                     SimulationConfiguration configuration) {
        double capacity = 0.0;
        double demand = 0.0;
        FatigueModel fatigueModel = new FatigueModel();
        for (Employee employee : team.activeEmployees()) {
            double fatigueCapacity = fatigueModel.mentoringModifier(
                    employee.getFatigue(), configuration);
            if (employee.getExperienceLevel() == ExperienceLevel.SENIOR) {
                capacity += configuration.getMentoring().getSeniorCapacity() * fatigueCapacity;
            } else if (employee.getExperienceLevel() == ExperienceLevel.MID_LEVEL) {
                capacity += configuration.getMentoring().getMidLevelCapacity() * fatigueCapacity;
            } else {
                demand += configuration.getMentoring().getJuniorDemand();
            }
            if (employee.getOnboardingProgress() < 1.0) {
                demand += configuration.getMentoring().getOnboardingDemand()
                        * (1.0 - employee.getOnboardingProgress());
            }
        }
        demand += pendingHires * configuration.getMentoring().getOnboardingDemand();
        double coverage = demand == 0.0 ? 1.0 : clamp(capacity / demand);
        double load = capacity == 0.0 ? (demand == 0.0 ? 0.0 : 1.0) : clamp(demand / capacity);
        String loadLabel = load < 0.35 ? "Low"
                : load < 0.7 ? "Moderate"
                : load < 1.0 ? "High" : "Overloaded";
        return new MentoringResult(capacity, demand, coverage, load, loadLabel);
    }

    public int advanceOnboarding(Team team, MentoringResult mentoring,
                                 SimulationConfiguration configuration) {
        double mentoringFactor = configuration.getMentoring().getMinimumProgressFactor()
                + (1.0 - configuration.getMentoring().getMinimumProgressFactor())
                * mentoring.coverage();
        int completed = 0;
        for (Employee employee : team.onboardingEmployees()) {
            double initial = configuration.getOnboarding().getInitialEffectiveness();
            int duration = Math.max(1, employee.getOnboardingDurationWeeks());
            double step = (1.0 - initial) / duration;
            double previous = employee.getOnboardingProgress();
            double progress = Math.min(1.0,
                    employee.getOnboardingProgress() + step * mentoringFactor);
            employee.setOnboardingProgress(progress);
            employee.setOnboardingState(stateFor(progress));
            if (previous < 1.0 && progress >= 1.0) {
                completed++;
            }
        }
        return completed;
    }

    public double directProductivityModifier(Employee employee, MentoringResult result,
                                             SimulationConfiguration configuration) {
        if (employee.getExperienceLevel() == ExperienceLevel.JUNIOR || result.capacity() <= 0.0) {
            return 1.0;
        }
        double timeSpentMentoring = Math.min(1.0, result.demand() / result.capacity());
        double mentoringCost = Math.max(0.0, 1.0
                - timeSpentMentoring * configuration.getMentoring().getMaximumDirectProductivityLoss());
        return mentoringCost;
    }

    public double calculateMentorLoad(Team team) {
        return calculate(team, 0, new SimulationConfiguration()).load();
    }

    public double onboardingModifier(Employee employee) {
        return employee.getOnboardingProgress();
    }

    public double calculateDeveloperCapacity(Team team) {
        return team.activeEmployees().stream()
                .filter(employee -> employee.getRole() == edu.simulator.model.Role.DEVELOPER)
                .mapToDouble(employee -> employee.getExperienceLevel() == ExperienceLevel.SENIOR ? 1.2 : 1.0)
                .sum();
    }

    public OnboardingState stateFor(double progress) {
        if (progress >= 1.0) {
            return OnboardingState.FULLY_INTEGRATED;
        }
        if (progress >= 0.75) {
            return OnboardingState.NEARLY_READY;
        }
        if (progress >= 0.4) {
            return OnboardingState.LEARNING_PROJECT;
        }
        return OnboardingState.NEW;
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, Double.isFinite(value) ? value : 0.0));
    }

    public record MentoringResult(double capacity, double demand, double coverage,
                                  double load, String loadLabel) {
    }
}
