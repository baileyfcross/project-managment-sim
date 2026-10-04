# Software Project Management Simulator

The Software Project Management Simulator is an offline desktop learning application for students to act as project managers in a simulated software engineering project. It models staffing and delayed onboarding, phase-specific work, QA and rework, schedule and budget pressure, team health and turnover, contextual events, scope decisions, technical debt, and concurrency. After a run, it presents category scores, historical charts, decision review, evidence-based findings, and read-only instructor details.

## Purpose

This project is designed for classroom use in software engineering courses. Students make staffing, work-intensity, engineering-quality, concurrency, and event decisions that affect scope, schedule, quality, morale, onboarding, and cost, while the underlying simulation exposes how short-term tradeoffs create delayed project consequences.

## Architecture

The project uses a layered design:

- Java 21 backend for authoritative simulation, history, scoring, reporting, and analysis.
- JavaFX desktop shell with a JavaFX WebView frontend.
- Vite and TypeScript for the browser-like UI layer.
- Gradle to coordinate Java, frontend build, tests, and Windows application-image packaging.

## Technology Stack

- Java 21+
- JavaFX
- Gradle
- JavaScript / TypeScript
- Vite
- JUnit 5
- Jackson

## Project Structure

- frontend/ - Vite frontend source and build output
- src/main/java/ - Java backend classes
- src/main/resources/configuration/ - JSON configuration
- src/main/resources/scenarios/ - scenario definitions
- src/main/resources/web/ - generated UI assets copied from Vite
- src/test/java/ - unit and integration tests
- docs/ - architecture, scoring, instructor, and simulation documentation

## Development Requirements

- Java 21 or newer
- Node.js and npm for frontend build steps
- Gradle wrapper (Gradle 8.10.2)

Set `JAVA_HOME` to a Java 21 JDK before building. The Gradle Java toolchain requires Java 21. Node.js and npm are used by developer build tasks; students running the packaged Windows application do not need Java, Gradle, Node.js, npm, or VS Code.

## Run on Windows

In PowerShell, ensure `JAVA_HOME` points to the installed JDK, then run:

```powershell
.\gradlew.bat run
```

The same Gradle application run task is available on other platforms with `./gradlew run`. Gradle builds the TypeScript frontend into local Java resources before launching JavaFX. The desktop application loads bundled assets and does not require a browser, Vite server, internet connection, or remote service.

## Test on Windows

```powershell
.\gradlew.bat clean test
```

## Build on Windows

```powershell
.\gradlew.bat clean build
```

## Package for Windows

On Windows with a Java 21 JDK that includes `jpackage`, build a self-contained application image:

```powershell
.\gradlew.bat packageApp
```

The generated launcher and bundled runtime are under `build\package\windows\SoftwareProjectManagementSimulator`. The package task builds the frontend and includes the JavaFX modules required by the desktop app. It produces an application image, not an installer.

## Seeds and replay

Enter a numeric seed at setup to reproduce the same random stream. Leave it blank to generate a seed automatically. The final report displays the run seed and offers **Copy seed** and **Run Again With Same Seed**. Replay resets the simulation and restores the original team and scenario, but does not repeat prior decisions. **Start New Simulation** returns to setup and permits a new seed.

## Scenario Model

Scenarios are loaded from JSON in src/main/resources/scenarios. Each scenario defines project scope, deadline, budget, and starting parameters. The simulator stores seeded randomness to allow deterministic repeats for classroom comparison.

## Configuration Model

Simulation tuning values live in JSON configuration files in src/main/resources/configuration. These include experience-based salaries and starting experience by role, hiring delays and fees, onboarding, mentoring, coordination, developer rework allocation, phase overlap, quality and defect modifiers, QA throughput and testing priority, regression testing, workweek hours and effort, fatigue accumulation and effects, morale recovery, turnover risk, productivity, overtime premiums, event frequency and contextual weights, scope propagation, technical-debt effects, engineering approaches, and concurrency policies.

## Simulation Highlights

