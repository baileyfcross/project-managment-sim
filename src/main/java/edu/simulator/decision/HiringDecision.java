package edu.simulator.decision;

import edu.simulator.model.ExperienceLevel;
import edu.simulator.model.Role;

import java.util.Objects;

public record HiringDecision(Role role, ExperienceLevel experienceLevel, int quantity) {
    public HiringDecision {
        Objects.requireNonNull(role, "role");
        Objects.requireNonNull(experienceLevel, "experienceLevel");
        if (quantity < 1 || quantity > 10) {
            throw new IllegalArgumentException("Hiring quantity must be between 1 and 10");
        }
    }
}
