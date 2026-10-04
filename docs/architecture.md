# Architecture Overview

The simulator follows a layered architecture that separates the domain model from the UI and simulation engine.

## Backend

The Java backend contains the domain model, configuration, scenarios, simulation engine, and reporting classes. Domain objects represent employees, projects, work states, cost, and health metrics. The simulation engine coordinates a weekly loop and keeps a deterministic random seed for reproducible scenarios.

## Frontend

The frontend is a Vite-based TypeScript application rendered inside JavaFX WebView. It provides the initial team builder, active-project dashboard, and in-project Manage Team panel. The UI presents staffing DTOs and hiring previews, then sends hiring and work-intensity decisions to the Java bridge.

## Java and JavaScript Bridge

The JavaBridge exposes only JSON-based setup and gameplay operations: read setup values, update the initial team, start with a selected seed, read staffing state and hiring options, submit a hire decision, change work intensity, and advance a week. It does not expose domain objects or simulation history to normal gameplay. Java owns all authoritative state and calculations; TypeScript renders the returned DTOs.

## Gradle and Vite

Gradle is responsible for coordinating the build. It runs reproducible `npm ci` only when package inputs require it, executes the Vite production build when frontend inputs change, and includes the generated frontend in Java resources for embedding.

## Simulation Layers

- Domain: project, employee, pending hire, onboarding state, budget, per-phase work stocks, testing priority, health, snapshots
- Simulation: experience-based productivity and cost, hiring delays, mentoring, onboarding, phase readiness, work allocation, defect generation and discovery, rework, QA backlog, fatigue, coordination, schedule pressure, forecast

Focused models own their formulas: `WorkAllocationModel` divides developer capacity, `PhaseReadinessModel` provides gradual phase availability, `NewWorkModel` attempts new developer and DevOps work, `DefectModel` bounds and samples defective work, `ReworkModel` repairs known defects, `TestingModel` processes the bounded testable queue and discovers defects, `QualityHealthModel` derives visible quality status, `FatigueModel` applies role effects and recovery, `MoraleModel` updates employee morale, and `TurnoverModel` resolves seeded per-employee departures. `SimulationEngine` coordinates the weekly order and records snapshots.

`WorkState` is the authoritative per-phase work ledger. It distinguishes scenario base scope and remaining work from correct completion, unknown and known rework, test backlog, and weekly flow metrics. `WeeklySnapshot` retains both manager-visible and hidden metrics; `SimulationStateDto` includes only perceived phase progress, known rework, current discoveries, testing status and priority, and QA capacity. Neither the gameplay DTO nor the forecast exposes true progress or unknown rework.

Phase 4 human state is stored on `Employee`: normalized fatigue, morale, and consecutive overtime weeks. `ProductivityModel` uses the configured intensity effort separately from fatigue productivity impact. `SchedulePressureModel` uses only perceived remaining work, known rework, test backlog, role capacities, and deadline. `TurnoverModel` uses the engine's seeded random generator; departed staff are removed from active team calculations while their departure records remain in weekly history. The gameplay DTO exposes aggregate health categories and current overtime cost, while exact employee state and turnover rates stay internal.
- Events: project events and scope changes
- Reporting: final results and post-simulation analysis

## Design Principles

- Separate simulation logic from UI code
- Do not put formulas in controllers
- Keep business rules in dedicated services and models
- Use deterministic seeded randomness for reproducibility
- Make configuration external to code
- Keep hidden simulation state out of gameplay DTOs
