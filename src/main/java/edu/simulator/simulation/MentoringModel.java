package edu.simulator.simulation;

import edu.simulator.model.Employee;
import edu.simulator.model.ExperienceLevel;
import edu.simulator.model.Role;
import edu.simulator.model.Team;

public class MentoringModel {
    public double calculateMentorLoad(Team team) {
        int juniors = 0;
        int seniors = 0;
        for (Employee employee : team.allEmployees()) {
            if (!employee.isActive()) {
                continue;
            }
            if (employee.getExperienceLevel() == ExperienceLevel.JUNIOR) {
                juniors++;
            }
            if (employee.getExperienceLevel() == ExperienceLevel.SENIOR) {
                seniors++;
            }
        }
        if (seniors == 0) {
            return juniors * 0.8;
        }
        return Math.max(0.0, juniors / (double) Math.max(1, seniors)) * 0.75;
    }

    public double onboardingModifier(Employee employee) {
        double raw = switch (employee.getExperienceLevel()) {
            case JUNIOR -> 0.7;
            case MID_LEVEL -> 0.85;
            case SENIOR -> 0.92;
        };
        return Math.max(0.35, raw);
    }

    public double calculateDeveloperCapacity(Team team) {
        double capacity = 0.0;
        for (Employee employee : team.allEmployees()) {
            if (employee.getRole() == Role.DEVELOPER) {
                capacity += employee.getExperienceLevel() == ExperienceLevel.SENIOR ? 1.2 : 1.0;
            }
        }
        return capacity;
    }
}
