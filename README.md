# Software Project Management Simulator

The Software Project Management Simulator is a desktop learning application for students to act as project managers in a simulated software engineering project. The simulator models staffing, onboarding, fatigue, defects, rework, budget pressure, scope change, and delayed consequences of management decisions.

## Purpose

This project is designed for classroom use in software engineering courses. Students make decisions that affect schedule, quality, morale, and cost, while the underlying simulation exposes how short-term tradeoffs create delayed project consequences.

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
- Gradle wrapper or installed Gradle 8+

## Run

```bash
./gradlew run
```

## Test

```bash
./gradlew test
```

## Build

```bash
./gradlew build
```

## Scenario Model

Scenarios are loaded from JSON in src/main/resources/scenarios. Each scenario defines project scope, deadline, budget, and starting parameters. The simulator stores seeded randomness to allow deterministic repeats for classroom comparison.

## Configuration Model

Simulation tuning values live in JSON configuration files in src/main/resources/configuration. These include productivity, fatigue, onboarding, cost, and quality parameters.

## Simulation Highlights

The engine tracks both perceived progress and true progress. Users normally see perceived progress, while the engine privately tracks unknown rework and defects. This allows the project to appear healthy while hidden problems accumulate.

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
