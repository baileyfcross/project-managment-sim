# Instructor guide

## Before class

1. Build or receive the Windows application image. On a Windows development machine with a Java 21 JDK and Node.js/npm, run `.\gradlew.bat packageApp`. The result is under `build\package\windows\SoftwareProjectManagementSimulator`; start it with `SoftwareProjectManagementSimulator.exe`.
2. The packaged application runs locally with its bundled runtime and frontend. It does not need Java installed on student machines, an internet connection, a browser, or an external service.
3. Use the default Small Business Web Application scenario for a short classroom exercise. It has a 20-week deadline and a $400,000 budget.
4. For a comparison exercise, give groups the same scenario, initial team, and numeric seed. Ask them to record their major choices. Blank seeds generate a seed automatically.

## Suggested class exercise

For a 60- to 90-minute activity, allow 10 minutes for setup and strategy, 25 to 40 minutes for teams to manage the project, and 20 to 30 minutes for report comparison and discussion. Teams can focus on staffing, testing, work intensity, concurrency, engineering approach, technical debt, and customer feature requests. The simulation is not designed to have one optimal decision sequence.

At the end of a run, review the outcome and category scores first. Then use the progress, cost, quality/rework, team health, team size, debt, and schedule-pressure charts to connect decisions to later project conditions. The decision and event timeline records when choices and resolved events occurred. The report's causal findings identify evidence-backed patterns in the observed run, but should be discussed as possible explanations rather than proof of causation.

Useful discussion prompts:

- Which early choices had the largest delayed consequences?
- Did improving throughput increase testing demand, defects, fatigue, or future cost?
- How did the team respond when events changed scope or introduced pressure?
- Did the schedule forecast reflect the eventual release date? What work or risk was not forecast?
- Which tradeoffs would the group change in a replay, and what outcomes do they expect?

## Instructor history inspector

The instructor inspector selects a recorded week and separates metrics into two groups:

- **Visible to Student at the Time** shows the manager-facing values retained for that week.
- **Hidden State at the Time** shows internal progress, defects/rework, and other recorded metrics that were withheld during play.

Resolved event details show their recorded effects. The inspector is available only after termination. The simulator does not fabricate historical employee-level values or reconstruct exact past probabilities that were not stored.

## Reproducibility and limitations

The report displays the scenario, seed, initial team summary, ending week, termination reason, and headline metrics. **Copy seed** copies the seed for recording or sharing. **Run Again With Same Seed** resets the simulation using the same scenario, original team, and seed, but starts without prior management decisions. To compare strategies, record each group's choices and compare reports. Identical seeds do not make different decision sequences equivalent.

The application currently provides an on-screen report and seed copying, not JSON/CSV report export, import, or a persistent multi-run library. Preserve results by recording report values in classroom materials. The final report is generated from the run history and does not simulate additional weeks.
