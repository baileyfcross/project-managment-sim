package edu.simulator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.simulator.configuration.ConfigurationLoader;
import edu.simulator.configuration.ScenarioLoader;
import edu.simulator.model.Employee;
import edu.simulator.model.ExperienceLevel;
import edu.simulator.model.Project;
import edu.simulator.model.ProjectPhase;
import edu.simulator.model.Role;
import edu.simulator.model.Team;
import edu.simulator.model.WorkState;
import edu.simulator.simulation.CostModel;
import edu.simulator.simulation.DefectModel;
import edu.simulator.simulation.FatigueModel;
import edu.simulator.simulation.ProductivityModel;
import edu.simulator.simulation.SimulationEngine;
import edu.simulator.simulation.WorkIntensity;
import edu.simulator.ui.JavaBridge;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SimulationEngineTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void defaultScenarioAndConfigurationLoad() {
        var scenario = ScenarioLoader.loadScenario("small-web-app");
        var configuration = ConfigurationLoader.loadDefault();

        assertEquals("small-web-app", scenario.getId());
        assertEquals(20, scenario.getDeadlineWeeks());
        assertEquals(new BigDecimal("400000"), scenario.getBudget());
        assertEquals(0.08, configuration.getQuality().getBaseDefectRate());
    }

    @Test
    void initialTeamChangesUpdateAuthoritativePayrollAndRejectNegativeCounts() throws Exception {
        JavaBridge bridge = new JavaBridge();
        JsonNode initial = objectMapper.readTree(bridge.getSetupState());
        BigDecimal initialPayroll = initial.get("weeklyPayroll").decimalValue();

        String updated = bridge.updateInitialTeam("""
                {"DEVELOPER":6,"QA_ENGINEER":2,"DEVOPS_ENGINEER":1,"PROJECT_MANAGER":1}
                """);
        JsonNode setup = objectMapper.readTree(updated);
        assertEquals(6, setup.at("/teamCounts/DEVELOPER").intValue());
        assertTrue(setup.get("weeklyPayroll").decimalValue().compareTo(initialPayroll) > 0);
        assertEquals(
                setup.get("weeklyPayroll").decimalValue().multiply(BigDecimal.valueOf(20)),
                setup.get("projectedPayroll").decimalValue());
        assertEquals(
                setup.get("budget").decimalValue().subtract(setup.get("projectedPayroll").decimalValue()),
                setup.get("contingency").decimalValue());

        assertThrows(IllegalArgumentException.class, () -> bridge.updateInitialTeam(
                "{\"DEVELOPER\":-1,\"QA_ENGINEER\":0,\"DEVOPS_ENGINEER\":0,\"PROJECT_MANAGER\":0}"));
    }

    @Test
    void selectedTeamAndSeedAreUsedWhenStarting() throws Exception {
        JavaBridge bridge = new JavaBridge();
        bridge.updateInitialTeam("""
                {"DEVELOPER":3,"QA_ENGINEER":1,"DEVOPS_ENGINEER":0,"PROJECT_MANAGER":1}
                """);
        String seed = Long.toString(Long.MAX_VALUE);
        JsonNode state = objectMapper.readTree(bridge.startSimulation("small-web-app", seed));

        assertEquals(0, state.get("week").intValue());
        assertEquals(seed, state.get("seed").textValue());
        assertEquals(3, state.at("/teamCounts/DEVELOPER").intValue());
        assertEquals(0, state.at("/teamCounts/DEVOPS_ENGINEER").intValue());
        assertThrows(IllegalStateException.class, () -> bridge.updateInitialTeam(
                "{\"DEVELOPER\":1,\"QA_ENGINEER\":1,\"DEVOPS_ENGINEER\":0,\"PROJECT_MANAGER\":1}"));
    }

    @Test
    void blankSeedIsGeneratedAndReported() throws Exception {
        JavaBridge bridge = new JavaBridge();
        JsonNode state = objectMapper.readTree(bridge.startSimulation("small-web-app", ""));
        assertTrue(state.has("seed"));
    }

    @Test
    void eachAdvanceRecordsTheEndOfThatWeekExactlyOnce() {
        SimulationEngine engine = newEngine(42L, null);
        assertEquals(0, engine.getCurrentWeek());
        assertTrue(engine.getHistory().isEmpty());

        for (int week = 1; week <= 5; week++) {
            engine.advanceWeek();
            assertEquals(week, engine.getCurrentWeek());
            assertEquals(week, engine.getHistory().size());
            assertEquals(week, engine.getHistory().get(week - 1).getWeek());
        }
    }

    @Test
    void sameSeedTeamAndDecisionsProduceIdenticalWeeklyResults() {
        Map<Role, Integer> team = teamCounts(5, 2, 1, 1);
        SimulationEngine first = newEngine(3107L, team);
        SimulationEngine second = newEngine(3107L, team);
        for (int week = 0; week < 5; week++) {
            if (week == 2) {
                first.setWorkIntensity(WorkIntensity.INCREASED);
                second.setWorkIntensity(WorkIntensity.INCREASED);
            }
            first.advanceWeek();
            second.advanceWeek();
        }

        assertEquals(observations(first), observations(second));
    }

    @Test
    void differentSeedsAreAcceptedAndStored() {
        Map<Role, Integer> largeTeam = teamCounts(100, 0, 0, 0);
        SimulationEngine first = newEngine(11L, largeTeam);
        SimulationEngine second = newEngine(12L, largeTeam);
        first.advanceWeek();
        second.advanceWeek();

        assertNotEquals(first.getHistory().getFirst().getTeamSize(),
                second.getHistory().getFirst().getTeamSize());
    }

    @Test
    void gameplayStateDoesNotSerializeHiddenProgressOrUnknownRework() throws Exception {
        SimulationEngine engine = newEngine(7L, null);
        engine.advanceWeek();
        String serialized = objectMapper.writeValueAsString(engine.getCurrentState());

        assertFalse(serialized.contains("trueProgress"));
        assertFalse(serialized.contains("unknownRework"));
        assertTrue(serialized.contains("perceivedProgress"));
    }

    @Test
    void costsComeFromConfigurationAndTeamCounts() {
        var configuration = ConfigurationLoader.loadDefault();
        CostModel costs = new CostModel();
        Map<Role, Integer> small = teamCounts(1, 0, 0, 0);
        Map<Role, Integer> larger = teamCounts(2, 0, 0, 0);

        assertEquals(0, BigDecimal.valueOf(2100)
                .compareTo(costs.calculateWeeklyPayroll(small, configuration)));
        assertEquals(0, BigDecimal.valueOf(4200)
                .compareTo(costs.calculateWeeklyPayroll(larger, configuration)));
    }

    @Test
    void fatigueAndDefectProbabilitiesRemainBounded() {
        var configuration = ConfigurationLoader.loadDefault();
        FatigueModel fatigueModel = new FatigueModel();
        double fatigued = 0.1;
        for (int week = 0; week < 30; week++) {
            fatigued = fatigueModel.updateFatigue(fatigued, WorkIntensity.CRUNCH, configuration);
        }
        assertTrue(fatigued >= 0.0 && fatigued <= 1.0);

        double probability = new DefectModel().defectProbability(
                0.08, fatigued, 1.0, 0.32, 1.0, configuration);
        assertTrue(probability >= 0.0 && probability <= 1.0);
    }

    @Test
    void workStocksAndProgressRejectInvalidNumericValues() {
        WorkState work = new WorkState();
        work.setTotalWork(ProjectPhase.DEVELOPMENT, Double.NaN);
        work.addUnknownRework(ProjectPhase.DEVELOPMENT, Double.POSITIVE_INFINITY);
        work.addKnownRework(ProjectPhase.DEVELOPMENT, 10.0);
        work.addKnownRework(ProjectPhase.DEVELOPMENT, Double.NaN);

        assertEquals(0.0, work.getTotalWork(ProjectPhase.DEVELOPMENT));
        assertEquals(0.0, work.getUnknownRework(ProjectPhase.DEVELOPMENT));
        assertEquals(10.0, work.getKnownRework(ProjectPhase.DEVELOPMENT));
        assertTrue(Double.isFinite(work.totalRemainingWork()));
        assertTrue(work.totalRemainingWork() >= 0.0);

        Project project = new Project("test", "Test", 10, BigDecimal.valueOf(1000));
        project.setPhaseProgress(ProjectPhase.DEVELOPMENT, Double.NaN);
        project.recordSchedulePressure(Double.POSITIVE_INFINITY);
        assertEquals(0.0, project.getPhaseProgress(ProjectPhase.DEVELOPMENT));
        assertTrue(Double.isFinite(project.getAverageSchedulePressure()));
        assertThrows(IllegalArgumentException.class, () -> project.setCurrentWeek(-1));
        assertThrows(IllegalArgumentException.class, () -> project.addSpent(BigDecimal.valueOf(-1)));
    }

    @Test
    void productivityAndHistoryValuesRemainFiniteAndNonNegative() {
        var configuration = ConfigurationLoader.loadDefault();
        Team team = new Team();
        team.addEmployee(new Employee(Role.DEVELOPER, ExperienceLevel.MID_LEVEL, 2100.0));
        team.addEmployee(new Employee(Role.QA_ENGINEER, ExperienceLevel.MID_LEVEL, 1900.0));

        var productivity = new ProductivityModel().calculate(
                team, WorkIntensity.SUSTAINABLE, 0.15, 0.2, configuration);
        assertTrue(productivity.totalEffectiveCapacity() > 0.0);
        assertTrue(productivity.developerEffectiveCapacity() > 0.0);

        SimulationEngine engine = newEngine(88L, null);
        for (int week = 0; week < 5; week++) {
            engine.advanceWeek();
        }
        for (var snapshot : engine.getHistory()) {
            assertTrue(Double.isFinite(snapshot.getPerceivedProgress()));
            assertTrue(Double.isFinite(snapshot.getTrueProgress()));
            assertTrue(Double.isFinite(snapshot.getAverageFatigue()));
            assertTrue(snapshot.getKnownRework() >= 0.0);
            assertTrue(snapshot.getUnknownRework() >= 0.0);
            assertNotNull(snapshot.getTeamCounts());
        }
    }

    private SimulationEngine newEngine(long seed, Map<Role, Integer> team) {
        var scenario = ScenarioLoader.loadScenario("small-web-app");
        var configuration = ConfigurationLoader.loadDefault();
        return team == null
                ? new SimulationEngine(scenario, configuration, seed)
                : new SimulationEngine(scenario, configuration, seed, team);
    }

    private Map<Role, Integer> teamCounts(int developers, int qa, int devops, int managers) {
        Map<Role, Integer> counts = new EnumMap<>(Role.class);
        counts.put(Role.DEVELOPER, developers);
        counts.put(Role.QA_ENGINEER, qa);
        counts.put(Role.DEVOPS_ENGINEER, devops);
        counts.put(Role.PROJECT_MANAGER, managers);
        return counts;
    }

    private List<String> observations(SimulationEngine engine) {
        List<String> result = new ArrayList<>();
        engine.getHistory().forEach(snapshot -> result.add(
                snapshot.getWeek() + "|" + snapshot.getSpent() + "|"
                        + snapshot.getPerceivedProgress() + "|" + snapshot.getTrueProgress() + "|"
                        + snapshot.getAverageFatigue() + "|" + snapshot.getTeamCounts() + "|"
                        + snapshot.getKnownRework() + "|" + snapshot.getUnknownRework()));
        return result;
    }
}
