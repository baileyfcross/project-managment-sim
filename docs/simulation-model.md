# Simulation Model

## Week numbering and weekly loop

Week 0 is the initial setup state. The first advance simulates Week 1. Each completed turn updates the displayed project week and records exactly one snapshot for the end of that same week.

The weekly calculation order is: begin the week; activate hires whose recruiting delay elapsed; progress onboarding; calculate mentoring, coordination, and role-specific capacity; reserve developer capacity for rework; repair known rework and create regression demand; attempt readiness-limited developer work; perform DevOps deployment work; make completed output testable; inspect available QA work and discover defects; update perceived and true progress; update schedule pressure, fatigue, and morale; apply existing seeded turnover and events; calculate cost and forecast; evaluate release, budget, and deadline; then record the end-of-week snapshot. There are no mid-week circular recalculations.

## Productivity

Productivity is computed by a dedicated model and work throughput uses role-specific capacity, never total team size. Developers contribute to requirements, design, development, and known rework. QA engineers contribute to testing and discovery. DevOps engineers contribute to deployment. Project Managers contribute to coordination only. Junior, mid-level, and senior employees have configurable experience multipliers. Active hires contribute in proportion to onboarding effectiveness; initial staff begin fully integrated, while post-start hires join after an experience-based recruiting delay and begin at configured initial effectiveness.

## Defects

The defect model creates aggregate defective-work quantities from attempted development and deployment work. The bounded probability reflects configured base rate, employee experience, onboarding, fatigue, coordination, schedule pressure, mentoring coverage, work intensity, and prior unknown defects in upstream phases. Propagation is modest and proportional to hidden upstream defect stock, so early discovery reduces downstream risk without a scripted late penalty. A seeded Gaussian approximation to an aggregate binomial avoids one random draw per small work unit.

## Unknown and known rework

Each phase tracks scenario scope, base work remaining, correct work completed, unknown rework, known rework, and repaired work. Attempted work reduces base scope and splits into correct work and unknown rework. Unknown rework is work the project currently believes is correct. It contributes to perceived progress but not true progress. Testing discoveries remove it from perceived progress and move it into visible known rework, so displayed progress can stall or decline. Forecasts worsen from known rework, not from hidden defects. When correctly repaired, known work contributes to true progress; an unsuccessful repair returns defective work to the unknown stock. The ledger also records weekly attempted work, defects created and discovered, and rework completed.

## Reproducibility

The Java engine stores its active seed and uses one seeded random generator for stochastic simulation decisions. An empty setup seed requests a newly generated seed; a supplied signed 64-bit integer is used directly. Same scenario, seed, team, and decisions result in identical weekly snapshots.

## QA

Testing is not treated as equivalent to feature development. Development and deployment output adds work to a bounded, testable queue. QA can inspect only that queue, at configured throughput based on effective QA capacity. QA effective capacity reflects staff count, experience, onboarding, fatigue, coordination, and mentoring effects. Testing priority has three settings: LOW reduces inspection and discovery effort, NORMAL is baseline, and HIGH increases them. Low testing priority does not directly raise defect probability; it leaves more defects hidden and allows backlog to accumulate. Discoveries are seeded, bounded by available unknown defects, and converted to known rework. The player sees a backlog category, not the hidden defect stock.

Phase readiness permits overlap with a configurable availability floor. Requirements work can begin immediately. Design availability rises with perceived requirements progress; development availability rises with perceived design progress; testing work is produced in proportion to completed development output; deployment readiness rises with perceived testing completion. No hard phase-completion gates are used.

When known rework exists, a configurable share of developer capacity is assigned to repairs. Correct repairs increase correct work; defective repairs return to unknown rework. Correct repairs create a configurable regression-testing demand. QA retesting consumes the same bounded testing queue. This causes repair and retest effort to extend the schedule and payroll instead of applying a flat cost penalty.

## Fatigue

Fatigue is normalized from 0 to 1. It accumulates under increased or crunch work and recovers gradually under sustainable work. High fatigue reduces productivity and increases defect probability, which creates delayed effects.

## Hiring and onboarding

The Week 0 team builder creates fully integrated initial staff using configurable default experience levels by role. In-project hires are selected by role, experience, and quantity. A configured one-time hiring fee is charged when the decision is accepted. Employees remain pending and incur no payroll or productivity until their recruiting delay expires; when they join, their weekly salary begins and onboarding starts at the configured initial effectiveness. Onboarding moves toward full effectiveness over the experience-specific duration, with progress slowed by low mentoring coverage. Junior employees and onboarding staff create mentoring demand; mid-level and senior employees provide capacity, with a bounded direct-productivity cost for mentoring.

## Coordination and schedule pressure

Coordination overhead follows a bounded square-root curve based on team pairs. Project managers and senior staff reduce the penalty. Schedule pressure rises when work remains high and time is short. This influences morale, overtime cost, and event probability. Payroll uses role- and experience-specific salary settings; the staffing view includes active payroll, pending hires, one-time hiring costs, and a cost forecast.

## Forecast and completion

Quality Health uses only visible known rework, testing backlog, and current discoveries. It never reads unknown rework. The schedule forecast uses perceived remaining work, known rework, active developer, QA, and DevOps capacity, and regression-testing backlog. It never uses unknown rework or exact hidden defect counts. A project is release-ready when all scenario base work is attempted, known rework is cleared, testable work is inspected, and deployment scope is completed. Unknown rework does not block release; its released-defect count is retained internally.

## Scoring

Post-simulation scoring and analysis are planned for a later phase.
