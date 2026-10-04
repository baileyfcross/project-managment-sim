package edu.simulator.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;

public final class ScenarioLoader {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ScenarioLoader() {
    }

    public static ScenarioConfiguration loadScenario(String scenarioId) {
        String resourcePath = "/scenarios/" + scenarioId + ".json";
        try (InputStream inputStream = ScenarioLoader.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalStateException("Scenario not found: " + scenarioId);
            }
            return OBJECT_MAPPER.readValue(inputStream, ScenarioConfiguration.class);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load scenario: " + scenarioId, e);
        }
    }
}