The engine tracks perceived progress separately from true progress and unknown rework. During play, students see perceived progress, known rework, current testing signals, and other manager-visible information. Exact true progress, unknown rework, internal event effects, and detailed weekly state remain in Java and are not included in the gameplay DTO. Once a run ends, the final report deliberately reveals historical truth for teaching and discussion. The instructor view separates **Visible to Student at the Time** from **Hidden State at the Time**.

The final score is calculated in Java from Schedule (25 points), Budget (25), Quality (25), Team Sustainability (15), and Customer Value (10). The report includes perceived-versus-true progress, cost, quality/rework, team health, team size, debt, and schedule-pressure charts; a decision and event timeline; evidence-based causal findings; and reflection prompts. See [docs/scoring.md](docs/scoring.md) and [docs/instructor-guide.md](docs/instructor-guide.md).

## Work, quality, and testing

Each project phase starts with its scenario-defined base work. Attempted work reduces base work remaining and is split into correct work and unknown rework. Unknown rework is defective output the project currently believes is correct: it contributes to perceived progress but not true progress. When QA tests available work, a seeded aggregate detection model may move unknown rework to known rework. The manager sees known rework and testing backlog status, but not the hidden stock.

Developers reserve a configurable share of capacity for known rework and use the rest for new requirements, design, and development. Repairs consume capacity; correct repairs increase true progress, while defective repairs return to unknown rework. Correctly repaired work creates regression-testing demand. QA capacity is limited to output that has become testable and advances testing work without creating development features. DevOps capacity performs deployment work. Project phases overlap gradually: requirements progress increases design availability, design progress increases development availability, and tested development increases deployment readiness.

Testing priority changes QA inspection capacity and detection effectiveness. Low priority does not directly create defects; it allows more defects to remain hidden while testing backlog grows. Defect probability is bounded and affected by configured base rates, experience, onboarding, fatigue, coordination, schedule pressure, work intensity, mentoring coverage, and hidden upstream defects. Defective work is sampled in aggregate using the engine's seeded random generator rather than rolling once per microscopic work unit.

The schedule forecast uses perceived remaining work, known rework, role-specific capacity, and testing backlog. It never uses unknown rework or exact hidden defect counts. A project can satisfy its known work, testing, and deployment requirements and release while unknown defects remain; the released-defect count is retained internally for later analysis.

Post-start hires are selected by role, experience, and quantity. Hiring fees are charged when the decision is accepted; salary and productivity begin only when recruiting delay expires. Hires then contribute at configured initial onboarding effectiveness and progress toward full contribution over their experience-based onboarding duration, moderated by mentoring coverage. Junior, mid-level, and senior staff have different salary and productivity values. Non-developer roles contribute to QA, DevOps, project management, mentoring, and coordination effects, but not feature-development throughput.

## Overtime and team health

Managers can select a Sustainable, Increased, or Crunch workweek. The default schedules are 40, 48, and 60 hours. Higher intensity applies a separate configurable effort multiplier, so overtime can improve immediate throughput with diminishing returns rather than scaling directly with hours. Overtime adds a premium only for hours above the standard schedule; the default premium rate is applied proportionally to those extra hours and does not multiply all payroll by an overtime factor.

Fatigue belongs to each active employee and remains bounded from 0 to 1. Fatigue at the start of a week affects that week's productivity, defect probability, QA effectiveness, mentoring capacity, and coordination contribution. The selected intensity and that week's schedule pressure update fatigue at the end of the week. Accumulation increases gradually with overtime streaks and fatigue; sustainable hours recover gradually, with pressure slowing recovery. The productivity penalty grows with squared fatigue, allowing an early Crunch boost to later be outweighed by accumulated fatigue.

Morale is also maintained per employee and summarized as Good, Stable, Strained, Poor, or Critical. Sustainable work during lower-pressure periods permits gradual recovery. Turnover is seeded per-employee chance, with a low baseline that increases with severe fatigue, morale below baseline, schedule pressure, and consecutive overtime. Departures are not scripted at a fatigue threshold. A departure removes the employee from future payroll, capacity, mentoring, and coordination. The manager must decide whether to hire a replacement, which still experiences the Phase 2 recruiting delay and onboarding process.

