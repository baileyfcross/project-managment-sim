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
import edu.simulator.model.WeeklySnapshot;
import edu.simulator.model.WorkState;
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
    private static final double WORK_UNITS_PER_PRODUCTIVITY = 180.0;
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
    private double fatigue = 0.18;
    private double morale = 0.72;
    private double schedulePressure = 0.15;
    private WorkIntensity workIntensity = WorkIntensity.SUSTAINABLE;
    private boolean complete;
    private int totalTurnover;
    private int defectsReleased;
    private BigDecimal lastWeeklyCost = BigDecimal.ZERO;
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
        this.workIntensity = workIntensity;
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
        double weeklyWorkCapacity = developerCapacity() * WORK_UNITS_PER_PRODUCTIVITY;
        int estimatedCompletionWeek = new ForecastModel().estimateCompletionWeek(project, weeklyWorkCapacity);
        int estimatedWeeksToFinish = estimatedCompletionWeek < 0
                ? Math.max(0, scenario.getDeadlineWeeks() - project.getCurrentWeek())
                : Math.max(0, estimatedCompletionWeek - project.getCurrentWeek());
        BigDecimal projectedCost = payroll.add(costModel.calculateOvertimeCost(payroll, workIntensity, config))
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
                complete
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
        activatePendingHires(simulatedWeek);
        for (Employee employee : team.activeEmployees()) {
            employee.incrementWeek();
        }
        MentoringModel mentoringModel = new MentoringModel();
        MentoringModel.MentoringResult mentoring =
                mentoringModel.calculate(team, pendingHires.size(), config);
        double coordinationPenalty = new CoordinationModel().calculatePenalty(team, config);
        ProductivityModel.ProductivityResult productivity = new ProductivityModel().calculate(
                team, workIntensity, fatigue, schedulePressure, config, mentoring);
        double workCapacity = productivity.developerEffectiveCapacity() * WORK_UNITS_PER_PRODUCTIVITY;
        double defectRate = new DefectModel().defectProbability(
                config.getQuality().getBaseDefectRate(), fatigue, schedulePressure,
                coordinationPenalty, averageDeveloperOnboardingDeficit(), config);

        workCapacity = completeKnownRework(workCapacity, defectRate);
        performNewWork(workCapacity, defectRate);
        discoverDefects(productivity.qaEffectiveCapacity());

        schedulePressure = new SchedulePressureModel().calculate(
                project, project.getWorkState().perceivedRemainingWork(),
                Math.max(1.0, workCapacity), project.getWorkState().totalKnownRework(), config);
        project.recordSchedulePressure(schedulePressure);
        fatigue = new FatigueModel().updateFatigue(fatigue, workIntensity, config);
        for (Employee employee : team.activeEmployees()) {
            employee.setFatigue(fatigue);
        }
        morale = clamp(morale - schedulePressure * 0.18
                + (workIntensity == WorkIntensity.SUSTAINABLE ? 0.03 : -0.02));
        int onboardedCount = mentoringModel.advanceOnboarding(team, mentoring, config);
        if (onboardedCount > 0) {
            addMessage(onboardedCount + (onboardedCount == 1
                    ? " employee completed onboarding."
                    : " employees completed onboarding."));
        }
        String mentoringLabel = mentoringModel.calculate(team, pendingHires.size(), config).loadLabel();
        if (mentoringLabel.equals("Overloaded") && !mentoring.loadLabel().equals("Overloaded")) {
            addMessage("Mentoring demand is overloaded; onboarding is progressing more slowly.");
        }

        int departures = new TurnoverModel().calculateTurnover(
                team, fatigue, morale, schedulePressure, config, random);
        totalTurnover += departures;
        if (departures > 0) {
            team.removeInactiveEmployees();
            addMessage(departures + " team member(s) left the project.");
            events.add(new ProjectEvent(EventType.EMPLOYEE_RESIGNATION,
                    departures + " employees exited the project in Week " + simulatedWeek + ".",
                    simulatedWeek));
        }

        BigDecimal payroll = new CostModel().calculateWeeklyPayroll(team, config);
        BigDecimal overtime = new CostModel().calculateOvertimeCost(payroll, workIntensity, config);
        BigDecimal hiringCost = hiringCostSinceSnapshot;
        lastWeeklyCost = payroll.add(overtime).add(hiringCost);
        project.addSpent(payroll.add(overtime));

        if (schedulePressure > 0.8 && random.nextDouble() < 0.25) {
            events.add(new ProjectEvent(EventType.PRODUCTION_BUG,
                    "Production issues emerged under heavy schedule pressure in Week " + simulatedWeek + ".",
                    simulatedWeek));
            addMessage("Production issues emerged under heavy schedule pressure.");
        }

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

        double averageProductivity = team.totalCount() == 0
                ? 0.0 : productivity.totalEffectiveCapacity() / team.totalCount();
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
                mentoring.load(),
                mentoring.coverage(),
                coordinationPenalty,
                payroll,
                hiringCost,
                averageProductivity,
                fatigue,
                project.getWorkState().totalKnownRework(),
                project.getWorkState().totalUnknownRework(),
                new ForecastModel().estimateCompletionWeek(
                        project, developerCapacity() * WORK_UNITS_PER_PRODUCTIVITY),
                schedulePressure,
                messages
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
                fatigue,
                morale,
                customerValue
        );
    }

    public double productivityForUi() {
        return new ProductivityModel().calculate(team, workIntensity, fatigue,
                schedulePressure, config).totalEffectiveCapacity();
    }

    private double completeKnownRework(double availableCapacity, double defectRate) {
        WorkState work = project.getWorkState();
        double capacity = availableCapacity;
        double reworkDefectRate = defectRate * config.getQuality().getReworkCreationRate();
        for (ProjectPhase phase : ProjectPhase.values()) {
            double known = work.getKnownRework(phase);
            double attempted = Math.min(known, capacity);
            if (attempted <= 0.0) {
                continue;
            }
            double completed = attempted * (1.0 - reworkDefectRate);
            work.addKnownRework(phase, -attempted);
            work.addKnownRework(phase, attempted - completed);
            work.addCompletedRework(phase, completed);
            work.addCompletedWork(phase, completed);
            capacity -= attempted;
            if (capacity <= 0.0) {
                break;
            }
        }
        return Math.max(0.0, capacity);
    }

    private void performNewWork(double availableCapacity, double defectRate) {
        WorkState work = project.getWorkState();
        double capacity = availableCapacity;
        for (ProjectPhase phase : ProjectPhase.values()) {
            double unattempted = Math.max(0.0, work.getTotalWork(phase)
                    - work.getCompletedWork(phase)
                    - work.getKnownRework(phase)
                    - work.getUnknownRework(phase));
            double attempted = Math.min(unattempted, capacity);
            if (attempted <= 0.0) {
                continue;
            }
            double defective = attempted * defectRate;
            work.addCompletedWork(phase, attempted - defective);
            work.addUnknownRework(phase, defective);
            capacity -= attempted;
            if (capacity <= 0.0) {
                break;
            }
        }
    }

    private void discoverDefects(double qaCapacity) {
        double discoveryCapacity = qaCapacity * 50.0
                * config.getQa().getBaseDetectionRate()
                * config.getQa().getCapacityMultiplier()
                * (1.0 - fatigue * 0.5);
        double remainingDetection = Math.max(0.0, discoveryCapacity);
        WorkState work = project.getWorkState();
        for (ProjectPhase phase : ProjectPhase.values()) {
            double found = Math.min(work.getUnknownRework(phase), remainingDetection);
            if (found > 0.0) {
                work.addUnknownRework(phase, -found);
                work.addKnownRework(phase, found);
                remainingDetection -= found;
            }
            if (remainingDetection <= 0.0) {
                break;
            }
        }
    }

    private double developerCapacity() {
        MentoringModel.MentoringResult mentoring =
                new MentoringModel().calculate(team, pendingHires.size(), config);
        return new ProductivityModel().calculate(team, workIntensity, fatigue,
                schedulePressure, config, mentoring).developerEffectiveCapacity();
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
        if (schedulePressure < 0.25) {
            return "Healthy";
        }
        if (schedulePressure < 0.63) {
            return "At Risk";
        }
        return "Critical";
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
        if (project.getWorkState().totalKnownRework() == 0.0) {
            return "Healthy";
        }
        if (project.getWorkState().totalKnownRework() < 100.0) {
            return "Concerning";
        }
        return "At Risk";
    }

    private String moraleHealthLabel() {
        if (morale > 0.7) {
            return "Good";
        }
        if (morale > 0.45) {
            return "Strained";
        }
        return "Poor";
    }
}
