package edu.simulator.simulation;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.simulator.configuration.ScenarioConfiguration;
import edu.simulator.configuration.SimulationConfiguration;
import edu.simulator.event.EventType;
import edu.simulator.event.ProjectEvent;
import edu.simulator.model.*;
import edu.simulator.report.FinalProjectReport;
import edu.simulator.report.ScoringService;
import edu.simulator.ui.SimulationStateDto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SimulationEngine {
    private final ScenarioConfiguration scenario;
    private final SimulationConfiguration config;
    private final Random random;
    private final Project project;
    private final Team team;
    private final List<WeeklySnapshot> history = new ArrayList<>();
    private final List<ProjectEvent> events = new ArrayList<>();
    private final List<String> messages = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private double fatigue = 0.18;
    private double morale = 0.72;
    private double schedulePressure = 0.15;
    private WorkIntensity workIntensity = WorkIntensity.SUSTAINABLE;
    private boolean complete;
    private int totalTurnover;
    private int defectsReleased;

    public SimulationEngine(ScenarioConfiguration scenario, SimulationConfiguration config, long seed) {
        this.scenario = scenario;
        this.config = config;
        this.random = new Random(seed);
        this.project = new Project(scenario.getId(), scenario.getName(), scenario.getDeadlineWeeks(), scenario.getBudget());
        this.team = new Team();
        this.messages.add("Project initialized. The team has been assembled.");
        initializeScenarioWork();
        initializeTeam();
    }

    public void initializeScenarioWork() {
        for (Map.Entry<String, Double> entry : scenario.getScope().entrySet()) {
            ProjectPhase phase = phaseFromKey(entry.getKey());
            this.project.getWorkState().setTotalWork(phase, entry.getValue());
        }
    }

    public void initializeTeam() {
        Map<String, Integer> counts = scenario.getInitialTeam();
        if (counts == null) {
            return;
        }
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            Role role = roleFromKey(entry.getKey());
            int count = Math.max(0, entry.getValue());
            for (int index = 0; index < count; index++) {
                ExperienceLevel level = assignDefaultExperience(role);
                Employee employee = new Employee(role, level, weeklyRateFor(role, level));
                employee.setOnboardingProgress(0.0);
                team.addEmployee(employee);
            }
        }
    }

    public void setWorkIntensity(WorkIntensity workIntensity) {
        this.workIntensity = workIntensity == null ? WorkIntensity.SUSTAINABLE : workIntensity;
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

    public boolean isComplete() {
        return complete;
    }

    public SimulationStateDto getCurrentState() {
        double actualProgress = project.calculateTrueProgress();
        double perceivedProgress = project.calculatePerceivedProgress();
        int estimatedCompletionWeek = new ForecastModel().estimateCompletionWeek(project, productivityForUi(), project.getWorkState().totalKnownRework());
        BigDecimal payroll = new CostModel().calculateWeeklyPayroll(team, config);
        Map<String, Integer> teamCounts = new HashMap<>();
        for (Role role : Role.values()) {
            teamCounts.put(role.name(), team.count(role));
        }
        Map<String, Double> phaseProgress = new HashMap<>();
        for (ProjectPhase phase : ProjectPhase.values()) {
            phaseProgress.put(phase.name(), project.getWorkState().getCompletedWork(phase) / Math.max(1.0, project.getWorkState().getTotalWork(phase)));
        }

        return new SimulationStateDto(
                project.getCurrentWeek(),
                scenario.getDeadlineWeeks(),
                scenario.getBudget(),
                project.getSpent(),
                project.getRemainingBudget(),
                payroll.doubleValue(),
                project.getRemainingBudget().doubleValue(),
                estimatedCompletionWeek,
                perceivedProgress,
                actualProgress,
                phaseProgress,
                teamCounts,
                scheduleHealthLabel(),
                budgetHealthLabel(),
                qualityHealthLabel(),
                moraleHealthLabel(),
                messages,
                project.getWorkState().totalKnownRework(),
                project.getWorkState().totalUnknownRework(),
                fatigue,
                morale,
                schedulePressure,
                project.getWorkState().totalRemainingWork(),
                project.getWorkState().totalKnownRework(),
                project.getDeadlineWeeks(),
                project.getBudget(),
                project.getSpent(),
                project.getRemainingBudget(),
                scenario.getName(),
                events.isEmpty() ? "No events" : events.get(events.size() - 1).getSummary()
        );
    }

    public void advanceWeek() {
        if (complete) {
            return;
        }

        for (Employee employee : team.allEmployees()) {
            employee.incrementWeek();
            int onboardingWeeks = onboardingWeeksFor(employee.getExperienceLevel());
            if (employee.getWeeksOnProject() <= onboardingWeeks) {
                employee.setOnboardingProgress(Math.min(1.0, employee.getWeeksOnProject() / (double) onboardingWeeks));
            } else {
                employee.setOnboardingProgress(1.0);
            }
        }

        double coordinationPenalty = new CoordinationModel().calculatePenalty(team, config);
        ProductivityModel.ProductivityResult productivityResult = new ProductivityModel().calculate(team, workIntensity, fatigue, schedulePressure, config);
        double totalCapacity = productivityResult.totalEffectiveCapacity();
        double qualityRate = new DefectModel().defectProbability(config.getQuality().getBaseDefectRate(), fatigue, schedulePressure, coordinationPenalty, 0.45, config);

        double output = Math.max(0.0, totalCapacity * 180.0);
        double phaseAllocation = output / 5.0;

        ProjectPhase[] phases = ProjectPhase.values();
        for (ProjectPhase phase : phases) {
            double totalForPhase = project.getWorkState().getTotalWork(phase);
            double remainingForPhase = Math.max(0.0, totalForPhase - project.getWorkState().getCompletedWork(phase));
            double completedContribution = Math.min(remainingForPhase, phaseAllocation * phaseWeight(phase));
            project.getWorkState().addCompletedWork(phase, completedContribution);
        }

        double defectUnits = Math.max(0.0, output * qualityRate * 0.6);
        double reworkFromDefects = Math.max(0.0, defectUnits * 0.35);
        for (ProjectPhase phase : ProjectPhase.values()) {
            project.getWorkState().addUnknownRework(phase, defectUnits * phaseWeight(phase) * 0.2);
        }

        double qaCapacity = team.count(Role.QA_ENGINEER) * (1.0 + config.getQa().getCapacityMultiplier()) * 40.0;
        double discovered = Math.min(project.getWorkState().totalUnknownRework(), qaCapacity * (0.08 + config.getQa().getBaseDetectionRate()));
        if (discovered > 0.0) {
            for (ProjectPhase phase : ProjectPhase.values()) {
                double phaseUnknown = project.getWorkState().getUnknownRework(phase);
                double moved = Math.min(phaseUnknown, discovered * phaseWeight(phase));
                project.getWorkState().addKnownRework(phase, moved);
                project.getWorkState().addUnknownRework(phase, -moved);
                discovered -= moved;
                if (discovered <= 0.0) {
                    break;
                }
            }
        }

        double reworkCapacity = Math.max(0.0, totalCapacity * 120.0 * 0.45);
        for (ProjectPhase phase : ProjectPhase.values()) {
            double known = project.getWorkState().getKnownRework(phase);
            double fixed = Math.min(known, reworkCapacity * phaseWeight(phase));
            project.getWorkState().addCompletedRework(phase, fixed);
            project.getWorkState().addKnownRework(phase, -fixed);
            project.getWorkState().addCompletedWork(phase, fixed * 0.7);
            reworkCapacity -= fixed;
        }

        double totalKnown = project.getWorkState().totalKnownRework();
        schedulePressure = new SchedulePressureModel().calculate(project, project.getWorkState().totalRemainingWork(), totalCapacity, totalKnown, config);
        project.recordSchedulePressure(schedulePressure);

        fatigue = new FatigueModel().updateFatigue(fatigue, workIntensity, config);
        morale = clamp(morale - (schedulePressure * 0.18) + (workIntensity == WorkIntensity.SUSTAINABLE ? 0.03 : -0.02));

        int turnover = new TurnoverModel().calculateTurnover(team, fatigue, morale, schedulePressure, config, random);
        totalTurnover += turnover;
        if (turnover > 0) {
            messages.add(turnover + " employees left the team. Replacement hiring is now necessary.");
            events.add(new ProjectEvent(EventType.EMPLOYEE_RESIGNATION, turnover + " employees exited the project."));
        }

        BigDecimal payroll = new CostModel().calculateWeeklyPayroll(team, config);
        BigDecimal overtimeCost = new CostModel().calculateOvertimeCost(payroll, workIntensity, config);
        BigDecimal weeklyCost = payroll.add(overtimeCost);
        project.addSpent(weeklyCost);

        if (project.getRemainingBudget().compareTo(BigDecimal.ZERO) <= 0 && scenario.isBudgetFailureAllowed()) {
            complete = true;
            messages.add("The project has exceeded the available budget.");
            events.add(new ProjectEvent(EventType.PERFORMANCE_PROBLEM, "Budget pressure has forced the project to end."));
        }

        if (project.getCurrentWeek() >= scenario.getDeadlineWeeks() && project.getWorkState().totalRemainingWork() > 0.0) {
            complete = true;
            messages.add("The deadline was reached before completion. The project closed with unresolved work.");
            events.add(new ProjectEvent(EventType.PERFORMANCE_PROBLEM, "Deadline missed."));
        }

        if (schedulePressure > 0.8 && random.nextDouble() < 0.25) {
            events.add(new ProjectEvent(EventType.PRODUCTION_BUG, "Production issues emerged under heavy schedule pressure."));
            messages.add("A production bug threatened the delivery plan.");
        }

        history.add(new WeeklySnapshot(project.getCurrentWeek(), project.getSpent(), project.getRemainingBudget(), weeklyCost.doubleValue(),
                project.calculatePerceivedProgress(), project.calculateTrueProgress(), schedulePressure, fatigue, team.totalCount(), new ArrayList<>(messages)));

        if (project.getWorkState().totalRemainingWork() <= 0.0 && project.getWorkState().totalKnownRework() <= 0.0) {
            complete = true;
            messages.add("The project reached a release-ready state.");
            events.add(new ProjectEvent(EventType.CUSTOMER_FEATURE_REQUEST, "The project is ready to release."));
        }

        project.incrementWeek();
    }

    public List<WeeklySnapshot> getHistory() {
        return new ArrayList<>(history);
    }

    public FinalProjectReport generateFinalReport() {
        double customerValue = Math.max(0.0, Math.min(1.0, 0.9 - (double) defectsReleased / 100.0 + project.calculateTrueProgress() * 0.2));
        double defectsRate = Math.max(0.0, defectsReleased / 10.0);
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
        return new ProductivityModel().calculate(team, workIntensity, fatigue, schedulePressure, config).totalEffectiveCapacity();
    }

    private double clamp(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, value));
    }

    private double phaseWeight(ProjectPhase phase) {
        return switch (phase) {
            case REQUIREMENTS -> 0.10;
            case DESIGN -> 0.14;
            case DEVELOPMENT -> 0.48;
            case TESTING -> 0.20;
            case DEPLOYMENT -> 0.08;
        };
    }

    private int onboardingWeeksFor(ExperienceLevel level) {
        return switch (level) {
            case JUNIOR -> config.getOnboarding().getJuniorWeeks();
            case MID_LEVEL -> config.getOnboarding().getMidWeeks();
            case SENIOR -> config.getOnboarding().getSeniorWeeks();
        };
    }

    private ExperienceLevel assignDefaultExperience(Role role) {
        return switch (role) {
            case DEVELOPER -> ExperienceLevel.MID_LEVEL;
            case PROJECT_MANAGER -> ExperienceLevel.SENIOR;
            case QA_ENGINEER -> ExperienceLevel.MID_LEVEL;
            case DEVOPS_ENGINEER -> ExperienceLevel.MID_LEVEL;
        };
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

    private Role roleFromKey(String key) {
        String cleaned = key.trim();
        return switch (cleaned) {
            case "projectManagers" -> Role.PROJECT_MANAGER;
            case "developers" -> Role.DEVELOPER;
            case "qaEngineers" -> Role.QA_ENGINEER;
            case "devOpsEngineers", "devopsEngineers" -> Role.DEVOPS_ENGINEER;
            default -> Role.DEVELOPER;
        };
    }

    private ProjectPhase phaseFromKey(String key) {
        String cleaned = key.trim();
        return switch (cleaned) {
            case "requirements" -> ProjectPhase.REQUIREMENTS;
            case "design" -> ProjectPhase.DESIGN;
            case "development" -> ProjectPhase.DEVELOPMENT;
            case "testing" -> ProjectPhase.TESTING;
            case "deployment" -> ProjectPhase.DEPLOYMENT;
            default -> ProjectPhase.DEVELOPMENT;
        };
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
        if (project.getRemainingBudget().doubleValue() / Math.max(1, scenario.getBudget().doubleValue()) > 0.2) {
            return "Healthy";
        }
        if (project.getRemainingBudget().doubleValue() / Math.max(1, scenario.getBudget().doubleValue()) > 0.08) {
            return "At Risk";
        }
        return "Critical";
    }

    private String qualityHealthLabel() {
        if (schedulePressure < 0.35) {
            return "Healthy";
        }
        if (schedulePressure < 0.7) {
            return "Concerning";
        }
        return "Unknown";
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
