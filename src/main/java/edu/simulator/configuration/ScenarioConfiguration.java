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
    private double featureRequestRate = 1.0;
    private double stakeholderVolatility = 1.0;
    private double dependencyRisk = 1.0;
    private double technicalDebtSensitivity = 1.0;
    private double projectComplexity = 1.0;
    private Map<String, Double> eventWeights = new HashMap<>();

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

    public double getFeatureRequestRate() { return featureRequestRate; }
    public void setFeatureRequestRate(double value) { featureRequestRate = value; }
    public double getStakeholderVolatility() { return stakeholderVolatility; }
    public void setStakeholderVolatility(double value) { stakeholderVolatility = value; }
    public double getDependencyRisk() { return dependencyRisk; }
    public void setDependencyRisk(double value) { dependencyRisk = value; }
    public double getTechnicalDebtSensitivity() { return technicalDebtSensitivity; }
    public void setTechnicalDebtSensitivity(double value) { technicalDebtSensitivity = value; }
    public double getProjectComplexity() { return projectComplexity; }
    public void setProjectComplexity(double value) { projectComplexity = value; }
    public Map<String, Double> getEventWeights() { return eventWeights; }
    public void setEventWeights(Map<String, Double> value) { eventWeights = value; }

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
        if (eventWeights == null) {
            throw new IllegalArgumentException("Scenario event weights cannot be null");
        }
        for (double factor : new double[]{featureRequestRate, stakeholderVolatility,
                dependencyRisk, technicalDebtSensitivity, projectComplexity}) {
            if (!Double.isFinite(factor) || factor < 0.0 || factor > 5.0) {
                throw new IllegalArgumentException("Scenario event and complexity factors must be between 0 and 5");
            }
        }
        for (Map.Entry<String, Double> entry : eventWeights.entrySet()) {
            try {
                edu.simulator.event.EventType.valueOf(entry.getKey().toUpperCase());
            } catch (IllegalArgumentException | NullPointerException exception) {
                throw new IllegalArgumentException("Unknown scenario event type: " + entry.getKey(), exception);
            }
            if (entry.getValue() == null || !Double.isFinite(entry.getValue())
                    || entry.getValue() < 0.0 || entry.getValue() > 10.0) {
                throw new IllegalArgumentException("Scenario event weights must be between 0 and 10");
            }
        }
        for (Map.Entry<String, Integer> entry : initialTeam.entrySet()) {
            if (entry.getValue() == null || entry.getValue() < 0) {
                throw new IllegalArgumentException("Scenario team counts must be non-negative");
            }
        }
    }
}