The dashboard exposes workweek hours, an average fatigue category, team morale, turnover-risk category, departures this week, and the current overtime premium. It does not reveal exact employee-level fatigue or departure probabilities. Weekly snapshots retain internal fatigue, morale, overtime, streak, and departure history for later analysis. Forecast cost assumes the currently selected work intensity continues for its projected duration and does not predict future turnover.

## Classroom use

The default Small Business Web Application scenario is configured for a 20-week deadline and a $400,000 budget. Students can use a common seed, compare independently managed runs, and replay the same seed with different decisions. The instructor guide includes setup suggestions, discussion questions, and chart interpretation. Local JSON run export/import and multi-run storage are not implemented; the report includes structured metadata and comparison-ready headline metrics.

## Project events and management policies

Project events are generated at the end of a simulated week using the engine's seeded random generator. Event chances and weights depend on elapsed project progress, hidden risk stocks, technical debt, schedule pressure, concurrency, staffing, and scenario settings. Configurable spacing and cooldown values prevent event spam. Important decisions remain pending and block the next advance until the player chooses an option. The event panel and history show descriptions, qualitative impact estimates, decisions, and visible results, not hidden formulas or exact rework quantities.

Customer feature requests are the primary scope-change event. Accepting a request adds phase-specific work to the authoritative ledger. Completed work can require change-related rework, and late changes can create additional testing demand. Scope is shown relative to the original plan and feeds forecast cost and schedule pressure through the added workload. Deferring a feature records it for a future release without adding current work; rejecting it records the lost opportunity without changing scope. Each request is resolved once.

Technical debt represents shortcuts that make future changes harder, not the current defect count. Start-of-week debt affects developer capacity, defect risk, and rework difficulty. The selected engineering approach and pressure-related conditions update debt at week end. Careful work is somewhat slower and accumulates less debt; cutting corners is faster immediately but raises defect risk and future debt. Pay Down reserves developer capacity and reduces debt gradually for later weeks.

Concurrency controls how much downstream work may begin while upstream phases are unfinished. Sequential work reduces overlap; Moderate is the default; Aggressive makes more work available earlier and can improve immediate throughput. Work begun while upstream decisions remain unsettled contributes to internal dependency uncertainty and quality risk. Project management coordination support reduces some, but not all, of this risk.

Scope and event work can increase forecast duration, payroll, testing and known rework. Students can respond by hiring, but new staff still pass through recruiting, onboarding and mentoring. Added pressure flows through the existing overtime, fatigue, morale and turnover systems rather than directly changing employee state. The forecast uses visible current work, known rework, staffing and selected policies, but does not predict future events, unknown rework, scope requests or turnover.

## Week and seed behavior

The project starts at Week 0, before any work. Each press of **Advance 1 Week** simulates the next week, advances any pending recruiting and onboarding, stores an end-of-week snapshot, and leaves the project at that week. After N advances, the current week and history length are both N.

Leave the seed field blank to have Java generate and display a seed, or enter a signed 64-bit integer to reproduce a run. Stochastic work, turnover, and event decisions use the same seeded generator. Repeating a scenario with the same seed, team, staffing, work-intensity, testing, scope, concurrency, engineering, debt-priority, and event decisions reproduces the same weekly results.

## Frontend/backend boundary

Java is authoritative for setup, team counts, staffing options, project state, event rules, policy effects, cost calculations, and simulation outcomes. TypeScript renders DTOs and sends requested actions through the narrow JSON-based Java bridge; it does not maintain an independent simulation state.

## Adding a Scenario

1. Create a JSON file under src/main/resources/scenarios.
2. Give it a unique id.
3. Ensure it includes a name, deadline, budget, and phase work totals.
4. Reference it from the scenario loader or default scenario selection.

## Adding a New Event Type

1. Add a new event type to the EventType enum.
2. Implement contextual generation and consequences in focused event/simulation models.
3. Add visible options and resolution behavior through the gameplay DTO and Java bridge.
4. Keep hidden effects out of gameplay DTOs and cover the behavior with a deterministic test.

## License

This project is intended for educational use and classroom simulation exercises.
