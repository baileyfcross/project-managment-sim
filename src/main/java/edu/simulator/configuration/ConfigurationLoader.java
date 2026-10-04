package edu.simulator.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;

public final class ConfigurationLoader {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ConfigurationLoader() {
    }

    public static SimulationConfiguration loadDefault() {
        try (InputStream inputStream = ConfigurationLoader.class.getResourceAsStream("/configuration/simulation-defaults.json")) {
            if (inputStream == null) {
                throw new IllegalStateException("Missing default configuration at /configuration/simulation-defaults.json");
            }
            return OBJECT_MAPPER.readValue(inputStream, SimulationConfiguration.class);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load simulation defaults", e);
        }
    }
}
