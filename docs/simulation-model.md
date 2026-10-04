# Simulation Model

## Weekly loop

The simulation advances one week at a time. Each week applies management decisions, updates staffing and onboarding, calculates productivity, allocates work and rework, evaluates QA and defects, tests and rework flow, updates fatigue, computes cost, and records a snapshot.

## Productivity

Productivity is computed by a dedicated productivity model. It includes role, experience, fatigue, onboarding, mentoring load, coordination overhead, overtime, and schedule pressure. The formula is exposed as a result object that can be inspected in the debug report.

## Defects

The defect model creates defects from attempted work. Defect creation is influenced by experience, fatigue, coordination overhead, schedule pressure, and onboarding. They are then moved into unknown rework until QA discovers them.

## Unknown and known rework

Unknown rework represents defects that exist but have not been discovered. Once testing finds defects, they are converted into known rework. Developers then use future capacity to fix known rework. This creates delayed schedule pressure and cost.

## QA

Testing is not treated as equivalent to feature development. QA engineers contribute by discovering hidden defects and reducing the time spent on future rework. The simulation tracks insufficient QA as a cause of hidden defect accumulation.

## Fatigue

Fatigue is normalized from 0 to 1. It accumulates from overtime and schedule pressure and recovers more slowly during sustainable work. High fatigue reduces productivity and increases defect probability, which creates delayed effects.

## Hiring and onboarding

New employees are not fully effective immediately. They require onboarding and mentoring, and their early productivity is reduced. Rapid hiring increases coordination overhead and mentoring demand, but productive teams can still recover over time.

## Coordination and schedule pressure

Large teams create coordination overhead, which reduces effective productivity. Schedule pressure rises when work remains high and time is short. This influences delayer states, morale, overtime risk, and event probability.

## Scoring

The final project score combines schedule, budget, quality, team sustainability, and customer value. It is intentionally balanced so fast and cheap projects with poor quality do not escape punishment.
