package edu.simulator.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.simulator.configuration.ConfigurationLoader;
import edu.simulator.configuration.ScenarioConfiguration;
import edu.simulator.configuration.ScenarioLoader;
import edu.simulator.model.Role;
import edu.simulator.simulation.SimulationEngine;
import edu.simulator.simulation.WorkIntensity;

import java.util.HashMap;
import java.util.Map;

public class JavaBridge {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private SimulationEngine simulationEngine;

    public String getSimulationState() {
        if (simulationEngine == null) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(simulationEngine.getCurrentState());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize simulation state", e);
        }
    }

    public String startScenario(String scenarioId, long seed) {
        try {
            ScenarioConfiguration scenario = ScenarioLoader.loadScenario(scenarioId == null || scenarioId.isBlank() ? "small-web-app" : scenarioId);
            simulationEngine = new SimulationEngine(scenario, ConfigurationLoader.loadDefault(), seed);
            return getSimulationState();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to start scenario", e);
        }
    }

    public String updateInitialTeam(String teamJson) {
        if (simulationEngine == null) {
            return "{}";
        }
        Map<Role, Integer> counts = new HashMap<>();
        try {
            Map<String, Integer> raw = objectMapper.readValue(teamJson, Map.class);
            for (Map.Entry<String, Integer> entry : raw.entrySet()) {
                counts.put(Role.valueOf(entry.getKey()), entry.getValue());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Invalid team payload", e);
        }
        return getSimulationState();
    }

    public String setWorkIntensity(String intensity) {
        if (simulationEngine == null) {
            return "{}";
        }
        simulationEngine.setWorkIntensity(WorkIntensity.valueOf(intensity));
        return getSimulationState();
    }

    public String advanceWeek() {
        if (simulationEngine == null) {
            return "{}";
        }
        simulationEngine.advanceWeek();
        return getSimulationState();
    }

    public String getHistory() {
        if (simulationEngine == null) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(simulationEngine.getHistory());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize history", e);
        }
    }
}
