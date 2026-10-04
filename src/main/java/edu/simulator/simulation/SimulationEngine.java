package edu.simulator.simulation;

import edu.simulator.configuration.ScenarioConfiguration;
import edu.simulator.configuration.SimulationConfiguration;
import edu.simulator.event.EventType;
import edu.simulator.event.ProjectEvent;
import edu.simulator.model.Employee;
import edu.simulator.model.ExperienceLevel;
import edu.simulator.model.Project;
import edu.simulator.model.ProjectPhase;
import edu.simulator.model.Role;
import edu.simulator.model.Team;
import edu.simulator.model.WeeklySnapshot;
import edu.simulator.model.WorkState;
import edu.simulator.report.FinalProjectReport;
import edu.simulator.ui.SimulationStateDto;

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
    private final List<String> messages = new ArrayList<>();
    private double fatigue = 0.18;
    private double morale = 0.72;
    private double schedulePressure = 0.15;
    private WorkIntensity workIntensity = WorkIntensity.SUSTAINABLE;
    private boolean complete;
    private int totalTurnover;
    private int defectsReleased;
    private BigDecimal lastWeeklyCost = BigDecimal.ZERO;

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
                Employee employee = new Employee(role, level, weeklyRateFor(role, level));
                employee.setOnboardingProgress(1.0);
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

    public SimulationStateDto getCurrentState() {
        BigDecimal payroll = new CostModel().calculateWeeklyPayroll(team, config);
        double weeklyWorkCapacity = developerCapacity() * WORK_UNITS_PER_PRODUCTIVITY;
        int estimatedCompletionWeek = new ForecastModel().estimateCompletionWeek(project, weeklyWorkCapacity);
        int estimatedWeeksToFinish = estimatedCompletionWeek < 0
                ? 0 : Math.max(0, estimatedCompletionWeek - project.getCurrentWeek());
        BigDecimal forecastCost = project.getSpent().add(
                payroll.multiply(BigDecimal.valueOf(estimatedWeeksToFinish)));
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
        double coordinationPenalty = new CoordinationModel().calculatePenalty(team, config);
        ProductivityModel.ProductivityResult productivity = new ProductivityModel().calculate(
                team, workIntensity, fatigue, schedulePressure, config);
        double workCapacity = productivity.developerEffectiveCapacity() * WORK_UNITS_PER_PRODUCTIVITY;
        double defectRate = new DefectModel().defectProbability(
                config.getQuality().getBaseDefectRate(), fatigue, schedulePressure,
                coordinationPenalty, 0.0, config);

        workCapacity = completeKnownRework(workCapacity, defectRate);
        performNewWork(workCapacity, defectRate);
        discoverDefects();

        schedulePressure = new SchedulePressureModel().calculate(
                project, project.getWorkState().perceivedRemainingWork(),
                Math.max(1.0, workCapacity), project.getWorkState().totalKnownRework(), config);
        project.recordSchedulePressure(schedulePressure);
        fatigue = new FatigueModel().updateFatigue(fatigue, workIntensity, config);
        morale = clamp(morale - schedulePressure * 0.18
                + (workIntensity == WorkIntensity.SUSTAINABLE ? 0.03 : -0.02));

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
        lastWeeklyCost = payroll.add(overtime);
        project.addSpent(lastWeeklyCost);

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
                averageProductivity,
                fatigue,
                project.getWorkState().totalKnownRework(),
                project.getWorkState().totalUnknownRework(),
                new ForecastModel().estimateCompletionWeek(
                        project, developerCapacity() * WORK_UNITS_PER_PRODUCTIVITY),
                schedulePressure,
                messages
        ));
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

    private void discoverDefects() {
        double qaCount = team.count(Role.QA_ENGINEER);
        double discoveryCapacity = qaCount * 50.0
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
        return new ProductivityModel().calculate(team, workIntensity, fatigue,
                schedulePressure, config).developerEffectiveCapacity();
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

    private double weeklyRateFor(Role role, ExperienceLevel level) {
        return switch (role) {
            case DEVELOPER -> switch (level) {
                case JUNIOR -> config.getCosts().getJuniorDeveloperWeekly();
                case MID_LEVEL -> config.getCosts().getMidDeveloperWeekly();
                case SENIOR -> config.getCosts().getSeniorDeveloperWeekly();
            };
            case QA_ENGINEER -> config.getCosts().getQaWeekly();
            case DEVOPS_ENGINEER -> config.getCosts().getDevopsWeekly();
            case PROJECT_MANAGER -> config.getCosts().getProjectManagerWeekly();
        };
    }

    private ExperienceLevel assignDefaultExperience(Role role) {
        return switch (role) {
            case PROJECT_MANAGER -> ExperienceLevel.SENIOR;
            case DEVELOPER, QA_ENGINEER, DEVOPS_ENGINEER -> ExperienceLevel.MID_LEVEL;
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
