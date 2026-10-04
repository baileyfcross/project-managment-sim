# Simulation Model

## Week numbering and weekly loop

Week 0 is the initial setup state. The first advance simulates Week 1. Each completed turn updates the displayed project week and records exactly one snapshot for the end of that same week.

The weekly calculation order is: calculate capacity; repair known rework; attempt remaining phase work and create defects; allow QA to discover defects; update schedule pressure, fatigue, and morale; apply seeded turnover and events; calculate costs; evaluate completion; and record one end-of-week snapshot.

## Productivity

Productivity is computed by a dedicated productivity model and work throughput uses developer capacity, not the aggregate capacity of non-development roles. The initial team is ready to contribute at the start of Week 1; later hiring and onboarding are outside this Phase 1 foundation.

## Defects

The defect model creates defects from attempted work. Defect creation is influenced by fatigue, coordination overhead, and schedule pressure. The model supports an onboarding modifier, but initial Phase 1 staff begin ready to work and onboarding is not advanced during play. Defects move into unknown rework until QA discovers them.

## Unknown and known rework

Unknown rework represents defects that exist but have not been discovered. Once testing finds defects, they are converted into known rework. Developers then use future capacity to fix known rework. This creates delayed schedule pressure and cost. The player sees perceived progress, which includes unknown defective work, but not exact hidden stocks.

## Reproducibility

The Java engine stores its active seed and uses one seeded random generator for stochastic simulation decisions. An empty setup seed requests a newly generated seed; a supplied signed 64-bit integer is used directly. Same scenario, seed, team, and decisions result in identical weekly snapshots.

## QA

Testing is not treated as equivalent to feature development. QA engineers contribute by discovering hidden defects and reducing the time spent on future rework. The simulation tracks insufficient QA as a cause of hidden defect accumulation.

## Fatigue

Fatigue is normalized from 0 to 1. It accumulates under increased or crunch work and recovers gradually under sustainable work. High fatigue reduces productivity and increases defect probability, which creates delayed effects.

## Hiring and onboarding

In-project hiring and onboarding are planned for a later phase. The initial team builder configures the staff present at Week 0; those employees begin with full project onboarding effectiveness.

## Coordination and schedule pressure

Large teams create coordination overhead, which reduces effective productivity. Schedule pressure rises when work remains high and time is short. This influences delayer states, morale, overtime risk, and event probability.

## Scoring

Post-simulation scoring and analysis are planned for a later phase.
