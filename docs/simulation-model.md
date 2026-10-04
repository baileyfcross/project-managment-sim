# Simulation Model

## Week numbering and weekly loop

Week 0 is the initial setup state. The first advance simulates Week 1. Each completed turn updates the displayed project week and records exactly one snapshot for the end of that same week.

The weekly calculation order is: activate hires whose recruiting delay has elapsed; calculate capacity and mentoring; repair known rework; attempt remaining phase work and create defects; allow QA to discover defects; update schedule pressure, fatigue, and morale; progress onboarding; apply seeded turnover and events; calculate costs; evaluate completion; and record one end-of-week snapshot.

## Productivity

Productivity is computed by a dedicated productivity model and work throughput uses developer capacity, not aggregate team capacity. Junior, mid-level, and senior employees have configurable experience multipliers. Active hires contribute in proportion to onboarding effectiveness; initial staff begin fully integrated, while post-start hires join after a role-independent, experience-based recruiting delay and begin at configured initial effectiveness. QA and DevOps have separate effective capacities, and neither adds feature-development throughput.

## Defects

The defect model creates defects from attempted development work. Defect creation is influenced by fatigue, coordination overhead, schedule pressure, and the active developers' onboarding deficit. Defects move into unknown rework until QA discovers them. Discovery capacity is based on effective QA capacity.

## Unknown and known rework

Unknown rework represents defects that exist but have not been discovered. Once testing finds defects, they are converted into known rework. Developers then use future capacity to fix known rework. This creates delayed schedule pressure and cost. The player sees perceived progress, which includes unknown defective work, but not exact hidden stocks.

## Reproducibility

The Java engine stores its active seed and uses one seeded random generator for stochastic simulation decisions. An empty setup seed requests a newly generated seed; a supplied signed 64-bit integer is used directly. Same scenario, seed, team, and decisions result in identical weekly snapshots.

## QA

Testing is not treated as equivalent to feature development. QA engineers contribute by discovering hidden defects and reducing the time spent on future rework. Discovery capacity scales with role, experience, fatigue, and team coordination.

## Fatigue

Fatigue is normalized from 0 to 1. It accumulates under increased or crunch work and recovers gradually under sustainable work. High fatigue reduces productivity and increases defect probability, which creates delayed effects.

## Hiring and onboarding

The Week 0 team builder creates fully integrated initial staff using configurable default experience levels by role. In-project hires are selected by role, experience, and quantity. A configured one-time hiring fee is charged when the decision is accepted. Employees remain pending and incur no payroll or productivity until their recruiting delay expires; when they join, their weekly salary begins and onboarding starts at the configured initial effectiveness. Onboarding moves toward full effectiveness over the experience-specific duration, with progress slowed by low mentoring coverage. Junior employees and onboarding staff create mentoring demand; mid-level and senior employees provide capacity, with a bounded direct-productivity cost for mentoring.

## Coordination and schedule pressure

Coordination overhead follows a bounded square-root curve based on team pairs. Project managers and senior staff reduce the penalty. Schedule pressure rises when work remains high and time is short. This influences morale, overtime cost, and event probability. Payroll uses role- and experience-specific salary settings; the staffing view includes active payroll, pending hires, one-time hiring costs, and a cost forecast.

## Scoring

Post-simulation scoring and analysis are planned for a later phase.
