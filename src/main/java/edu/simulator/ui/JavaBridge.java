package edu.simulator.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.simulator.configuration.ConfigurationLoader;
import edu.simulator.configuration.ScenarioConfiguration;
import edu.simulator.configuration.ScenarioLoader;
import edu.simulator.configuration.SimulationConfiguration;
import edu.simulator.decision.HiringDecision;
import edu.simulator.model.ExperienceLevel;
import edu.simulator.model.Role;
import edu.simulator.model.TestingPriority;
import edu.simulator.model.WorkIntensity;
import edu.simulator.simulation.CostModel;
import edu.simulator.simulation.SimulationEngine;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.EnumMap;
import java.util.Map;

/** Narrow JSON API exposed to the embedded frontend. */
public class JavaBridge {
    private static final int MAX_EMPLOYEES_PER_ROLE = 12;
    private static final int MAX_INITIAL_TEAM_SIZE = 36;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SecureRandom seedGenerator = new SecureRandom();
    private final SimulationConfiguration configuration = ConfigurationLoader.loadDefault();
    private ScenarioConfiguration scenario;
    private Map<Role, Integer> selectedTeam;
    private SimulationEngine simulationEngine;

    public JavaBridge() {
        scenario = ScenarioLoader.loadScenario("small-web-app");
        selectedTeam = readScenarioTeam(scenario);
    }

    public String getSetupState() {
        return writeJson(createSetupState());
    }

    public String updateInitialTeam(String teamJson) {
        if (simulationEngine != null) {
            throw new IllegalStateException("The initial team cannot be changed after the project starts");
        }
        Map<Role, Integer> updatedTeam = parseTeam(teamJson);
        validateTeam(updatedTeam, false);
        selectedTeam = updatedTeam;
        return getSetupState();
    }

    public String startSimulation(String scenarioId, String seedText) {
        if (simulationEngine != null) {
            throw new IllegalStateException("A project has already been started");
        }
        if (scenarioId != null && !scenarioId.isBlank()
                && !scenario.getId().equals(scenarioId)) {
            scenario = ScenarioLoader.loadScenario(scenarioId);
            selectedTeam = readScenarioTeam(scenario);
        }
        validateTeam(selectedTeam, true);

        long seed = parseSeed(seedText);
        simulationEngine = new SimulationEngine(
                scenario, configuration, seed, new EnumMap<>(selectedTeam));
        return getSimulationState();
    }

    public String getSimulationState() {
        if (simulationEngine == null) {
            throw new IllegalStateException("The project has not started");
        }
        return writeJson(simulationEngine.getCurrentState());
    }

    public String advanceWeek() {
        requireSimulation();
        simulationEngine.advanceWeek();
        return getSimulationState();
    }

    public String setWorkIntensity(String intensity) {
        requireSimulation();
        try {
            simulationEngine.setWorkIntensity(WorkIntensity.valueOf(intensity));
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new IllegalArgumentException("Unknown work intensity: " + intensity, exception);
        }
        return getSimulationState();
    }

    public String setTestingPriority(String priority) {
        requireSimulation();
        try {
            simulationEngine.setTestingPriority(TestingPriority.valueOf(priority));
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new IllegalArgumentException("Unknown testing priority: " + priority, exception);
        }
        return getSimulationState();
    }

    public String getTeamManagementState() {
        requireSimulation();
        return writeJson(simulationEngine.getTeamManagementState());
    }

    public String hireEmployee(String roleName, String experienceName, int quantity) {
        requireSimulation();
        final Role role;
        final ExperienceLevel experience;
        try {
            role = Role.valueOf(roleName);
            experience = ExperienceLevel.valueOf(experienceName);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new IllegalArgumentException("Choose a valid role and experience level", exception);
        }
        simulationEngine.hire(new HiringDecision(role, experience, quantity));
        return getSimulationState();
    }

