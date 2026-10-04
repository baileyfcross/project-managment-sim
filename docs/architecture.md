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

- Domain: project, employee, pending hire, onboarding state, budget, work state, health, snapshots
- Simulation: experience-based productivity and cost, hiring delays, mentoring, onboarding, defects, fatigue, coordination, schedule pressure, forecast
- Events: project events and scope changes
- Reporting: final results and post-simulation analysis

## Design Principles

- Separate simulation logic from UI code
- Do not put formulas in controllers
- Keep business rules in dedicated services and models
- Use deterministic seeded randomness for reproducibility
- Make configuration external to code
- Keep hidden simulation state out of gameplay DTOs
