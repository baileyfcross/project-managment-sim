# Software Project Management Simulator

The Software Project Management Simulator is a desktop learning application for students to act as project managers in a simulated software engineering project. The Phase 2 release supports initial and in-project staffing, recruiting delays, experience-based payroll and productivity, onboarding and mentoring, coordination overhead, weekly project work, QA, defects, rework, budget pressure, and repeatable seeded scenarios.

## Purpose

This project is designed for classroom use in software engineering courses. Students make initial staffing, hiring, and weekly work-intensity decisions that affect schedule, quality, morale, onboarding, and cost, while the underlying simulation exposes how short-term tradeoffs create delayed project consequences.

## Architecture

The project uses a layered design:

- Java backend for simulation and domain logic.
- JavaFX desktop shell with a JavaFX WebView frontend.
- Vite and TypeScript for the browser-like UI layer.
- Gradle to coordinate Java, frontend build, and packaging.

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
- docs/ - architecture and simulation documentation

## Development Requirements

- Java 21 or newer
- Node.js and npm for frontend build steps
- Gradle wrapper (Gradle 8.10.2)

Set `JAVA_HOME` to a Java 21 or newer JDK before building. The Gradle Java toolchain requires Java 21; Java 8 is not supported.

## Run on Windows

In PowerShell, ensure `JAVA_HOME` points to the installed JDK, then run:

```powershell
.\gradlew.bat run
```

The same Gradle application run task is available on other platforms with `./gradlew run`. Gradle builds the TypeScript frontend into local Java resources before launching JavaFX. The packaged desktop runtime does not require Node.js or internet access.

## Test on Windows

```powershell
.\gradlew.bat clean test
```

## Build on Windows

```powershell
.\gradlew.bat clean build
```

## Scenario Model

Scenarios are loaded from JSON in src/main/resources/scenarios. Each scenario defines project scope, deadline, budget, and starting parameters. The simulator stores seeded randomness to allow deterministic repeats for classroom comparison.

## Configuration Model

Simulation tuning values live in JSON configuration files in src/main/resources/configuration. These include experience-based salaries and starting experience by role, hiring delays and fees, onboarding, mentoring, coordination, productivity, fatigue, overtime, and quality parameters.

## Simulation Highlights

The engine tracks perceived progress separately from internal true progress and unknown rework. Gameplay receives only the perceived estimate and known project information; internal stocks remain in Java and snapshots and are not included in the normal gameplay JSON DTO. The in-project Manage Team panel shows active staff by role and experience, onboarding states, pending hires, salary and hiring-cost previews, payroll and cost forecasts, mentoring load, and coordination health.

Post-start hires are selected by role, experience, and quantity. Hiring fees are charged when the decision is accepted; salary and productivity begin only when recruiting delay expires. Hires then contribute at configured initial onboarding effectiveness and progress toward full contribution over their experience-based onboarding duration, moderated by mentoring coverage. Junior, mid-level, and senior staff have different salary and productivity values. Non-developer roles contribute to QA, DevOps, project management, mentoring, and coordination effects, but not feature-development throughput.

## Week and seed behavior

The project starts at Week 0, before any work. Each press of **Advance 1 Week** simulates the next week, advances any pending recruiting and onboarding, stores an end-of-week snapshot, and leaves the project at that week. After N advances, the current week and history length are both N.

Leave the seed field blank to have Java generate and display a seed, or enter a signed 64-bit integer to reproduce a run. All stochastic simulation decisions use the seeded generator. Repeating a scenario with the same seed, team, and decisions reproduces the same weekly results.

## Frontend/backend boundary

Java is authoritative for setup, team counts, staffing options, project state, cost calculations, and simulation outcomes. TypeScript renders DTOs and sends requested actions through the narrow JSON-based Java bridge; it does not maintain an independent simulation state.

## Adding a Scenario

1. Create a JSON file under src/main/resources/scenarios.
2. Give it a unique id.
3. Ensure it includes a name, deadline, budget, and phase work totals.
4. Reference it from the scenario loader or default scenario selection.

## Adding a New Event Type

1. Add a new event type to the EventType enum.
2. Implement its generation logic in the event generator or simulation engine.
3. Add a response path in the UI bridge when student decision support is required.
4. Cover the behavior with a unit test.

## License

This project is intended for educational use and classroom simulation exercises.
