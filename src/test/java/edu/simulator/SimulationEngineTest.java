package edu.simulator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.simulator.configuration.ConfigurationLoader;
import edu.simulator.configuration.ScenarioLoader;
import edu.simulator.decision.HiringDecision;
import edu.simulator.model.Employee;
import edu.simulator.model.ExperienceLevel;
import edu.simulator.model.Project;
import edu.simulator.model.ProjectPhase;
import edu.simulator.model.Role;
import edu.simulator.model.Team;
import edu.simulator.model.WorkState;
import edu.simulator.simulation.CostModel;
import edu.simulator.simulation.CoordinationModel;
import edu.simulator.simulation.DefectModel;
import edu.simulator.simulation.FatigueModel;
import edu.simulator.simulation.MentoringModel;
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
    void initialEmployeesUseConfiguredExperienceAndStartFullyIntegrated() {
        SimulationEngine engine = newEngine(17L, teamCounts(1, 1, 1, 1));
        assertTrue(engine.getTeam().onboardingEmployees().isEmpty());
        assertEquals(ExperienceLevel.MID_LEVEL,
                engine.getTeam().activeEmployees().stream()
                        .filter(employee -> employee.getRole() == Role.DEVELOPER)
                        .findFirst().orElseThrow().getExperienceLevel());
        assertEquals(ExperienceLevel.SENIOR,
                engine.getTeam().activeEmployees().stream()
                        .filter(employee -> employee.getRole() == Role.PROJECT_MANAGER)
                        .findFirst().orElseThrow().getExperienceLevel());
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
        Team nonDeveloperTeam = new Team();
        nonDeveloperTeam.addEmployee(new Employee(Role.QA_ENGINEER, ExperienceLevel.SENIOR, 2500));
        nonDeveloperTeam.addEmployee(new Employee(Role.DEVOPS_ENGINEER, ExperienceLevel.SENIOR, 3100));
        assertEquals(0.0, new ProductivityModel().calculate(
                nonDeveloperTeam, WorkIntensity.SUSTAINABLE, 0.0, 0.0, configuration)
                .developerEffectiveCapacity());

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

    @Test
    void hiringDelayControlsActivationPayrollAndOneTimeFee() {
        SimulationEngine engine = newEngine(721L, teamCounts(0, 0, 0, 0));
        engine.hire(new HiringDecision(Role.DEVELOPER, ExperienceLevel.MID_LEVEL, 1));

        assertEquals(0, engine.getTeam().totalCount());
        assertEquals(1, engine.getPendingHires().size());
        assertEquals(new BigDecimal("1500.0"), engine.getProject().getSpent());
        assertEquals(new BigDecimal("0.0"),
                engine.getTeamManagementState().weeklyPayroll());

        engine.advanceWeek();
        assertEquals(0, engine.getTeam().totalCount());
        assertEquals(1, engine.getPendingHires().getFirst().getWeeksUntilStart());
        assertEquals(new BigDecimal("0.0"), engine.getHistory().getFirst().getWeeklyPayroll());
        assertEquals(new BigDecimal("1500.0"), engine.getHistory().getFirst().getHiringCost());

        engine.advanceWeek();
        assertEquals(1, engine.getTeam().count(Role.DEVELOPER));
        assertTrue(engine.getPendingHires().isEmpty());
        assertEquals(new BigDecimal("2100.0"), engine.getHistory().get(1).getWeeklyPayroll());
        assertEquals(BigDecimal.ZERO, engine.getHistory().get(1).getHiringCost());
        assertTrue(engine.getTeam().onboardingEmployees().getFirst().getOnboardingProgress() > 0.4);
        assertTrue(engine.getTeam().onboardingEmployees().getFirst().getOnboardingProgress() < 1.0);
        assertEquals(new BigDecimal("1500.0"), engine.getTotalHiringCost());
    }

    @Test
    void immediateHireUsesConfiguredExperienceSalaryAndOnboardsOverConfiguredWeeks() {
        var scenario = ScenarioLoader.loadScenario("small-web-app");
        scenario.setScope(Map.of("DEVELOPMENT", 1_000_000.0));
        var config = ConfigurationLoader.loadDefault();
        config.getCosts().setJuniorHiringDelayWeeks(0);
        config.validate();
        SimulationEngine engine = new SimulationEngine(scenario, config, 722L,
                teamCounts(0, 0, 0, 1));

        engine.hire(new HiringDecision(Role.DEVELOPER, ExperienceLevel.JUNIOR, 1));

        Employee hire = engine.getTeam().activeEmployees().stream()
                .filter(employee -> employee.getRole() == Role.DEVELOPER)
                .findFirst().orElseThrow();
        assertEquals(1500.0, hire.getBaseWeeklyCost());
        assertEquals(4, hire.getOnboardingDurationWeeks());
        assertEquals(0.4, hire.getOnboardingProgress());
        assertEquals(1, engine.getTeamManagementState().onboardingEmployees());
        engine.advanceWeek();
        assertTrue(hire.getOnboardingProgress() > 0.4);
        assertEquals(engine.getTeam().onboardingEmployees().size(),
                engine.getHistory().getFirst().getOnboardingEmployees());
    }

    @Test
    void experienceLevelChangesProductivityAndExperienceSpecificPayroll() {
        var config = ConfigurationLoader.loadDefault();
        Team juniorTeam = new Team();
        juniorTeam.addEmployee(new Employee(Role.DEVELOPER, ExperienceLevel.JUNIOR, 1500));
        Team seniorTeam = new Team();
        seniorTeam.addEmployee(new Employee(Role.DEVELOPER, ExperienceLevel.SENIOR, 2900));
        ProductivityModel model = new ProductivityModel();

        assertTrue(model.calculate(seniorTeam, WorkIntensity.SUSTAINABLE, 0, 0, config)
                .developerEffectiveCapacity()
                > model.calculate(juniorTeam, WorkIntensity.SUSTAINABLE, 0, 0, config)
                .developerEffectiveCapacity());
        assertEquals(new BigDecimal("1500.0"),
                new CostModel().calculateWeeklyPayroll(juniorTeam, config));
        assertEquals(new BigDecimal("2900.0"),
                new CostModel().calculateWeeklyPayroll(seniorTeam, config));
    }

    @Test
    void mentoringCoverageImprovesOnboardingAndMentorsSpendCapacity() {
        var config = ConfigurationLoader.loadDefault();
        Team team = new Team();
        Employee junior = new Employee(Role.DEVELOPER, ExperienceLevel.JUNIOR, 1500);
        junior.setOnboardingProgress(0.4);
        junior.setOnboardingDurationWeeks(4);
        team.addEmployee(junior);
        MentoringModel model = new MentoringModel();
        var unsupported = model.calculate(team, 0, config);
        model.advanceOnboarding(team, unsupported, config);
        double unsupportedProgress = junior.getOnboardingProgress();

        Employee senior = new Employee(Role.DEVELOPER, ExperienceLevel.SENIOR, 2900);
        team.addEmployee(senior);
        var supported = model.calculate(team, 0, config);
        model.advanceOnboarding(team, supported, config);
        while (junior.getOnboardingProgress() < 1.0) {
            supported = model.calculate(team, 0, config);
            model.advanceOnboarding(team, supported, config);
        }

        assertTrue(supported.coverage() > unsupported.coverage());
        assertTrue(junior.getOnboardingProgress() > unsupportedProgress);
        assertEquals(1.0, junior.getOnboardingProgress());
        assertTrue(model.directProductivityModifier(senior, supported, config) < 1.0);
        assertTrue(supported.coverage() <= 1.0);
    }

    @Test
    void coordinationPenaltyIsBoundedAndReducedByProjectManagersAndSeniorStaff() {
        var config = ConfigurationLoader.loadDefault();
        CoordinationModel model = new CoordinationModel();
        double unmanaged = model.calculatePenalty(30, 0, 0, config);
        double managed = model.calculatePenalty(30, 1, 0, config);
        double seniorManaged = model.calculatePenalty(30, 1, 5, config);

        assertTrue(unmanaged <= config.getCoordination().getMaximumPenalty());
        assertTrue(managed < unmanaged);
        assertTrue(seniorManaged < managed);
        assertEquals(0.0, model.calculatePenalty(1, 0, config));
    }

    @Test
    void bridgeProvidesHiringOptionsAcceptsHireAndRejectsInvalidDecisions() throws Exception {
        JavaBridge bridge = new JavaBridge();
        bridge.startSimulation("small-web-app", "723");
        JsonNode before = objectMapper.readTree(bridge.getTeamManagementState());
        int developersBefore = objectMapper.readTree(bridge.getSimulationState())
                .at("/teamCounts/DEVELOPER").intValue();
        BigDecimal forecastBefore = objectMapper.readTree(bridge.getSimulationState())
                .get("forecastCost").decimalValue();
        assertTrue(before.at("/hiringOptions/DEVELOPER/MID_LEVEL/weeklySalary")
                .decimalValue().compareTo(BigDecimal.ZERO) > 0);

        JsonNode state = objectMapper.readTree(
                bridge.hireEmployee("DEVELOPER", "JUNIOR", 1));
        assertEquals(developersBefore, state.at("/teamCounts/DEVELOPER").intValue());
        assertTrue(state.get("forecastCost").decimalValue().compareTo(forecastBefore) > 0);
        JsonNode after = objectMapper.readTree(bridge.getTeamManagementState());
        assertEquals(1, after.get("pendingHires").intValue());
        assertEquals(1, after.at("/pending/0/weeksUntilStart").intValue());
        assertThrows(IllegalArgumentException.class,
                () -> bridge.hireEmployee("NO_SUCH_ROLE", "JUNIOR", 1));
        assertThrows(IllegalArgumentException.class,
                () -> bridge.hireEmployee("DEVELOPER", "JUNIOR", 0));
    }

    @Test
    void sameSeedAndHiringDecisionsProduceIdenticalStaffingSnapshots() {
        SimulationEngine first = newEngine(724L, teamCounts(3, 1, 0, 1));
        SimulationEngine second = newEngine(724L, teamCounts(3, 1, 0, 1));
        HiringDecision decision = new HiringDecision(Role.DEVELOPER, ExperienceLevel.SENIOR, 1);
        first.hire(decision);
        second.hire(decision);
        for (int week = 0; week < 5; week++) {
            first.advanceWeek();
            second.advanceWeek();
        }
        assertEquals(observations(first), observations(second));
        assertEquals(first.getHistory().get(4).getExperienceCounts(),
                second.getHistory().get(4).getExperienceCounts());
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
