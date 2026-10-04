package edu.simulator.configuration;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ScenarioConfiguration {
    private String id = "small-web-app";
    private String name = "Small Business Web Application";
    private int deadlineWeeks = 20;
    private BigDecimal budget = BigDecimal.valueOf(400000);
    private boolean budgetFailureAllowed = true;
    private Map<String, Integer> initialTeam = new HashMap<>();
    private Map<String, Double> scope = new HashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDeadlineWeeks() {
        return deadlineWeeks;
    }

    public void setDeadlineWeeks(int deadlineWeeks) {
        this.deadlineWeeks = deadlineWeeks;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public boolean isBudgetFailureAllowed() {
        return budgetFailureAllowed;
    }

    public void setBudgetFailureAllowed(boolean budgetFailureAllowed) {
        this.budgetFailureAllowed = budgetFailureAllowed;
    }

    public Map<String, Integer> getInitialTeam() {
        return initialTeam;
    }

    public void setInitialTeam(Map<String, Integer> initialTeam) {
        this.initialTeam = initialTeam;
    }

    public Map<String, Double> getScope() {
        return scope;
    }

    public void setScope(Map<String, Double> scope) {
        this.scope = scope;
    }

    public void validate() {
        if (id == null || id.isBlank() || name == null || name.isBlank()) {
            throw new IllegalArgumentException("Scenario id and name are required");
        }
        if (deadlineWeeks < 1 || budget == null || budget.signum() < 0) {
            throw new IllegalArgumentException("Scenario deadline and budget must be valid");
        }
        if (scope == null || scope.isEmpty()) {
            throw new IllegalArgumentException("Scenario must define project work scope");
        }
        for (Map.Entry<String, Double> entry : scope.entrySet()) {
            try {
                edu.simulator.model.ProjectPhase.valueOf(entry.getKey().toUpperCase());
            } catch (IllegalArgumentException | NullPointerException exception) {
                throw new IllegalArgumentException("Unknown project phase: " + entry.getKey(), exception);
            }
            if (entry.getValue() == null || !Double.isFinite(entry.getValue()) || entry.getValue() < 0.0) {
                throw new IllegalArgumentException("Scenario work values must be finite and non-negative");
            }
        }
        Objects.requireNonNull(initialTeam, "Scenario initialTeam is required");
        for (Map.Entry<String, Integer> entry : initialTeam.entrySet()) {
            if (entry.getValue() == null || entry.getValue() < 0) {
                throw new IllegalArgumentException("Scenario team counts must be non-negative");
            }
        }
    }
}