    private SetupStateDto createSetupState() {
        BigDecimal weeklyPayroll = new CostModel().calculateWeeklyPayroll(selectedTeam, configuration);
        BigDecimal projectedPayroll = weeklyPayroll.multiply(BigDecimal.valueOf(scenario.getDeadlineWeeks()));
        return new SetupStateDto(
                scenario.getId(),
                scenario.getName(),
                scenario.getDeadlineWeeks(),
                scenario.getBudget(),
                roleCounts(selectedTeam),
                weeklyPayroll,
                projectedPayroll,
                scenario.getBudget().subtract(projectedPayroll)
        );
    }

    private Map<Role, Integer> parseTeam(String teamJson) {
        final JsonNode teamNode;
        try {
            teamNode = objectMapper.readTree(teamJson);
        } catch (IOException exception) {
            throw new IllegalArgumentException("Team configuration must be valid JSON", exception);
        }
        if (teamNode == null || !teamNode.isObject()) {
            throw new IllegalArgumentException("Team configuration must be a JSON object");
        }

        Map<Role, Integer> counts = new EnumMap<>(Role.class);
        for (Role role : Role.values()) {
            counts.put(role, 0);
        }
        var fields = teamNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            final Role role;
            try {
                role = Role.valueOf(field.getKey());
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException("Unknown team role: " + field.getKey(), exception);
            }
            JsonNode value = field.getValue();
            if (!value.isIntegralNumber() || !value.canConvertToInt()) {
                throw new IllegalArgumentException("Team counts must be whole numbers");
            }
            int count = value.intValue();
            if (count < 0 || count > MAX_EMPLOYEES_PER_ROLE) {
                throw new IllegalArgumentException(
                        role + " count must be between 0 and " + MAX_EMPLOYEES_PER_ROLE);
            }
            counts.put(role, count);
        }
        return counts;
    }

    private void validateTeam(Map<Role, Integer> counts, boolean requirePeople) {
        int total = counts.values().stream().mapToInt(Integer::intValue).sum();
        if (total > MAX_INITIAL_TEAM_SIZE) {
            throw new IllegalArgumentException(
                    "Initial team cannot exceed " + MAX_INITIAL_TEAM_SIZE + " people");
        }
        if (requirePeople && total == 0) {
            throw new IllegalArgumentException("Choose at least one team member before starting");
        }
    }

    private long parseSeed(String seedText) {
        if (seedText == null || seedText.isBlank()) {
            return seedGenerator.nextLong();
        }
        try {
            return Long.parseLong(seedText.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Seed must be a whole number between -2^63 and 2^63-1", exception);
        }
    }

    private Map<Role, Integer> readScenarioTeam(ScenarioConfiguration source) {
        Map<Role, Integer> counts = new EnumMap<>(Role.class);
        for (Role role : Role.values()) {
            counts.put(role, 0);
        }
        if (source.getInitialTeam() == null) {
            return counts;
        }
        source.getInitialTeam().forEach((key, value) -> {
            Role role = switch (key) {
                case "projectManagers" -> Role.PROJECT_MANAGER;
                case "developers" -> Role.DEVELOPER;
                case "qaEngineers" -> Role.QA_ENGINEER;
                case "devOpsEngineers", "devopsEngineers" -> Role.DEVOPS_ENGINEER;
                default -> throw new IllegalArgumentException("Unknown role in scenario: " + key);
            };
            counts.put(role, value);
        });
        validateTeam(counts, true);
        return counts;
    }

    private Map<String, Integer> roleCounts(Map<Role, Integer> counts) {
        Map<String, Integer> result = new java.util.LinkedHashMap<>();
        for (Role role : Role.values()) {
            result.put(role.name(), counts.getOrDefault(role, 0));
        }
        return result;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to serialize application state", exception);
        }
    }

    private void requireSimulation() {
        if (simulationEngine == null) {
            throw new IllegalStateException("Start a project before applying simulation actions");
        }
    }
}
