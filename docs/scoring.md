# Final score guide

The final score is calculated in Java after a simulation terminates. It totals 100 points across five bounded categories. Each category is clamped to its point range, and the total is rounded to two decimal places.

| Category | Maximum |
| --- | ---: |
| Schedule | 25 |
| Budget | 25 |
| Quality | 25 |
| Team Sustainability | 15 |
| Customer Value | 10 |

The score is intended to support reflection on tradeoffs, not to identify a single universally correct management strategy. Random events and seeded stochastic work can lead to different outcomes. Compare strategies using the same scenario, starting team, and seed, while recognizing that replay starts a fresh run and does not repeat the student's decisions.

## Schedule (25 points)

For a released project, let `lateWeeks = max(0, actualWeek - deadline)` and `lateRatio = lateWeeks / deadline`:

`25 x clamp(1 - 0.55 x lateRatio - 0.45 x lateRatio^2, 0, 1)`

A release on or before its deadline earns 25 points. Late releases lose points progressively. An incomplete project receives at most 15 points, scaled by final true progress:

`min(15, 15 x trueProgress)`

## Budget (25 points)

Let `overrun = max(0, actualCost / budget - 1)`. A released project receives:

`25 x clamp(1 - 0.75 x overrun - 0.25 x overrun^2, 0, 1)`

Staying at or below budget earns 25 points. An incomplete project has its budget score multiplied by final true progress and capped at 14 points, so spending little without delivering the project cannot earn a full budget score.

## Quality (25 points)

The report uses total project scope with a floor of one work unit for normalization. `hiddenDefects` is the greater of released defect work and remaining unknown rework. The quality index is:

`1 - 2.5 x hiddenDefects / scope - 0.9 x knownRework / scope - 0.25 x testingBacklog / scope - 0.25 x technicalDebt`

The quality score is `25 x clamp(qualityIndex, 0, 1)`. Hidden defects are weighted most heavily; visible unfinished rework, testing backlog, and technical debt also reduce the score. Technical debt is already normalized by the simulation.

## Team Sustainability (15 points)

The index uses average fatigue, peak fatigue, average morale, work-intensity load, the fraction of weeks with average fatigue at or above 70%, and departures as a fraction of the initial team size. Sustainable, Increased, and Crunch weeks contribute intensity values of 0, 1, and 1.5 respectively. Ratios are averaged across recorded weeks.

`1 - 0.3 x averageFatigue - 0.1 x peakFatigue - 0.25 x (1 - averageMorale) - 0.2 x intensityLoad - 0.1 x highFatigueWeekRatio - 0.05 x departureRatio`

The score is `15 x clamp(index, 0, 1)`. Departures are a small component rather than an automatic failure; fatigue, morale, and sustained intensity are also considered.

## Customer Value (10 points)

For each customer feature request, the report uses the event's configured potential value. Accepting contributes potential multiplied by final true project progress, deferring contributes 45% of potential, and rejecting contributes 8%. The score is the resulting value divided by the total potential, scaled to ten points and clamped to range. If no customer feature request occurred, the category is neutral at 5 points.

Accepting requests adds work and may affect schedule, budget, and quality. This category is therefore intentionally balanced with the other score categories rather than rewarding every accepted request as fully delivered.

## Reading the result

- Category explanations describe the run data used by the score. They do not forecast how a different decision would have changed the run.
- A high score does not imply every decision was prudent in every context. Review the timeline and charts alongside category scores.
- Causal findings are deterministic summaries of observed evidence. They are not controlled experiments or proof of causation.
- Exact formulas and scores belong to the instructor-facing final report. During active play, hidden quality and true progress remain hidden from the student-facing state.
