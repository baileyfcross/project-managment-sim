package edu.simulator.simulation;

import edu.simulator.configuration.ScenarioConfiguration;
import edu.simulator.configuration.SimulationConfiguration;
import edu.simulator.decision.HiringDecision;
import edu.simulator.event.EventType;
import edu.simulator.event.ProjectEvent;
import edu.simulator.model.Employee;
import edu.simulator.model.ExperienceLevel;
import edu.simulator.model.Project;
import edu.simulator.model.ProjectPhase;
import edu.simulator.model.PendingHire;
import edu.simulator.model.Role;
import edu.simulator.model.Team;
import edu.simulator.model.TestingPriority;
import edu.simulator.model.WeeklySnapshot;
import edu.simulator.model.WorkIntensity;
import edu.simulator.report.FinalProjectReport;
import edu.simulator.ui.SimulationStateDto;
import edu.simulator.ui.TeamManagementDto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SimulationEngine {
    private static final int RECENT_MESSAGE_LIMIT = 8;

    private final ScenarioConfiguration scenario;
    private final SimulationConfiguration config;
    private final Random random;
    private final long seed;
    private final Project project;
    private final Team team;
    private final List<WeeklySnapshot> history = new ArrayList<>();
    private final List<ProjectEvent> events = new ArrayList<>();
    private final List<PendingHire> pendingHires = new ArrayList<>();
    private final List<String> messages = new ArrayList<>();
    private double schedulePressure = 0.15;
    private WorkIntensity workIntensity = WorkIntensity.SUSTAINABLE;
    private TestingPriority testingPriority = TestingPriority.NORMAL;
    private boolean complete;
    private int totalTurnover;
    private int defectsReleased;
    private BigDecimal lastWeeklyCost = BigDecimal.ZERO;
    private BigDecimal lastOvertimeCost = BigDecimal.ZERO;
    private int departuresThisWeek;
    private BigDecimal hiringCostSinceSnapshot = BigDecimal.ZERO;
    private BigDecimal totalHiringCost = BigDecimal.ZERO;

    public SimulationEngine(ScenarioConfiguration scenario, SimulationConfiguration config, long seed) {
        this(scenario, config, seed, readScenarioTeam(scenario));
    }

    public SimulationEngine(ScenarioConfiguration scenario, SimulationConfiguration config,
                            long seed, Map<Role, Integer> initialTeam) {
        this.scenario = scenario;
        this.config = config;
        this.seed = seed;
        this.random = new Random(seed);
        this.project = new Project(scenario.getId(), scenario.getName(),
                scenario.getDeadlineWeeks(), scenario.getBudget());
        this.team = new Team();
        initializeScenarioWork();
        initializeTeam(initialTeam);
        messages.add("Project initialized. Advance from Week 0 to begin the first simulated week.");
    }

    public void initializeScenarioWork() {
        for (Map.Entry<String, Double> entry : scenario.getScope().entrySet()) {
            ProjectPhase phase = phaseFromKey(entry.getKey());
            project.getWorkState().setTotalWork(phase, entry.getValue());
        }
    }

    private void initializeTeam(Map<Role, Integer> counts) {
        for (Role role : Role.values()) {
            int count = counts.getOrDefault(role, 0);
            if (count < 0) {
                throw new IllegalArgumentException("Initial " + role + " count cannot be negative");
            }
            for (int index = 0; index < count; index++) {
                ExperienceLevel level = assignDefaultExperience(role);
                Employee employee = new Employee(role, level,
                        config.getCosts().weeklySalary(role, level));
                employee.setMorale(config.getMorale().getBaseline());
                employee.setOnboardingProgress(1.0);
                employee.setOnboardingDurationWeeks(onboardingWeeksFor(level));
                team.addEmployee(employee);
            }
        }
    }

    private static Map<Role, Integer> readScenarioTeam(ScenarioConfiguration scenario) {
        Map<Role, Integer> counts = new EnumMap<>(Role.class);
        for (Role role : Role.values()) {
            counts.put(role, 0);
        }
        if (scenario.getInitialTeam() == null) {
            return counts;
        }
        for (Map.Entry<String, Integer> entry : scenario.getInitialTeam().entrySet()) {
            counts.put(roleFromKey(entry.getKey()), entry.getValue());
        }
        return counts;
    }

    public void setWorkIntensity(WorkIntensity workIntensity) {
        if (workIntensity == null) {
            throw new IllegalArgumentException("Work intensity is required");
        }
        WorkIntensity previous = this.workIntensity;
        this.workIntensity = workIntensity;
        if (previous != workIntensity && workIntensity == WorkIntensity.SUSTAINABLE
                && previous != WorkIntensity.SUSTAINABLE) {
            addMessage("Returning to sustainable hours is allowing the team to recover.");
        } else if (previous != workIntensity && workIntensity == WorkIntensity.CRUNCH) {
            addMessage("Crunch hours increase short-term effort, but sustained overtime raises team risk.");
        } else if (previous != workIntensity && workIntensity == WorkIntensity.INCREASED) {
            addMessage("Increased hours raise short-term effort and may accumulate fatigue.");
        }
    }

    public void setTestingPriority(TestingPriority testingPriority) {
        if (testingPriority == null) {
            throw new IllegalArgumentException("Testing priority is required");
        }
        this.testingPriority = testingPriority;
    }

    public Project getProject() {
        return project;
    }

    public Team getTeam() {
        return team;
    }

    public int getCurrentWeek() {
        return project.getCurrentWeek();
    }

    public long getSeed() {
        return seed;
    }

    public boolean isComplete() {
        return complete;
    }

    public List<WeeklySnapshot> getHistory() {
        return List.copyOf(history);
    }

    public List<ProjectEvent> getEvents() {
        return List.copyOf(events);
    }

    public List<PendingHire> getPendingHires() {
        return List.copyOf(pendingHires);
    }

    public BigDecimal getTotalHiringCost() {
        return totalHiringCost;
    }

    public void hire(HiringDecision decision) {
        if (complete) {
            throw new IllegalStateException("Cannot hire after the project has ended");
        }
        int totalPeople = team.totalCount() + pendingHires.size();
        if (totalPeople + decision.quantity() > 60) {
            throw new IllegalArgumentException("The active team and pending hires cannot exceed 60 people");
        }

        CoordinationModel coordinationModel = new CoordinationModel();
        String oldCoordination = coordinationModel.healthLabel(
                coordinationModel.calculatePenalty(team, config));
        String oldMentoring = new MentoringModel().calculate(team, pendingHires.size(), config).loadLabel();
        int delay = config.getCosts().hiringDelayWeeks(decision.experienceLevel());
        BigDecimal perHireCost = new CostModel().hiringCost(decision.experienceLevel(), config);
        BigDecimal totalCost = perHireCost.multiply(BigDecimal.valueOf(decision.quantity()));
        for (int index = 0; index < decision.quantity(); index++) {
            Employee employee = new Employee(decision.role(), decision.experienceLevel(),
                    config.getCosts().weeklySalary(decision.role(), decision.experienceLevel()));
            employee.setMorale(config.getMorale().getBaseline());
            employee.setOnboardingDurationWeeks(onboardingWeeksFor(decision.experienceLevel()));
            employee.setOnboardingProgress(config.getOnboarding().getInitialEffectiveness());
            employee.setOnboardingState(new MentoringModel().stateFor(employee.getOnboardingProgress()));
            if (delay == 0) {
                team.addEmployee(employee);
            } else {
                employee.setActive(false);
                pendingHires.add(new PendingHire(employee, delay));
            }
        }
        project.addSpent(totalCost);
        hiringCostSinceSnapshot = hiringCostSinceSnapshot.add(totalCost);
        totalHiringCost = totalHiringCost.add(totalCost);

        String roleName = decision.role().name().replace('_', ' ').toLowerCase();
        String experienceName = decision.experienceLevel().name().replace('_', '-').toLowerCase();
        addMessage(decision.quantity() + " " + experienceName + " " + roleName
                + (decision.quantity() == 1 ? " hired." : "s hired.")
                + (delay == 0 ? " They joined and began onboarding."
                : " They will join in " + delay + (delay == 1 ? " week." : " weeks.")));

        String newCoordination = coordinationModel.healthLabel(
                coordinationModel.calculatePenalty(team, config));
        if (!newCoordination.equals(oldCoordination)) {
            addMessage("Coordination is now " + newCoordination.toLowerCase()
                    + " as the team expands.");
        }
        String newMentoring = new MentoringModel()
                .calculate(team, pendingHires.size(), config).loadLabel();
        if (isHigherMentoringLoad(newMentoring, oldMentoring)) {
            addMessage("Mentoring demand is now " + newMentoring.toLowerCase()
                    + " because of new team members.");
        }
    }

    public SimulationStateDto getCurrentState() {
        CostModel costModel = new CostModel();
        BigDecimal payroll = costModel.calculateWeeklyPayroll(team, config);
        BigDecimal forecastOvertime = costModel.calculateOvertimeCost(payroll, workIntensity, config);
        ProductivityModel.ProductivityResult productivity = calculateProductivity();
        int estimatedCompletionWeek = new ForecastModel().estimateCompletionWeek(
                project, productivity.developerEffectiveCapacity(),
                productivity.qaEffectiveCapacity()
                        * config.getTesting().capacityFactor(testingPriority),
                productivity.devopsEffectiveCapacity(),
                project.getWorkState().getTotalTestingBacklog());
        int estimatedWeeksToFinish = estimatedCompletionWeek < 0
                ? Math.max(0, scenario.getDeadlineWeeks() - project.getCurrentWeek())
                : Math.max(0, estimatedCompletionWeek - project.getCurrentWeek());
        BigDecimal projectedCost = payroll.add(forecastOvertime)
                .multiply(BigDecimal.valueOf(estimatedWeeksToFinish));
        for (PendingHire pendingHire : pendingHires) {
            int paidWeeks = Math.max(0, estimatedWeeksToFinish - pendingHire.getWeeksUntilStart());
            BigDecimal hirePayroll = BigDecimal.valueOf(pendingHire.getEmployee().getBaseWeeklyCost())
                    .multiply(BigDecimal.valueOf(paidWeeks));
            projectedCost = projectedCost.add(hirePayroll)
                    .add(costModel.calculateOvertimeCost(hirePayroll, workIntensity, config));
        }
        BigDecimal forecastCost = project.getSpent().add(projectedCost);
        return new SimulationStateDto(
                project.getCurrentWeek(),
                seed,
                scenario.getDeadlineWeeks(),
                scenario.getBudget(),
                project.getSpent(),
                project.getRemainingBudget(),
                lastWeeklyCost,
                forecastCost,
                estimatedCompletionWeek,
                project.calculatePerceivedProgress(),
                roleCounts(),
                scheduleHealthLabel(),
                budgetHealthLabel(),
                qualityHealthLabel(),
                moraleHealthLabel(),
                messages,
                workIntensity,
                complete,
                phaseProgressForUi(),
                project.getWorkState().totalKnownRework(),
                project.getWorkState().getTotalDefectsDiscoveredThisWeek(),
                new TestingModel().backlogStatus(
                        project.getWorkState().getTotalTestingBacklog(), config),
                testingPriority,
                        productivity.qaEffectiveCapacity(),
                        config.getWorkIntensity().hours(workIntensity),
                        fatigueHealthLabel(),
                        new TurnoverModel().riskCategory(team, schedulePressure, config),
                        departuresThisWeek,
                        lastOvertimeCost
        );
    }

    public TeamManagementDto getTeamManagementState() {
        Map<String, Map<String, Integer>> experienceCounts = new HashMap<>();
        team.toExperienceCounts().forEach((role, counts) -> {
            Map<String, Integer> levels = new HashMap<>();
            counts.forEach((experience, count) -> levels.put(experience.name(), count));
            experienceCounts.put(role.name(), Map.copyOf(levels));
        });

        List<TeamManagementDto.OnboardingEmployee> onboarding = team.onboardingEmployees().stream()
                .map(employee -> new TeamManagementDto.OnboardingEmployee(
                        employee.getRole().name(), employee.getExperienceLevel().name(),
                        employee.getOnboardingState().name()))
                .toList();
        List<TeamManagementDto.PendingHire> pending = pendingHires.stream()
                .map(hire -> new TeamManagementDto.PendingHire(
                        hire.getEmployee().getRole().name(),
                        hire.getEmployee().getExperienceLevel().name(),
                        hire.getWeeksUntilStart()))
                .toList();
        Map<String, Map<String, TeamManagementDto.HiringOption>> options = new HashMap<>();
        CostModel costModel = new CostModel();
        for (Role role : Role.values()) {
            Map<String, TeamManagementDto.HiringOption> levels = new HashMap<>();
            for (ExperienceLevel experience : ExperienceLevel.values()) {
                levels.put(experience.name(), new TeamManagementDto.HiringOption(
                        costModel.weeklyRate(role, experience, config),
                        costModel.hiringCost(experience, config),
                        config.getCosts().hiringDelayWeeks(experience),
                        onboardingWeeksFor(experience)));
            }
            options.put(role.name(), Map.copyOf(levels));
        }

        MentoringModel.MentoringResult mentoring =
                new MentoringModel().calculate(team, pendingHires.size(), config);
        CoordinationModel coordination = new CoordinationModel();
        String coordinationHealth = coordination.healthLabel(
                coordination.calculatePenalty(team, config));
        BigDecimal payroll = costModel.calculateWeeklyPayroll(team, config);
        return new TeamManagementDto(
                team.totalCount(),
                onboarding.size(),
                pendingHires.size(),
                Map.copyOf(experienceCounts),
                onboarding,
                pending,
                Map.copyOf(options),
                payroll,
                getCurrentState().getForecastCost(),
                totalHiringCost,
                mentoring.loadLabel(),
                coordinationHealth
        );
    }

    /**
     * Simulates the next week in a fixed order and records exactly one snapshot
     * representing that week's end.
     */
    public void advanceWeek() {
        if (complete) {
            return;
        }

        int simulatedWeek = project.getCurrentWeek() + 1;
        project.setCurrentWeek(simulatedWeek);
        project.getWorkState().beginWeek();
        departuresThisWeek = 0;
        String previousFatigueCategory = new FatigueModel().category(averageFatigue());
        String previousMoraleCategory = moraleHealthLabel();
        double previousOvertimeStreak = averageOvertimeStreak();
        String previousBacklogStatus = new TestingModel().backlogStatus(
                project.getWorkState().getTotalTestingBacklog(), config);
        double previousKnownRework = project.getWorkState().totalKnownRework();
        activatePendingHires(simulatedWeek);
        for (Employee employee : team.activeEmployees()) {
            employee.incrementWeek();
        }
        MentoringModel mentoringModel = new MentoringModel();
        MentoringModel.MentoringResult mentoring =
                mentoringModel.calculate(team, pendingHires.size(), config);
        int onboardedCount = mentoringModel.advanceOnboarding(team, mentoring, config);
        if (onboardedCount > 0) {
            addMessage(onboardedCount + (onboardedCount == 1
                    ? " employee completed onboarding."
                    : " employees completed onboarding."));
        }
        mentoring = mentoringModel.calculate(team, pendingHires.size(), config);
        double coordinationPenalty = new CoordinationModel().calculatePenalty(team, config);
        ProductivityModel.ProductivityResult productivity = new ProductivityModel().calculateForTeam(
                team, workIntensity, schedulePressure, config, mentoring);
        double developerWorkCapacity =
                productivity.developerEffectiveCapacity() * ProductivityModel.WORK_UNITS_PER_CAPACITY;
        int workingTeamSize = team.totalCount();
        WorkAllocationModel.Allocation allocation = new WorkAllocationModel().allocate(
                developerWorkCapacity, project.getWorkState().totalKnownRework(), config);
        DefectModel defectModel = new DefectModel();
        double developerDefectRate = defectModel.defectProbability(
                ProjectPhase.DEVELOPMENT, developerExperienceModifier(), averageDeveloperOnboardingDeficit(),
                mentoring.coverage(), averageFatigue(Role.DEVELOPER), coordinationPenalty, schedulePressure,
                workIntensity, project.getWorkState(), config);
        ReworkModel.ReworkResult rework = new ReworkModel().perform(
                project.getWorkState(), allocation.reworkCapacity(), developerDefectRate, config, random);
        new NewWorkModel().performDevelopment(
                project.getWorkState(), allocation.newWorkCapacity() + rework.unusedCapacity(),
                developerExperienceModifier(), averageDeveloperOnboardingDeficit(),
                mentoring.coverage(), averageFatigue(Role.DEVELOPER), coordinationPenalty, schedulePressure,
                workIntensity, config, random);
        new NewWorkModel().performDeployment(
                project.getWorkState(),
                productivity.devopsEffectiveCapacity() * ProductivityModel.WORK_UNITS_PER_CAPACITY,
                roleExperienceModifier(Role.DEVOPS_ENGINEER), mentoring.coverage(),
                averageFatigue(Role.DEVOPS_ENGINEER), coordinationPenalty, schedulePressure,
                workIntensity, config, random);
        TestingModel.TestingResult testing = new TestingModel().perform(
                project.getWorkState(), productivity.qaEffectiveCapacity(),
                averageFatigue(Role.QA_ENGINEER),
                testingPriority, config, random);
        if (testing.defectsDiscovered() >= 1.0) {
            addMessage("QA discovered " + Math.round(testing.defectsDiscovered())
                    + " defects requiring rework.");
        }
        if (!testing.backlogStatus().equals(previousBacklogStatus)) {
            addMessage("Testing backlog is " + testing.backlogStatus().toLowerCase() + ".");
        }
        if (rework.attempted() >= 1.0) {
            addMessage("Developers completed " + Math.round(rework.attempted())
                    + " units of known rework.");
        }
        if (rework.fixedCorrectly() > 0.0 && config.getQuality().getRegressionTestingFactor() > 0.0) {
            addMessage("Regression testing demand increased after recent fixes.");
        }
        if (previousKnownRework == 0.0 && project.getWorkState().totalKnownRework() > 0.0) {
            addMessage("Known rework is affecting the completion forecast.");
        }

        schedulePressure = new SchedulePressureModel().calculate(
                project, productivity.developerEffectiveCapacity(),
                productivity.qaEffectiveCapacity()
                        * config.getTesting().capacityFactor(testingPriority),
                productivity.devopsEffectiveCapacity(),
                project.getWorkState().totalKnownRework(),
                project.getWorkState().getTotalTestingBacklog(), config);
        project.recordSchedulePressure(schedulePressure);
        FatigueModel fatigueModel = new FatigueModel();
        MoraleModel moraleModel = new MoraleModel();
        for (Employee employee : team.activeEmployees()) {
            employee.setFatigue(fatigueModel.updateFatigue(
                    employee, workIntensity, schedulePressure, config));
            employee.setMorale(moraleModel.updateMorale(
                    employee, workIntensity, schedulePressure, config));
            employee.recordWorkIntensity(workIntensity != WorkIntensity.SUSTAINABLE);
        }
        if (previousOvertimeStreak < 3.0 && averageOvertimeStreak() >= 3.0) {
            addMessage("The team has worked overtime for three consecutive weeks.");
        }
        String currentFatigueCategory = new FatigueModel().category(averageFatigue());
        if (!currentFatigueCategory.equals(previousFatigueCategory)) {
            String message = "Average team fatigue is now " + currentFatigueCategory.toLowerCase();
            if (currentFatigueCategory.equals("Burnout Risk")
                    || currentFatigueCategory.equals("Severe Burnout")) {
                message += ", reducing productivity and QA effectiveness";
            }
            addMessage(message + ".");
        }
        String currentMoraleCategory = moraleHealthLabel();
        if (!currentMoraleCategory.equals(previousMoraleCategory)) {
            addMessage("Team morale is now " + currentMoraleCategory.toLowerCase() + ".");
        }
        CostModel costModel = new CostModel();
        BigDecimal payroll = costModel.calculateWeeklyPayroll(team, config);
        lastOvertimeCost = costModel.calculateOvertimeCost(payroll, workIntensity, config);
        List<Employee> departures = new TurnoverModel().resolveDepartures(
                team, schedulePressure, config, random);
        departuresThisWeek = departures.size();
        totalTurnover += departuresThisWeek;
        if (!departures.isEmpty()) {
            team.removeInactiveEmployees();
            for (Employee employee : departures) {
                addMessage(employee.getExperienceLevel().name().replace('_', '-')
                        + " " + employee.getRole().name().replace('_', ' ').toLowerCase()
                        + " left the project.");
            }
            events.add(new ProjectEvent(EventType.EMPLOYEE_RESIGNATION,
                    departuresThisWeek + " employees exited the project in Week " + simulatedWeek + ".",
                    simulatedWeek));
        }
        MentoringModel.MentoringResult endingMentoring =
                mentoringModel.calculate(team, pendingHires.size(), config);
        double endingCoordinationPenalty = new CoordinationModel().calculatePenalty(team, config);

        BigDecimal hiringCost = hiringCostSinceSnapshot;
        lastWeeklyCost = payroll.add(lastOvertimeCost).add(hiringCost);
        project.addSpent(payroll.add(lastOvertimeCost));

        if (project.getWorkState().isReleaseReady()) {
            complete = true;
            defectsReleased = (int) Math.round(project.getWorkState().totalUnknownRework());
            addMessage("The project reached a release-ready state.");
        } else if (scenario.isBudgetFailureAllowed()
                && project.getSpent().compareTo(scenario.getBudget()) > 0) {
            complete = true;
            addMessage("The project exceeded its available budget and was closed.");
            events.add(new ProjectEvent(EventType.PERFORMANCE_PROBLEM,
                    "Budget overrun ended the project in Week " + simulatedWeek + ".",
                    simulatedWeek));
        } else if (simulatedWeek >= scenario.getDeadlineWeeks()) {
            complete = true;
            addMessage("The project missed its deadline with work still unresolved.");
            events.add(new ProjectEvent(EventType.PERFORMANCE_PROBLEM,
                    "Deadline missed in Week " + simulatedWeek + ".",
                    simulatedWeek));
        }

        double averageProductivity = workingTeamSize == 0
                ? 0.0 : productivity.totalEffectiveCapacity() / workingTeamSize;
        history.add(new WeeklySnapshot(
                simulatedWeek,
                project.getSpent(),
                project.getRemainingBudget(),
                lastWeeklyCost,
                project.calculatePerceivedProgress(),
                project.calculateTrueProgress(),
                team.totalCount(),
                team.toRoleCounts(),
                team.toExperienceCounts(),
                team.onboardingEmployees().size(),
                pendingHires.size(),
                averageOnboardingEffectiveness(),
                endingMentoring.load(),
                endingMentoring.coverage(),
                endingCoordinationPenalty,
                payroll,
                hiringCost,
                averageProductivity,
                averageFatigue(),
                project.getWorkState().totalKnownRework(),
                project.getWorkState().totalUnknownRework(),
                new ForecastModel().estimateCompletionWeek(
                        project, productivity.developerEffectiveCapacity(),
                        productivity.qaEffectiveCapacity()
                                * config.getTesting().capacityFactor(testingPriority),
                        productivity.devopsEffectiveCapacity(),
                        project.getWorkState().getTotalTestingBacklog()),
                schedulePressure,
                messages,
                project.getWorkState().phaseWorkAttemptedThisWeek(),
                project.getWorkState().phaseCorrectWorkCompleted(),
                project.getWorkState().phaseUnknownRework(),
                project.getWorkState().phaseKnownRework(),
                project.getWorkState().phaseReworkCompleted(),
                project.getWorkState().phaseDefectsCreatedThisWeek(),
                project.getWorkState().phaseDefectsDiscoveredThisWeek(),
                project.getWorkState().getTotalTestingBacklog(),
                productivity.qaEffectiveCapacity(),
                complete ? defectsReleased : 0,
                qualityHealthLabel(),
                testing.backlogStatus(),
                testingPriority,
                workIntensity,
                maximumFatigue(),
                averageMorale(),
                lastOvertimeCost,
                averageOvertimeStreak(),
                new TurnoverModel().riskCategory(team, schedulePressure, config),
                departures.stream().map(employee -> employee.getExperienceLevel().name()
                        + " " + employee.getRole().name()).toList()
        ));
        hiringCostSinceSnapshot = BigDecimal.ZERO;
    }

    public FinalProjectReport generateFinalReport() {
        double customerValue = clamp(0.9 - defectsReleased / 100.0
                + project.calculateTrueProgress() * 0.2);
        return new FinalProjectReport(
                project.getCurrentWeek(),
                scenario.getDeadlineWeeks(),
                project.getSpent(),
                scenario.getBudget(),
                defectsReleased,
                totalTurnover,
                averageFatigue(),
                averageMorale(),
                customerValue
        );
    }

    public double productivityForUi() {
        return calculateProductivity().totalEffectiveCapacity();
    }

    private ProductivityModel.ProductivityResult calculateProductivity() {
        MentoringModel.MentoringResult mentoring =
                new MentoringModel().calculate(team, pendingHires.size(), config);
        return new ProductivityModel().calculateForTeam(
                team, workIntensity, schedulePressure, config, mentoring);
    }

    private double averageFatigue() {
        return team.activeEmployees().stream().mapToDouble(Employee::getFatigue)
                .average().orElse(0.0);
    }

    private double averageFatigue(Role role) {
        return team.activeEmployees().stream()
                .filter(employee -> employee.getRole() == role)
                .mapToDouble(Employee::getFatigue).average().orElse(0.0);
    }

    private double maximumFatigue() {
        return team.activeEmployees().stream().mapToDouble(Employee::getFatigue)
                .max().orElse(0.0);
    }

    private double averageMorale() {
        return team.activeEmployees().stream().mapToDouble(Employee::getMorale)
                .average().orElse(0.0);
    }

    private double averageOvertimeStreak() {
        return team.activeEmployees().stream()
                .mapToInt(Employee::getConsecutiveOvertimeWeeks)
                .average().orElse(0.0);
    }

    private Map<String, Integer> roleCounts() {
        Map<String, Integer> counts = new HashMap<>();
        for (Role role : Role.values()) {
            counts.put(role.name(), team.count(role));
        }
        return counts;
    }

    private void addMessage(String message) {
        messages.add(message);
        while (messages.size() > RECENT_MESSAGE_LIMIT) {
            messages.remove(0);
        }
    }

    private int onboardingWeeksFor(ExperienceLevel level) {
        return switch (level) {
            case JUNIOR -> config.getOnboarding().getJuniorWeeks();
            case MID_LEVEL -> config.getOnboarding().getMidWeeks();
            case SENIOR -> config.getOnboarding().getSeniorWeeks();
        };
    }

    private ExperienceLevel assignDefaultExperience(Role role) {
        return config.getInitialTeamExperience().forRole(role);
    }

    private void activatePendingHires(int simulatedWeek) {
        List<PendingHire> joining = new ArrayList<>();
        for (PendingHire pendingHire : pendingHires) {
            if (pendingHire.advanceWeek()) {
                joining.add(pendingHire);
            }
        }
        for (PendingHire pendingHire : joining) {
            Employee employee = pendingHire.getEmployee();
            employee.setActive(true);
            team.addEmployee(employee);
            addMessage(employee.getExperienceLevel().name().replace('_', '-')
                    + " " + employee.getRole().name().replace('_', ' ').toLowerCase()
                    + " joined the project in Week " + simulatedWeek + " and began onboarding.");
            pendingHires.remove(pendingHire);
        }
    }

    private double averageOnboardingEffectiveness() {
        if (team.totalCount() == 0) {
            return 0.0;
        }
        return team.activeEmployees().stream()
                .mapToDouble(Employee::getOnboardingProgress)
                .average()
                .orElse(0.0);
    }

    private double averageDeveloperOnboardingDeficit() {
        List<Employee> developers = team.activeEmployees().stream()
                .filter(employee -> employee.getRole() == Role.DEVELOPER)
                .toList();
        if (developers.isEmpty()) {
            return 0.0;
        }
        return developers.stream()
                .mapToDouble(employee -> 1.0 - employee.getOnboardingProgress())
                .average()
                .orElse(0.0);
    }

    private boolean isHigherMentoringLoad(String candidate, String baseline) {
        return mentoringRank(candidate) > mentoringRank(baseline);
    }

    private int mentoringRank(String label) {
        return switch (label) {
            case "Low" -> 0;
            case "Moderate" -> 1;
            case "High" -> 2;
            case "Overloaded" -> 3;
            default -> 0;
        };
    }

    private static Role roleFromKey(String key) {
        return switch (key) {
            case "projectManagers" -> Role.PROJECT_MANAGER;
            case "developers" -> Role.DEVELOPER;
            case "qaEngineers" -> Role.QA_ENGINEER;
            case "devOpsEngineers", "devopsEngineers" -> Role.DEVOPS_ENGINEER;
            default -> Role.valueOf(key.toUpperCase());
        };
    }

    private static ProjectPhase phaseFromKey(String key) {
        try {
            return ProjectPhase.valueOf(key.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unknown project phase in scenario: " + key, exception);
        }
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, Double.isFinite(value) ? value : 0.0));
    }

    private String scheduleHealthLabel() {
        return new SchedulePressureModel().category(schedulePressure);
    }

    private String budgetHealthLabel() {
        double ratio = project.getRemainingBudget().doubleValue()
                / Math.max(1.0, scenario.getBudget().doubleValue());
        if (ratio > 0.2) {
            return "Healthy";
        }
        if (ratio > 0.08) {
            return "At Risk";
        }
        return "Critical";
    }

    private String qualityHealthLabel() {
        return new QualityHealthModel().assess(
                project.getWorkState().totalKnownRework(),
                project.getWorkState().getTotalTestingBacklog(),
                project.getWorkState().getTotalDefectsDiscoveredThisWeek(), config);
    }

    private Map<String, Double> phaseProgressForUi() {
        Map<String, Double> progress = new java.util.LinkedHashMap<>();
        project.getWorkState().phasePerceivedProgress()
                .forEach((phase, value) -> progress.put(phase.name(), value));
        return Map.copyOf(progress);
    }

    private double developerExperienceModifier() {
        return roleExperienceModifier(Role.DEVELOPER);
    }

    private double roleExperienceModifier(Role role) {
        List<Employee> employees = team.activeEmployees().stream()
                .filter(employee -> employee.getRole() == role)
                .toList();
        if (employees.isEmpty()) {
            return 1.0;
        }
        return employees.stream().mapToDouble(employee -> switch (employee.getExperienceLevel()) {
            case JUNIOR -> config.getQuality().getJuniorDefectMultiplier();
            case MID_LEVEL -> config.getQuality().getMidDefectMultiplier();
            case SENIOR -> config.getQuality().getSeniorDefectMultiplier();
        }).average().orElse(1.0);
    }

    private String moraleHealthLabel() {
        if (team.totalCount() == 0) {
            return "No Active Team";
        }
        return new MoraleModel().healthLabel(averageMorale());
    }

    private String fatigueHealthLabel() {
        if (team.totalCount() == 0) {
            return "No Active Team";
        }
        return new FatigueModel().category(averageFatigue());
    }
}
