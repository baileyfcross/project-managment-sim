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
import edu.simulator.model.TestingPriority;
import edu.simulator.model.WorkState;
import edu.simulator.model.WorkIntensity;
import edu.simulator.model.ConcurrencyPolicy;
import edu.simulator.model.EngineeringApproach;
import edu.simulator.model.TechnicalDebtPriority;
import edu.simulator.event.EventType;
import edu.simulator.event.ProjectEvent;
import edu.simulator.simulation.CostModel;
import edu.simulator.simulation.CoordinationModel;
import edu.simulator.simulation.ConcurrencyModel;
import edu.simulator.simulation.DefectModel;
import edu.simulator.simulation.FatigueModel;
import edu.simulator.simulation.ForecastModel;
import edu.simulator.simulation.MentoringModel;
import edu.simulator.simulation.MoraleModel;
import edu.simulator.simulation.NewWorkModel;
import edu.simulator.simulation.PhaseReadinessModel;
import edu.simulator.simulation.ProductivityModel;
import edu.simulator.simulation.ReworkModel;
import edu.simulator.simulation.SchedulePressureModel;
import edu.simulator.simulation.ScopeChangeModel;
import edu.simulator.simulation.SimulationEngine;
import edu.simulator.simulation.TestingModel;
import edu.simulator.simulation.TurnoverModel;
import edu.simulator.simulation.TechnicalDebtModel;
import edu.simulator.ui.JavaBridge;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
            advanceOneWeek(engine);
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
            advanceOneWeek(first);
            advanceOneWeek(second);
        }

        assertEquals(observations(first), observations(second));
    }

    @Test
    void differentSeedsAreAcceptedAndStored() {
        Map<Role, Integer> largeTeam = teamCounts(100, 0, 0, 0);
        SimulationEngine first = newEngine(11L, largeTeam);
        SimulationEngine second = newEngine(12L, largeTeam);
        advanceOneWeek(first);
        advanceOneWeek(second);

        assertNotEquals(first.getHistory().getFirst().getTeamSize(),
                second.getHistory().getFirst().getTeamSize());
    }

    @Test
    void gameplayStateDoesNotSerializeHiddenProgressOrUnknownRework() throws Exception {
        SimulationEngine engine = newEngine(7L, null);
        advanceOneWeek(engine);
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
            advanceOneWeek(engine);
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

        advanceOneWeek(engine);
        assertEquals(0, engine.getTeam().totalCount());
        assertEquals(1, engine.getPendingHires().getFirst().getWeeksUntilStart());
        assertEquals(new BigDecimal("0.0"), engine.getHistory().getFirst().getWeeklyPayroll());
        assertEquals(new BigDecimal("1500.0"), engine.getHistory().getFirst().getHiringCost());

        advanceOneWeek(engine);
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
        advanceOneWeek(engine);
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
            advanceOneWeek(first);
            advanceOneWeek(second);
        }
        assertEquals(observations(first), observations(second));
        assertEquals(first.getHistory().get(4).getExperienceCounts(),
                second.getHistory().get(4).getExperienceCounts());
    }

    @Test
    void zeroDeveloperCapacityDoesNotAttemptNewPhaseWork() {
        var config = ConfigurationLoader.loadDefault();
        WorkState work = new WorkState();
        work.setTotalWork(ProjectPhase.DEVELOPMENT, 100.0);

        var result = new NewWorkModel().performDevelopment(
                work, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0,
                WorkIntensity.SUSTAINABLE, config, new Random(1));

        assertEquals(0.0, result.totalAttempted());
        assertEquals(100.0, work.getBaseWorkRemaining(ProjectPhase.DEVELOPMENT));
        assertEquals(0.0, work.getTotalWorkAttemptedThisWeek());
    }

    @Test
    void attemptedWorkSplitsBetweenCorrectAndUnknownRework() {
        var config = ConfigurationLoader.loadDefault();
        config.getQuality().setBaseDefectRate(1.0);
        config.getQuality().setDefectCap(1.0);
        WorkState work = new WorkState();
        work.setTotalWork(ProjectPhase.DEVELOPMENT, 100.0);

        new NewWorkModel().performDevelopment(
                work, 100.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0,
                WorkIntensity.SUSTAINABLE, config, new Random(2));

        assertEquals(0.0, work.getBaseWorkRemaining(ProjectPhase.DEVELOPMENT));
        assertEquals(0.0, work.getCorrectWorkCompleted(ProjectPhase.DEVELOPMENT));
        assertEquals(100.0, work.getUnknownRework(ProjectPhase.DEVELOPMENT));
        assertEquals(100.0, work.getWorkAttemptedThisWeek(ProjectPhase.DEVELOPMENT));
        assertEquals(100.0, work.getDefectsCreatedThisWeek(ProjectPhase.DEVELOPMENT));
    }

    @Test
    void defectProbabilityAndSamplingAreBoundedAndSeeded() {
        var config = ConfigurationLoader.loadDefault();
        DefectModel model = new DefectModel();
        double capped = model.defectProbability(1.0, 1.0, 1.0, 1.0, 1.0, config);
        assertTrue(capped >= 0.0 && capped <= 1.0);
        assertEquals(model.sampleDefectiveWork(800.0, 0.25, new Random(42)),
                model.sampleDefectiveWork(800.0, 0.25, new Random(42)));

        WorkState lowWork = new WorkState();
        WorkState highWork = new WorkState();
        lowWork.setTotalWork(ProjectPhase.DEVELOPMENT, 100_000.0);
        highWork.setTotalWork(ProjectPhase.DEVELOPMENT, 100_000.0);
        config.getQuality().setBaseDefectRate(0.05);
        new NewWorkModel().performDevelopment(
                lowWork, 80_000.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0,
                WorkIntensity.SUSTAINABLE, config, new Random(6));
        config.getQuality().setBaseDefectRate(0.4);
        new NewWorkModel().performDevelopment(
                highWork, 80_000.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0,
                WorkIntensity.SUSTAINABLE, config, new Random(6));
        assertTrue(highWork.totalUnknownRework() > lowWork.totalUnknownRework());
    }

    @Test
    void qaInspectionIsLimitedByTestableWorkAndUnknownDefects() {
        var config = ConfigurationLoader.loadDefault();
        config.getQa().setBaseDetectionRate(1.0);
        WorkState noQa = new WorkState();
        noQa.addUnknownRework(ProjectPhase.DEVELOPMENT, 10.0);
        noQa.addTestableWork(ProjectPhase.DEVELOPMENT, 100.0);
        TestingModel model = new TestingModel();
        var idle = model.perform(noQa, 0.0, 0.0, TestingPriority.NORMAL, config, new Random(3));
        assertEquals(0.0, idle.workInspected());
        assertEquals(0.0, idle.defectsDiscovered());
        assertEquals(100.0, idle.backlog());

        WorkState limited = new WorkState();
        limited.addUnknownRework(ProjectPhase.DEVELOPMENT, 10.0);
        limited.addTestableWork(ProjectPhase.DEVELOPMENT, 100.0);
        var result = model.perform(limited, 1.0, 0.0, TestingPriority.HIGH, config, new Random(4));
        assertTrue(result.workInspected() <= 100.0);
        assertTrue(result.defectsDiscovered() <= 10.0);
        assertTrue(result.defectsDiscovered() > 0.0);
        assertTrue(limited.getKnownRework(ProjectPhase.DEVELOPMENT) <= 10.0);

        WorkState ordinary = new WorkState();
        WorkState highPriority = new WorkState();
        ordinary.addTestableWork(ProjectPhase.DEVELOPMENT, 500.0);
        highPriority.addTestableWork(ProjectPhase.DEVELOPMENT, 500.0);
        double normalInspected = model.perform(
                ordinary, 0.5, 0.0, TestingPriority.NORMAL, config, new Random(5)).workInspected();
        double highInspected = model.perform(
                highPriority, 0.5, 0.0, TestingPriority.HIGH, config, new Random(5)).workInspected();
        assertTrue(highInspected > normalInspected);
    }

    @Test
    void reworkConsumesKnownStockCanFailAgainAndCreatesRegressionDemand() {
        var config = ConfigurationLoader.loadDefault();
        WorkState work = new WorkState();
        work.addUnknownRework(ProjectPhase.DEVELOPMENT, 20.0);
        assertEquals(20.0, work.discoverDefects(ProjectPhase.DEVELOPMENT, 30.0));
        assertEquals(0.0, work.getUnknownRework(ProjectPhase.DEVELOPMENT));
        assertEquals(20.0, work.getKnownRework(ProjectPhase.DEVELOPMENT));
        config.getQuality().setReworkCreationRate(0.0);
        var fixed = new ReworkModel().perform(work, 50.0, 1.0, config, new Random(8));
        assertEquals(20.0, fixed.attempted());
        assertEquals(20.0, fixed.fixedCorrectly());
        assertEquals(0.0, work.getKnownRework(ProjectPhase.DEVELOPMENT));
        assertEquals(20.0, work.getCorrectWorkCompleted(ProjectPhase.DEVELOPMENT));
        assertEquals(5.0, work.getTestingBacklog(ProjectPhase.TESTING));

        work.addKnownRework(ProjectPhase.DEVELOPMENT, 7.0);
        config.getQuality().setReworkCreationRate(1.0);
        config.getQuality().setReworkDefectMultiplier(1.0);
        new ReworkModel().perform(work, 7.0, 1.0, config, new Random(9));
        assertEquals(0.0, work.getKnownRework(ProjectPhase.DEVELOPMENT));
        assertEquals(7.0, work.getUnknownRework(ProjectPhase.DEVELOPMENT));
        assertTrue(work.getKnownRework(ProjectPhase.DEVELOPMENT) >= 0.0);
    }

    @Test
    void workDefectQaRepairRegressionCycleRunsEndToEnd() {
        var config = ConfigurationLoader.loadDefault();
        config.getQuality().setBaseDefectRate(1.0);
        config.getQuality().setDefectCap(1.0);
        config.getQuality().setReworkDefectMultiplier(1.0);
        config.getQuality().setReworkCreationRate(0.0);
        config.getQa().setBaseDetectionRate(1.0);
        WorkState work = new WorkState();
        work.setTotalWork(ProjectPhase.DEVELOPMENT, 100.0);
        work.setTotalWork(ProjectPhase.TESTING, 100.0);

        new NewWorkModel().performDevelopment(
                work, 100.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0,
                WorkIntensity.SUSTAINABLE, config, new Random(17));
        assertEquals(100.0, work.getUnknownRework(ProjectPhase.DEVELOPMENT));

        TestingModel testingModel = new TestingModel();
        var testPass = testingModel.perform(
                work, 1.0, 0.0, TestingPriority.NORMAL, config, new Random(18));
        assertTrue(testPass.defectsDiscovered() > 0.0);
        assertTrue(work.totalKnownRework() > 0.0);
        assertTrue(work.totalUnknownRework() < 100.0);

        double backlogBeforeRepairRetest = work.getTotalTestingBacklog();
        ReworkModel.ReworkResult repairs = new ReworkModel().perform(
                work, work.totalKnownRework(), 1.0, config, new Random(19));
        assertTrue(repairs.fixedCorrectly() > 0.0);
        assertEquals(0.0, work.totalKnownRework());
        assertTrue(work.getTestingBacklog(ProjectPhase.TESTING) > 0.0);
        assertTrue(work.getTotalTestingBacklog() > backlogBeforeRepairRetest);

        testingModel.perform(work, 1.0, 0.0, TestingPriority.HIGH, config, new Random(20));
        assertTrue(work.getTotalTestingBacklog() < backlogBeforeRepairRetest
                + repairs.fixedCorrectly() * config.getQuality().getRegressionTestingFactor());
    }

    @Test
    void perceivedProgressAndForecastRespectHiddenInformationBoundary() throws Exception {
        Project project = new Project("test", "Test", 20, BigDecimal.valueOf(100_000));
        WorkState work = project.getWorkState();
        work.setTotalWork(ProjectPhase.DEVELOPMENT, 100.0);
        work.recordNewWork(ProjectPhase.DEVELOPMENT, 100.0, 20.0);
        assertEquals(1.0, project.calculatePerceivedProgress());
        assertEquals(0.8, project.calculateTrueProgress());
        int beforeDiscovery = new ForecastModel().estimateCompletionWeek(project, 100.0);

        work.discoverDefects(ProjectPhase.DEVELOPMENT, 20.0);
        assertEquals(0.8, project.calculatePerceivedProgress());
        assertEquals(0.8, project.calculateTrueProgress());
        int afterDiscovery = new ForecastModel().estimateCompletionWeek(project, 100.0);
        assertTrue(afterDiscovery > beforeDiscovery);

        SimulationEngine engine = newEngine(35L, null);
        advanceOneWeek(engine);
        String json = objectMapper.writeValueAsString(engine.getCurrentState());
        assertFalse(json.contains("trueProgress"));
        assertFalse(json.contains("unknownRework"));
        assertFalse(json.contains("defectProbability"));
        assertTrue(json.contains("phaseProgress"));
        assertTrue(json.contains("testingBacklogStatus"));
    }

    @Test
    void phaseReadinessOverlapsGraduallyAndDevopsDoesDeploymentWork() {
        var config = ConfigurationLoader.loadDefault();
        WorkState work = new WorkState();
        work.setTotalWork(ProjectPhase.REQUIREMENTS, 100.0);
        work.setTotalWork(ProjectPhase.DESIGN, 100.0);
        work.setTotalWork(ProjectPhase.DEVELOPMENT, 100.0);
        PhaseReadinessModel readiness = new PhaseReadinessModel();
        assertEquals(0.1, readiness.readiness(ProjectPhase.DESIGN, work, config));
        work.recordNewWork(ProjectPhase.REQUIREMENTS, 50.0, 0.0);
        assertTrue(readiness.readiness(ProjectPhase.DESIGN, work, config) > 0.1);
        assertEquals(0.1, readiness.readiness(ProjectPhase.DEVELOPMENT, work, config));
        work.recordNewWork(ProjectPhase.DESIGN, 50.0, 0.0);
        assertTrue(readiness.readiness(ProjectPhase.DEVELOPMENT, work, config) > 0.1);

        work.setTotalWork(ProjectPhase.DEPLOYMENT, 100.0);
        var model = new NewWorkModel();
        var noDevops = model.performDeployment(work, 0.0, 1.0, 1.0, 0.0,
                0.0, 0.0, WorkIntensity.SUSTAINABLE, config, new Random(10));
        assertEquals(0.0, noDevops.totalAttempted());
        var withDevops = model.performDeployment(work, 100.0, 1.0, 1.0, 0.0,
                0.0, 0.0, WorkIntensity.SUSTAINABLE, config, new Random(10));
        assertTrue(withDevops.totalAttempted() > 0.0);
    }

    @Test
    void developerHeavyTeamBuildsMoreTestBacklogAndHiddenDefects() {
        var scenario = ScenarioLoader.loadScenario("small-web-app");
        scenario.setScope(Map.of(
                "requirements", 0.0,
                "design", 0.0,
                "development", 40_000.0,
                "testing", 20_000.0,
                "deployment", 0.0));
        scenario.setBudget(BigDecimal.valueOf(2_000_000));
        var config = ConfigurationLoader.loadDefault();
        SimulationEngine balanced = new SimulationEngine(scenario, config, 411L,
                teamCounts(4, 6, 0, 0));
        SimulationEngine developerHeavy = new SimulationEngine(scenario, config, 411L,
                teamCounts(10, 1, 0, 0));
        for (int week = 0; week < 5; week++) {
            advanceOneWeek(balanced);
            advanceOneWeek(developerHeavy);
        }

        assertTrue(developerHeavy.getProject().calculatePerceivedPhaseProgress()
                .get(ProjectPhase.DEVELOPMENT)
                > balanced.getProject().calculatePerceivedPhaseProgress()
                .get(ProjectPhase.DEVELOPMENT));
        assertTrue(developerHeavy.getProject().getWorkState().getTotalTestingBacklog()
                > balanced.getProject().getWorkState().getTotalTestingBacklog());
        assertTrue(developerHeavy.getProject().getWorkState().totalUnknownRework()
                > balanced.getProject().getWorkState().totalUnknownRework());
    }

    @Test
    void workIntensityIncreasesImmediateCapacityAndOvertimeCostByConfiguredHours() {
        var config = ConfigurationLoader.loadDefault();
        Team team = new Team();
        team.addEmployee(new Employee(Role.DEVELOPER, ExperienceLevel.MID_LEVEL, 2100.0));
        ProductivityModel productivity = new ProductivityModel();
        double sustainable = productivity.calculate(
                team, WorkIntensity.SUSTAINABLE, 0.0, 0.0, config).developerEffectiveCapacity();
        double increased = productivity.calculate(
                team, WorkIntensity.INCREASED, 0.0, 0.0, config).developerEffectiveCapacity();
        double crunch = productivity.calculate(
                team, WorkIntensity.CRUNCH, 0.0, 0.0, config).developerEffectiveCapacity();
        CostModel costs = new CostModel();
        BigDecimal payroll = BigDecimal.valueOf(2100.0);

        assertTrue(sustainable < increased);
        assertTrue(increased < crunch);
        assertEquals(0.0, costs.calculateOvertimeCost(
                payroll, WorkIntensity.SUSTAINABLE, config).doubleValue());
        assertTrue(costs.calculateOvertimeCost(payroll, WorkIntensity.INCREASED, config)
                .compareTo(BigDecimal.ZERO) > 0);
        assertTrue(costs.calculateOvertimeCost(payroll, WorkIntensity.CRUNCH, config)
                .compareTo(costs.calculateOvertimeCost(payroll, WorkIntensity.INCREASED, config)) > 0);
        assertEquals(48, config.getWorkIntensity().hours(WorkIntensity.INCREASED));
        assertEquals(60, config.getWorkIntensity().hours(WorkIntensity.CRUNCH));
    }

    @Test
    void bridgeAcceptsWorkIntensityAndRejectsInvalidSettings() throws Exception {
        JavaBridge bridge = new JavaBridge();
        bridge.startSimulation("small-web-app", "812");
        JsonNode state = objectMapper.readTree(bridge.setWorkIntensity("CRUNCH"));

        assertEquals("CRUNCH", state.get("workIntensity").textValue());
        assertEquals(60, state.get("workIntensityHours").intValue());
        assertThrows(IllegalArgumentException.class, () -> bridge.setWorkIntensity("EXTREME"));
        assertThrows(IllegalArgumentException.class, () -> {
            var config = ConfigurationLoader.loadDefault();
            config.getWorkIntensity().setCrunchHours(48);
            config.validate();
        });
    }

    @Test
    void fatigueAccumulatesGraduallyRecoversSlowlyAndHasNonlinearEffects() {
        var config = ConfigurationLoader.loadDefault();
        FatigueModel model = new FatigueModel();
        assertEquals(0.0, model.updateFatigue(0.0, WorkIntensity.SUSTAINABLE, config));
        Employee increased = new Employee(Role.DEVELOPER, ExperienceLevel.MID_LEVEL, 2100);
        Employee crunch = new Employee(Role.DEVELOPER, ExperienceLevel.MID_LEVEL, 2100);
        for (int week = 0; week < 4; week++) {
            increased.setFatigue(model.updateFatigue(increased, WorkIntensity.INCREASED, 0.5, config));
            crunch.setFatigue(model.updateFatigue(crunch, WorkIntensity.CRUNCH, 0.5, config));
            increased.recordWorkIntensity(true);
            crunch.recordWorkIntensity(true);
        }
        assertTrue(increased.getFatigue() > 0.0);
        assertTrue(crunch.getFatigue() > increased.getFatigue());

        double recoveredOnce = model.updateFatigue(0.75, WorkIntensity.SUSTAINABLE, config);
        assertTrue(recoveredOnce < 0.75);
        assertTrue(recoveredOnce > 0.0);
        assertTrue((1.0 - model.productivityModifier(0.8, config))
                > 2.0 * (1.0 - model.productivityModifier(0.4, config)));
        assertTrue(model.defectModifier(0.8, config) > model.defectModifier(0.4, config));
        assertTrue(model.qaEffectivenessModifier(0.8, config)
                < model.qaEffectivenessModifier(0.4, config));
        assertEquals("Severe Burnout", model.category(0.9));
    }

    @Test
    void moraleRecoversGraduallyAndStressRaisesEmployeeTurnoverRisk() {
        var config = ConfigurationLoader.loadDefault();
        Employee employee = new Employee(Role.DEVELOPER, ExperienceLevel.MID_LEVEL, 2100);
        employee.setMorale(config.getMorale().getBaseline());
        MoraleModel morale = new MoraleModel();
        double healthyMorale = employee.getMorale();
        employee.setFatigue(0.9);
        for (int week = 0; week < 4; week++) {
            employee.setMorale(morale.updateMorale(employee, WorkIntensity.CRUNCH, 0.9, config));
            employee.recordWorkIntensity(true);
        }
        double stressedMorale = employee.getMorale();
        assertTrue(stressedMorale < healthyMorale);
        assertTrue(stressedMorale >= 0.0);

        employee.setFatigue(0.0);
        for (int week = 0; week < 4; week++) {
            employee.setMorale(morale.updateMorale(employee, WorkIntensity.SUSTAINABLE, 0.0, config));
            employee.recordWorkIntensity(false);
        }
        assertTrue(employee.getMorale() > stressedMorale);
        assertTrue(employee.getMorale() <= 1.0);

        Employee healthyEmployee = new Employee(Role.DEVELOPER, ExperienceLevel.MID_LEVEL, 2100);
        double healthyRisk = new TurnoverModel().departureProbability(healthyEmployee, 0.0, config);
        employee.setFatigue(1.0);
        employee.setMorale(0.0);
        employee.recordWorkIntensity(true);
        double stressedRisk = new TurnoverModel().departureProbability(employee, 1.0, config);
        assertTrue(healthyRisk < 0.01);
        assertTrue(stressedRisk > healthyRisk);
        assertTrue(stressedRisk <= config.getTurnover().getMaxRate());
    }

    @Test
    void turnoverUsesSeededIndividualDecisionsAndDepartedStaffStopContributing() {
        var config = ConfigurationLoader.loadDefault();
        Team first = new Team();
        Team second = new Team();
        for (int index = 0; index < 12; index++) {
            first.addEmployee(new Employee(Role.DEVELOPER, ExperienceLevel.MID_LEVEL, 2100));
            second.addEmployee(new Employee(Role.DEVELOPER, ExperienceLevel.MID_LEVEL, 2100));
        }
        config.getTurnover().setBaseRate(0.35);
        config.getTurnover().setMaxRate(0.35);
        TurnoverModel model = new TurnoverModel();
        List<Employee> firstDepartures = model.resolveDepartures(
                first, 0.0, config, new Random(90210));
        List<Employee> secondDepartures = model.resolveDepartures(
                second, 0.0, config, new Random(90210));

        assertEquals(firstDepartures.size(), secondDepartures.size());
        assertEquals(12 - firstDepartures.size(), first.totalCount());
        assertEquals(first.totalCount() * 2100.0,
                new CostModel().calculateWeeklyPayroll(first, config).doubleValue());

        Team allDepart = new Team();
        allDepart.addEmployee(new Employee(Role.DEVELOPER, ExperienceLevel.MID_LEVEL, 2100));
        config.getTurnover().setBaseRate(1.0);
        config.getTurnover().setMaxRate(1.0);
        model.resolveDepartures(allDepart, 0.0, config, new Random(1));
        assertEquals(0.0, new CostModel().calculateWeeklyPayroll(allDepart, config).doubleValue());
        assertEquals(0, new MentoringModel().calculate(allDepart, 0, config).capacity());
        assertEquals(0.0, new ProductivityModel().calculateForTeam(
                allDepart, WorkIntensity.SUSTAINABLE, 0.0, config,
                new MentoringModel().calculate(allDepart, 0, config)).totalEffectiveCapacity());
    }

    @Test
    void crunchReducesPerformanceAfterFatigueBuildsAndPreservesHiddenStateBoundary() throws Exception {
        var config = ConfigurationLoader.loadDefault();
        Team team = new Team();
        Employee developer = new Employee(Role.DEVELOPER, ExperienceLevel.MID_LEVEL, 2100);
        Employee qa = new Employee(Role.QA_ENGINEER, ExperienceLevel.MID_LEVEL, 1900);
        team.addEmployee(developer);
        team.addEmployee(qa);
        ProductivityModel model = new ProductivityModel();
        var mentoring = new MentoringModel().calculate(team, 0, config);
        double earlyCrunch = model.calculateForTeam(
                team, WorkIntensity.CRUNCH, 0.0, config, mentoring).developerEffectiveCapacity();
        FatigueModel fatigue = new FatigueModel();
        for (int week = 0; week < 12; week++) {
            developer.setFatigue(fatigue.updateFatigue(
                    developer, WorkIntensity.CRUNCH, 0.6, config));
            qa.setFatigue(fatigue.updateFatigue(qa, WorkIntensity.CRUNCH, 0.6, config));
            developer.recordWorkIntensity(true);
            qa.recordWorkIntensity(true);
        }
        double lateCrunch = model.calculateForTeam(
                team, WorkIntensity.CRUNCH, 0.0, config,
                new MentoringModel().calculate(team, 0, config)).developerEffectiveCapacity();
        assertTrue(lateCrunch < earlyCrunch);
        assertTrue(fatigue.qaEffectivenessModifier(qa.getFatigue(), config) < 1.0);
        DefectModel defects = new DefectModel();
        assertTrue(defects.discoveryProbability(1.0, TestingPriority.NORMAL, 0.8, config)
                < defects.discoveryProbability(1.0, TestingPriority.NORMAL, 0.0, config));
        assertTrue(defects.defectProbability(0.08, 0.8, 0.0, 0.0, 0.0, config)
                > defects.defectProbability(0.08, 0.0, 0.0, 0.0, 0.0, config));

        SimulationEngine engine = newEngine(351L, null);
        engine.setWorkIntensity(WorkIntensity.CRUNCH);
        advanceOneWeek(engine);
        JsonNode dto = objectMapper.readTree(objectMapper.writeValueAsString(engine.getCurrentState()));
        assertEquals("CRUNCH", dto.get("workIntensity").textValue());
        assertEquals(60, dto.get("workIntensityHours").intValue());
        assertTrue(dto.has("averageFatigueHealth"));
        assertTrue(dto.has("turnoverRisk"));
        assertFalse(dto.has("trueProgress"));
        assertFalse(dto.has("unknownRework"));
        assertEquals(WorkIntensity.CRUNCH, engine.getHistory().get(0).getWorkIntensity());
        assertTrue(engine.getHistory().get(0).getOvertimeCost().doubleValue() > 0.0);
        assertTrue(engine.getHistory().get(0).getMaximumFatigue() >= 0.0);
        assertTrue(engine.getHistory().get(0).getAverageMorale() >= 0.0);
        assertEquals(1.0, engine.getHistory().get(0).getAverageOvertimeStreak());
    }

    @Test
    void pendingHiresDoNotAccumulateFatigueBeforeJoining() {
        SimulationEngine engine = newEngine(418L, null);
        engine.hire(new HiringDecision(Role.DEVELOPER, ExperienceLevel.SENIOR, 1));
        Employee pending = engine.getPendingHires().get(0).getEmployee();
        engine.setWorkIntensity(WorkIntensity.CRUNCH);
        advanceOneWeek(engine);

        assertFalse(pending.isActive());
        assertEquals(0.0, pending.getFatigue());
        assertEquals(0, pending.getConsecutiveOvertimeWeeks());
    }

    @Test
    void seededCrunchComparisonShowsImmediateBenefitAndDelayedFatigue() {
        var scenario = ScenarioLoader.loadScenario("small-web-app");
        scenario.setScope(Map.of(
                "requirements", 0.0,
                "design", 0.0,
                "development", 30_000.0,
                "testing", 15_000.0,
                "deployment", 0.0));
        scenario.setBudget(BigDecimal.valueOf(5_000_000));
        var sustainableConfig = ConfigurationLoader.loadDefault();
        var crunchConfig = ConfigurationLoader.loadDefault();
        Map<Role, Integer> team = teamCounts(2, 2, 0, 0);
        SimulationEngine sustainable = new SimulationEngine(
                scenario, sustainableConfig, 711L, team);
        SimulationEngine crunch = new SimulationEngine(
                scenario, crunchConfig, 711L, team);
        sustainable.setWorkIntensity(WorkIntensity.SUSTAINABLE);
        crunch.setWorkIntensity(WorkIntensity.CRUNCH);
        advanceOneWeek(sustainable);
        advanceOneWeek(crunch);
        assertTrue(crunch.getProject().getWorkState().getTotalWorkAttemptedThisWeek()
                > sustainable.getProject().getWorkState().getTotalWorkAttemptedThisWeek());
        assertTrue(crunch.getHistory().get(0).getOvertimeCost().doubleValue() > 0.0);

        for (int week = 0; week < 5; week++) {
            advanceOneWeek(sustainable);
            advanceOneWeek(crunch);
        }
        assertTrue(crunch.getHistory().get(5).getAverageFatigue()
                > sustainable.getHistory().get(5).getAverageFatigue());
        assertTrue(crunch.getHistory().get(5).getAverageOvertimeStreak() >= 6.0);
    }

    @Test
    void controlledHighTurnoverRecordsDeparturesAndStopsFuturePayroll() {
        var scenario = ScenarioLoader.loadScenario("small-web-app");
        var config = ConfigurationLoader.loadDefault();
        config.getTurnover().setBaseRate(1.0);
        config.getTurnover().setMaxRate(1.0);
        SimulationEngine engine = new SimulationEngine(
                scenario, config, 77L, teamCounts(2, 1, 0, 0));
        advanceOneWeek(engine);

        var snapshot = engine.getHistory().get(0);
        assertEquals(3, snapshot.getEmployeesDeparted().size());
        assertEquals(0, engine.getTeam().totalCount());
        assertEquals(0.0, new CostModel().calculateWeeklyPayroll(engine.getTeam(), config).doubleValue());
        assertTrue(snapshot.getWeeklyPayroll().doubleValue() > 0.0);
        assertEquals(0, engine.getCurrentState().getTeamCounts().values().stream()
                .mapToInt(Integer::intValue).sum());
    }

    @Test
    void schedulePressureUsesTestingBacklogAndVisibleReworkOnly() {
        var config = ConfigurationLoader.loadDefault();
        Project project = new Project("stress", "Stress", 10, BigDecimal.valueOf(100_000));
        project.getWorkState().setTotalWork(ProjectPhase.DEVELOPMENT, 1_000.0);
        project.getWorkState().setTotalWork(ProjectPhase.TESTING, 1_000.0);
        SchedulePressureModel pressure = new SchedulePressureModel();
        double initial = pressure.calculate(project, 1.0, 1.0, 1.0,
                0.0, 0.0, config);
        project.getWorkState().addTestableWork(ProjectPhase.DEVELOPMENT, 100.0);
        double withBacklog = pressure.calculate(project, 1.0, 1.0, 1.0,
                0.0, project.getWorkState().getTotalTestingBacklog(), config);
        project.getWorkState().addKnownRework(ProjectPhase.DEVELOPMENT, 100.0);
        double withKnownRework = pressure.calculate(project, 1.0, 1.0, 1.0,
                project.getWorkState().totalKnownRework(),
                project.getWorkState().getTotalTestingBacklog(), config);
        project.getWorkState().addUnknownRework(ProjectPhase.DEVELOPMENT, 200.0);
        double withHiddenRework = pressure.calculate(project, 1.0, 1.0, 1.0,
                project.getWorkState().totalKnownRework(),
                project.getWorkState().getTotalTestingBacklog(), config);

        assertTrue(withBacklog > initial);
        assertTrue(withKnownRework > withBacklog);
        assertEquals(withKnownRework, withHiddenRework);
    }

    @Test
    void lateFeatureChangesConserveScopeAndCreateMoreReworkThanEarlyChanges() {
        var configuration = ConfigurationLoader.loadDefault();
        Project early = projectWithScope();
        Project late = projectWithScope();
        for (ProjectPhase phase : ProjectPhase.values()) {
            double currentScope = late.getWorkState().getTotalWork(phase);
            late.getWorkState().recordNewWork(phase, currentScope * 0.8, 0.0);
        }
        Map<ProjectPhase, Double> feature = Map.of(
                ProjectPhase.REQUIREMENTS, 20.0,
                ProjectPhase.DESIGN, 30.0,
                ProjectPhase.DEVELOPMENT, 100.0,
                ProjectPhase.TESTING, 40.0,
                ProjectPhase.DEPLOYMENT, 10.0);

        var earlyChange = new ScopeChangeModel().acceptFeature(
                early, feature, configuration.getPhaseFive());
        var lateChange = new ScopeChangeModel().acceptFeature(
                late, feature, configuration.getPhaseFive());

        assertEquals(early.getOriginalScopeTotal() + 200.0,
                early.getWorkState().totalScope(), 1.0e-8);
        assertEquals(early.getOriginalScopeTotal() + 200.0,
                earlyChange.addedScope() + early.getOriginalScopeTotal(), 1.0e-8);
        assertTrue(lateChange.reworkGenerated() > earlyChange.reworkGenerated());
        assertTrue(lateChange.reworkGenerated() >= 0.0);
        assertTrue(late.getWorkState().getBaseWorkRemaining(ProjectPhase.DEVELOPMENT)
                >= 20.0);
        assertTrue(late.getWorkState().getTotalTestingBacklog() > 0.0);
    }

    @Test
    void featureDecisionsBlockAdvancementAndOnlyAcceptanceExpandsCurrentScope() throws Exception {
        SimulationEngine rejected = featureRequestEngine(512L);
        rejected.advanceWeek();
        ProjectEvent request = rejected.getPendingEvents().get(0);
        assertEquals(EventType.CUSTOMER_FEATURE_REQUEST, request.getType());
        double originalScope = rejected.getProject().getWorkState().totalScope();
        assertThrows(IllegalStateException.class, rejected::advanceWeek);

        rejected.resolveEvent(request.getId(), "REJECT");
        assertEquals(originalScope, rejected.getProject().getWorkState().totalScope());
        assertTrue(rejected.getPendingEvents().isEmpty());
        assertTrue(rejected.getEvents().get(0).isResolved());
        assertEquals(1, rejected.getEventDecisions().size());
        assertEquals(1, rejected.getEvents().get(0).getEventResult().resolutionWeek());
        assertTrue(rejected.getEvents().get(0).getEventResult().internalEffects()
                .containsKey("eventCreatedWork"));
        assertThrows(IllegalStateException.class,
                () -> rejected.resolveEvent(request.getId(), "REJECT"));

        SimulationEngine accepted = featureRequestEngine(512L);
        accepted.advanceWeek();
        ProjectEvent acceptedRequest = accepted.getPendingEvents().get(0);
        double beforeForecast = accepted.getCurrentState().getForecastCost().doubleValue();
        double beforeScope = accepted.getProject().getWorkState().totalScope();
        accepted.resolveEvent(acceptedRequest.getId(), "ACCEPT");
        assertTrue(accepted.getProject().getWorkState().totalScope() > beforeScope);
        assertEquals(1, accepted.getCurrentState().getAcceptedFeatureCount());
        assertTrue(accepted.getCurrentState().getForecastCost().doubleValue() > beforeForecast);

        SimulationEngine deferred = featureRequestEngine(512L);
        deferred.advanceWeek();
        ProjectEvent deferredRequest = deferred.getPendingEvents().get(0);
        double deferredScope = deferred.getProject().getWorkState().totalScope();
        deferred.resolveEvent(deferredRequest.getId(), "DEFER");
        assertEquals(deferredScope, deferred.getProject().getWorkState().totalScope());
        assertEquals(1, deferred.getCurrentState().getDeferredFeatureCount());
        assertEquals(0, deferred.getCurrentState().getAcceptedFeatureCount());

        String gameplayJson = objectMapper.writeValueAsString(accepted.getCurrentState());
        assertFalse(gameplayJson.contains("featureWork"));
        assertFalse(gameplayJson.contains("customerValuePotential"));
        assertFalse(gameplayJson.contains("unknownRework"));
        assertFalse(gameplayJson.contains("trueProgress"));
        assertFalse(gameplayJson.contains("internalEffects"));
    }

    @Test
    void seededEventGenerationAndResolutionsAreReproducible() {
        SimulationEngine first = featureRequestEngine(8741L);
        SimulationEngine second = featureRequestEngine(8741L);
        for (int week = 0; week < 7; week++) {
            advanceOneWeek(first);
            advanceOneWeek(second);
        }
        assertEquals(first.getEvents().stream()
                        .map(event -> event.getId() + "|" + event.getType() + "|" + event.getWeek()).toList(),
                second.getEvents().stream()
                        .map(event -> event.getId() + "|" + event.getType() + "|" + event.getWeek()).toList());
        assertEquals(first.getEventDecisions(), second.getEventDecisions());
    }

    @Test
    void concurrencyAdjustsReadinessAndUncertaintyButNotRiskAfterUpstreamCompletion() {
        var configuration = ConfigurationLoader.loadDefault();
        WorkState work = new WorkState();
        work.setTotalWork(ProjectPhase.REQUIREMENTS, 100.0);
        work.setTotalWork(ProjectPhase.DESIGN, 100.0);
        ConcurrencyModel model = new ConcurrencyModel();

        double sequential = model.readiness(ProjectPhase.DESIGN, work,
                ConcurrencyPolicy.SEQUENTIAL, configuration.getPhaseFive());
        double moderate = model.readiness(ProjectPhase.DESIGN, work,
                ConcurrencyPolicy.MODERATE, configuration.getPhaseFive());
        double aggressive = model.readiness(ProjectPhase.DESIGN, work,
                ConcurrencyPolicy.AGGRESSIVE, configuration.getPhaseFive());
        double aggressiveRisk = model.dependencyUncertainty(ProjectPhase.DESIGN, work,
                ConcurrencyPolicy.AGGRESSIVE, 0.2, configuration.getPhaseFive());
        assertTrue(sequential < moderate && moderate < aggressive);
        assertTrue(aggressiveRisk > 0.0);

        work.recordNewWork(ProjectPhase.REQUIREMENTS, 100.0, 0.0);
        assertEquals(1.0, model.readiness(ProjectPhase.DESIGN, work,
                ConcurrencyPolicy.AGGRESSIVE, configuration.getPhaseFive()));
        assertEquals(0.0, model.dependencyUncertainty(ProjectPhase.DESIGN, work,
                ConcurrencyPolicy.AGGRESSIVE, 0.2, configuration.getPhaseFive()));
    }

    @Test
    void technicalDebtEffectsAreDelayedBoundedAndPayDownUsesCapacity() {
        var configuration = ConfigurationLoader.loadDefault();
        TechnicalDebtModel model = new TechnicalDebtModel();
        assertTrue(model.productivityModifier(0.8, configuration.getPhaseFive())
                < model.productivityModifier(0.2, configuration.getPhaseFive()));
        assertTrue(model.defectModifier(0.8, configuration.getPhaseFive())
                > model.defectModifier(0.2, configuration.getPhaseFive()));

        Project project = projectWithScope();
        project.setTechnicalDebt(0.6);
        var payDown = model.planPayDown(project, 1_000.0,
                TechnicalDebtPriority.PAY_DOWN, configuration.getPhaseFive());
        assertEquals(150.0, payDown.capacitySpent(), 1.0e-8);
        assertEquals(0.6, project.getTechnicalDebt(), 1.0e-8);
        project.setTechnicalDebt(project.getTechnicalDebt() - payDown.debtReduction());
        assertTrue(project.getTechnicalDebt() < 0.6);
        assertTrue(project.getTechnicalDebt() > 0.0);
        project.setTechnicalDebt(1.0);
        model.updateAtWeekEnd(project, EngineeringApproach.CUT_CORNERS,
                WorkIntensity.CRUNCH, ConcurrencyPolicy.AGGRESSIVE,
                1.0, 1.0, 1.0, configuration.getPhaseFive());
        assertTrue(project.getTechnicalDebt() <= 1.0);
        assertTrue(project.getTechnicalDebt() >= 0.0);
    }

    @Test
    void cutCornersTradeMoreImmediateWorkForHigherEndOfWeekDebt() {
        var scenario = ScenarioLoader.loadScenario("small-web-app");
        var balancedConfig = ConfigurationLoader.loadDefault();
        var cornersConfig = ConfigurationLoader.loadDefault();
        balancedConfig.getPhaseFive().getEvents().setBaseProbability(0.0);
        cornersConfig.getPhaseFive().getEvents().setBaseProbability(0.0);
        Map<Role, Integer> team = teamCounts(5, 2, 1, 1);
        SimulationEngine balanced = new SimulationEngine(scenario, balancedConfig, 997L, team);
        SimulationEngine corners = new SimulationEngine(scenario, cornersConfig, 997L, team);
        corners.setEngineeringApproach(EngineeringApproach.CUT_CORNERS);

        balanced.advanceWeek();
        corners.advanceWeek();

        assertTrue(corners.getHistory().get(0).getWorkAttemptedByPhase().values().stream()
                .mapToDouble(Double::doubleValue).sum()
                > balanced.getHistory().get(0).getWorkAttemptedByPhase().values().stream()
                .mapToDouble(Double::doubleValue).sum());
        assertTrue(corners.getHistory().get(0).getPhaseFive().technicalDebt()
                > balanced.getHistory().get(0).getPhaseFive().technicalDebt());
    }

    @Test
    void debtPayDownReducesFeatureCapacityWithoutFreeProductivity() {
        var scenario = ScenarioLoader.loadScenario("small-web-app");
        var normalConfig = ConfigurationLoader.loadDefault();
        var payDownConfig = ConfigurationLoader.loadDefault();
        normalConfig.getPhaseFive().getEvents().setBaseProbability(0.0);
        payDownConfig.getPhaseFive().getEvents().setBaseProbability(0.0);
        Map<Role, Integer> team = teamCounts(5, 2, 1, 1);
        SimulationEngine normal = new SimulationEngine(scenario, normalConfig, 1402L, team);
        SimulationEngine payDown = new SimulationEngine(scenario, payDownConfig, 1402L, team);
        normal.getProject().setTechnicalDebt(0.6);
        payDown.getProject().setTechnicalDebt(0.6);
        payDown.setTechnicalDebtPriority(TechnicalDebtPriority.PAY_DOWN);

        normal.advanceWeek();
        payDown.advanceWeek();

        double normalAttempted = normal.getHistory().get(0).getWorkAttemptedByPhase()
                .values().stream().mapToDouble(Double::doubleValue).sum();
        double payDownAttempted = payDown.getHistory().get(0).getWorkAttemptedByPhase()
                .values().stream().mapToDouble(Double::doubleValue).sum();
        assertTrue(payDownAttempted < normalAttempted);
        assertTrue(payDown.getProject().getTechnicalDebt() < normal.getProject().getTechnicalDebt());
    }

    private Project projectWithScope() {
        Project project = new Project("scope-test", "Scope test", 20, BigDecimal.valueOf(100_000));
        Map<ProjectPhase, Double> work = Map.of(
                ProjectPhase.REQUIREMENTS, 100.0,
                ProjectPhase.DESIGN, 200.0,
                ProjectPhase.DEVELOPMENT, 1_000.0,
                ProjectPhase.TESTING, 400.0,
                ProjectPhase.DEPLOYMENT, 100.0);
        work.forEach((phase, amount) -> {
            project.getWorkState().setTotalWork(phase, amount);
            project.recordOriginalScope(phase, amount);
        });
        return project;
    }

    private SimulationEngine featureRequestEngine(long seed) {
        var scenario = ScenarioLoader.loadScenario("small-web-app");
        var configuration = ConfigurationLoader.loadDefault();
        var settings = configuration.getPhaseFive().getEvents();
        settings.setBaseProbability(1.0);
        settings.setMinimumSpacingWeeks(1);
        settings.setCooldownWeeks(0);
        settings.setEventWeights(Map.of(EventType.CUSTOMER_FEATURE_REQUEST.name(), 1.0));
        configuration.getPhaseFive().getScope().setFeatureWorkFractions(Map.of(
                "REQUIREMENTS", 0.5,
                "DESIGN", 0.5,
                "DEVELOPMENT", 0.5,
                "TESTING", 0.5,
                "DEPLOYMENT", 0.5));
        return new SimulationEngine(scenario, configuration, seed,
                teamCounts(5, 2, 1, 1));
    }

    private SimulationEngine newEngine(long seed, Map<Role, Integer> team) {
        var scenario = ScenarioLoader.loadScenario("small-web-app");
        var configuration = ConfigurationLoader.loadDefault();
        return team == null
                ? new SimulationEngine(scenario, configuration, seed)
                : new SimulationEngine(scenario, configuration, seed, team);
    }

    private void advanceOneWeek(SimulationEngine engine) {
        for (var event : engine.getPendingEvents()) {
            String preferredChoice = switch (event.getType()) {
                case CUSTOMER_FEATURE_REQUEST -> "REJECT";
                case REQUIREMENTS_MISUNDERSTANDING -> "KEEP";
                case DEPENDENCY_PROBLEM -> "WORKAROUND";
                case FAILED_INTEGRATION, TECHNICAL_DEBT_ISSUE -> "DEFER";
                case SECURITY_VULNERABILITY -> "TEMPORARY_FIX";
                default -> event.getOptions().get(event.getOptions().size() - 1).id();
            };
            engine.resolveEvent(event.getId(), preferredChoice);
        }
        engine.advanceWeek();
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
                        + snapshot.getKnownRework() + "|" + snapshot.getUnknownRework() + "|"
                        + snapshot.getAverageMorale() + "|" + snapshot.getWorkIntensity() + "|"
                        + snapshot.getOvertimeCost() + "|" + snapshot.getEmployeesDeparted()));
        return result;
    }
}
