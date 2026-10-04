# Architecture Overview

The simulator follows a layered architecture that separates the domain model from the UI and simulation engine.

## Backend

The Java backend contains the domain model, configuration, scenarios, simulation engine, and reporting classes. Domain objects represent employees, projects, work states, cost, and health metrics. The simulation engine coordinates a weekly loop and keeps a deterministic random seed for reproducible scenarios.

## Frontend

The frontend is a Vite-based TypeScript application rendered inside JavaFX WebView. It provides the initial team builder, active-project dashboard, in-project Manage Team panel, Project Decisions controls, and player-facing event decisions/history. Once a run ends, it renders the final report, local SVG charts, decision review, causal findings, and an instructor history inspector.

## Java and JavaScript Bridge

The JavaBridge exposes JSON-based setup and gameplay operations: read setup values, update the initial team, start with a selected seed, read staffing state and hiring options, submit a hire decision, change work intensity/testing/concurrency/engineering/debt policies, resolve pending events, and advance a week. A separate post-termination operation provides the final report and instructor details. It does not expose domain objects or internal weekly snapshots during normal gameplay. Java owns all authoritative state and calculations; TypeScript renders DTOs.

## Gradle and Vite

Gradle is responsible for coordinating the build. It runs reproducible `npm ci` only when package inputs require it, executes the Vite production build when frontend inputs change, and includes the generated frontend in Java resources for embedding. On Windows, `packageApp` uses `jpackage` to create an application image with a bundled Java runtime and the JavaFX modules; it does not create an installer.

## Simulation Layers

- Domain: project, employee, pending hire, onboarding state, budget, per-phase work stocks, testing priority, work intensity, concurrency policy, engineering approach, technical-debt priority, event options, decisions and results, health, snapshots
- Simulation: experience-based productivity and cost, hiring delays, mentoring, onboarding, phase readiness, work allocation, defect generation and discovery, rework, QA backlog, fatigue, coordination, schedule pressure, event generation, scope propagation, technical debt, concurrency, forecast

Focused models own their formulas: `WorkAllocationModel` divides developer capacity, `PhaseReadinessModel` and `ConcurrencyModel` control downstream availability and dependency risk, `NewWorkModel` attempts new developer and DevOps work, `DefectModel` bounds and samples defective work, `ReworkModel` repairs known defects, `TestingModel` processes the bounded testable queue and discovers defects, `ScopeChangeModel` adds scope and progress-dependent rework, `TechnicalDebtModel` applies delayed debt effects and repayment, `EventGenerator` selects contextual seeded events, `QualityHealthModel` derives visible quality status, and the fatigue, morale, and turnover models maintain the Phase 4 team dynamics. `SimulationEngine` coordinates the weekly order, event resolution, and snapshots.

`WorkState` is the authoritative per-phase work ledger. It distinguishes original and expanded scope, remaining work, correct completion, unknown and known rework, test backlog, and weekly flow metrics. `WeeklySnapshot` retains both manager-visible and hidden metrics, including Phase 5 policies, event traces, out-of-sequence work, and technical debt. `SimulationStateDto` includes only visible event choices/history, scope and debt categories, policy selections, perceived progress, known rework, current discoveries, testing status and priority, and QA capacity. It never exposes hidden work stocks, exact event probabilities, internal risk multipliers, or true progress.

Phase 4 human state is stored on `Employee`: normalized fatigue, morale, and consecutive overtime weeks. `ProductivityModel` uses the configured intensity effort separately from fatigue productivity impact. `SchedulePressureModel` uses only perceived remaining work, known rework, test backlog, role capacities, and deadline. `TurnoverModel` uses the engine's seeded random generator; departed staff are removed from active team calculations while their departure records remain in weekly history. The gameplay DTO exposes aggregate health categories and current overtime cost, while exact employee state and turnover rates stay internal.

Events use the engine's single seeded random generator and are created at week end. Decision events remain blocking until an explicit option is selected. Event results retain internal outcome metadata, while the gameplay DTO exposes only player-facing result text. Scope increases update base work remaining rather than manipulating displayed percentages; progress-dependent rework and regression demand use the existing work and QA ledgers. Debt entering a week affects that week's work, while engineering decisions and planned repayment update debt at week end. A concurrency choice applies immediately to the next week's readiness, capacity, and risk.
- Events: project events and scope changes
- Reporting: `FinalReportService` assembles a cached report from the contiguous weekly history after termination. It does not advance or rerun the engine. `ScoringService` is the single Java-authoritative source for the bounded 100-point category score. `CausalAnalysisService` applies deterministic, evidence-bearing rules to the observed run; its findings are explanatory patterns, not statistical causal estimates.

Management decisions are recorded when accepted, with week, type, and before/after values. Event outcomes already have their own records. The final timeline merges these records for review. Same-seed replay constructs a fresh engine with the original scenario, initial team, and seed, but intentionally does not repeat player decisions. Starting a new simulation clears the engine and returns the UI to setup.

The final report is a post-run trust boundary: instructor data includes truth and hidden state retained in weekly snapshots, while `SimulationStateDto` remains sanitized during play. Only metrics actually retained by the run are presented. Historical per-employee state and exact past probabilities are not reconstructed or fabricated.

## Design Principles

- Separate simulation logic from UI code
- Do not put formulas in controllers
- Keep business rules in dedicated services and models
- Use deterministic seeded randomness for reproducibility
- Make configuration external to code
- Keep hidden simulation state out of gameplay DTOs
