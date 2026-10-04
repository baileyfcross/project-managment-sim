# Architecture Overview

The simulator follows a layered architecture that separates the domain model from the UI and simulation engine.

## Backend

The Java backend contains the domain model, configuration, scenarios, simulation engine, and reporting classes. Domain objects represent employees, projects, work states, cost, and health metrics. The simulation engine coordinates a weekly loop and keeps a deterministic random seed for reproducible scenarios.

## Frontend

The frontend is a Vite-based TypeScript application rendered inside JavaFX WebView. It provides screens for scenario selection, team building, the main dashboard, and history display. The UI remains focused on presenting model state and sending decisions to the Java bridge.

## Java and JavaScript Bridge

The JavaBridge object exposes a narrow API to the frontend. It serializes simulation state into JSON and accepts actions such as starting a scenario, changing team composition, setting work intensity, and advancing the week. The frontend does not manipulate hidden simulation variables directly.

## Gradle and Vite

Gradle is responsible for coordinating the build. It triggers npm install, executes the Vite production build, and compiles the Java application. The generated frontend is copied into Java resources for embedding.

## Simulation Layers

- Domain: project, employee, budget, work state, health, snapshots
- Simulation: productivity, defects, fatigue, coordination, schedule pressure, forecast
- Events: project events and scope changes
- Reporting: final results and post-simulation analysis

## Design Principles

- Separate simulation logic from UI code
- Do not put formulas in controllers
- Keep business rules in dedicated services and models
- Use deterministic seeded randomness for reproducibility
- Make configuration external to code
