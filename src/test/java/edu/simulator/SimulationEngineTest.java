package edu.simulator;

import edu.simulator.configuration.ConfigurationLoader;
import edu.simulator.configuration.ScenarioLoader;
import edu.simulator.model.*;
import edu.simulator.simulation.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimulationEngineTest {
    @Test
    void defaultScenarioLoadsAndProducesHistory() {
        var scenario = ScenarioLoader.loadScenario("small-web-app");
        var configuration = ConfigurationLoader.loadDefault();
        var engine = new SimulationEngine(scenario, configuration, 42L);

        engine.advanceWeek();
        engine.advanceWeek();
        engine.advanceWeek();

        assertFalse(engine.getHistory().isEmpty());
        assertTrue(engine.getCurrentState().getWeek() >= 3);
        assertTrue(engine.getCurrentState().getTotalRemainingWork() >= 0.0);
        assertTrue(engine.getCurrentState().getTeamCounts().getOrDefault("DEVELOPER", 0) >= 0);
    }

    @Test
    void fatigueAndProductivityBehaveReasonably() {
        var config = ConfigurationLoader.loadDefault();
        var fatigueModel = new FatigueModel();
        double fatigue = fatigueModel.updateFatigue(0.1, WorkIntensity.CRUNCH, config);
        assertTrue(fatigue >= 0.0 && fatigue <= 1.0);

        Team team = new Team();
        team.addEmployee(new Employee(Role.DEVELOPER, ExperienceLevel.MID_LEVEL, 2100.0));
        team.addEmployee(new Employee(Role.DEVELOPER, ExperienceLevel.MID_LEVEL, 2100.0));
        team.addEmployee(new Employee(Role.QA_ENGINEER, ExperienceLevel.MID_LEVEL, 1900.0));

        var productivity = new ProductivityModel().calculate(team, WorkIntensity.SUSTAINABLE, 0.15, 0.2, config);
        assertTrue(productivity.totalEffectiveCapacity() > 0.0);

        var defect = new DefectModel().defectProbability(0.08, 0.8, 0.7, 0.12, 0.5, config);
        assertTrue(defect >= 0.0 && defect <= config.getQuality().getDefectCap());
    }

    @Test
    void scenarioLoaderReturnsExpectedValues() {
        var scenario = ScenarioLoader.loadScenario("small-web-app");
        assertEquals("small-web-app", scenario.getId());
        assertEquals(20, scenario.getDeadlineWeeks());
        assertTrue(scenario.getBudget().compareTo(java.math.BigDecimal.ZERO) > 0);
    }
}
