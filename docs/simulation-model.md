# Simulation Model

## Week numbering and weekly loop

Week 0 is the initial setup state. The first advance simulates Week 1. Each completed turn updates the displayed project week and records exactly one snapshot for the end of that same week.

The weekly calculation order is: block advancement while a required event is pending; begin the week and clear weekly flow; activate hires whose recruiting delay elapsed; progress onboarding; calculate mentoring, coordination, and capacity from start-of-week employee state, technical debt, and selected policies; reserve developer capacity for known rework and debt pay-down; repair known rework and create regression demand; attempt new work using concurrency-adjusted readiness; record out-of-sequence work and dependency uncertainty; perform DevOps deployment work; inspect available QA work and discover defects; update perceived and true progress; calculate schedule pressure from visible phase workload, known rework, test backlog, and current role capacities; update fatigue and morale; record overtime streaks; calculate payroll and overtime cost; resolve seeded departures; update technical debt from this week's engineering choices and pay-down; evaluate release, budget, and deadline; generate an eligible end-of-week event; and record the end-of-week snapshot and causal trace. Management policies set before advancing apply during that simulated week. Debt entering the week affects current work; debt changes are applied at week end. There are no mid-week circular recalculations.

## Productivity

Productivity is computed by a dedicated model and work throughput uses role-specific capacity, never total team size. Developers contribute to requirements, design, development, and known rework. QA engineers contribute to testing and discovery. DevOps engineers contribute to deployment. Project Managers contribute to coordination only. Junior, mid-level, and senior employees have configurable experience multipliers. Active hires contribute in proportion to onboarding effectiveness; initial staff begin fully integrated, while post-start hires join after an experience-based recruiting delay and begin at configured initial effectiveness. Only active employees receive work-intensity effort and fatigue changes; pending hires retain their state until activation.

## Defects

The defect model creates aggregate defective-work quantities from attempted development and deployment work. The bounded probability reflects configured base rate, employee experience, onboarding, fatigue, coordination, schedule pressure, mentoring coverage, work intensity, technical debt, engineering approach, dependency uncertainty, and prior unknown defects in upstream phases. Propagation is proportional to hidden upstream defect stock, so early discovery reduces downstream risk without a scripted late penalty. A seeded Gaussian approximation to an aggregate binomial avoids one random draw per small work unit.

## Unknown and known rework

Each phase tracks scenario scope, base work remaining, correct work completed, unknown rework, known rework, and repaired work. Attempted work reduces base scope and splits into correct work and unknown rework. Unknown rework is work the project currently believes is correct. It contributes to perceived progress but not true progress. Testing discoveries remove it from perceived progress and move it into visible known rework, so displayed progress can stall or decline. Forecasts worsen from known rework, not from hidden defects. When correctly repaired, known work contributes to true progress; an unsuccessful repair returns defective work to the unknown stock. The ledger also records weekly attempted work, defects created and discovered, and rework completed.

## Reproducibility

The Java engine stores its active seed and uses one seeded random generator for stochastic simulation decisions. An empty setup seed requests a newly generated seed; a supplied signed 64-bit integer is used directly. Same scenario, seed, team, and decisions result in identical weekly snapshots.

## QA

Testing is not treated as equivalent to feature development. Development and deployment output adds work to a bounded, testable queue. QA can inspect only that queue, at configured throughput based on effective QA capacity. QA effective capacity reflects staff count, experience, onboarding, fatigue, coordination, and mentoring effects. Testing priority has three settings: LOW reduces inspection and discovery effort, NORMAL is baseline, and HIGH increases them. Low testing priority does not directly raise defect probability; it leaves more defects hidden and allows backlog to accumulate. Discoveries are seeded, bounded by available unknown defects, and converted to known rework. The player sees a backlog category, not the hidden defect stock.

Phase readiness permits overlap with a configurable availability floor. The concurrency policy sets the readiness floor: Sequential limits overlap, Moderate provides normal overlap, and Aggressive allows more downstream work before upstream phases are mature. Requirements work can begin immediately. Design availability rises with perceived requirements progress; development availability rises with perceived design progress; testing work is produced in proportion to completed development output; deployment readiness rises with perceived testing completion. Work attempted while upstream work is incomplete contributes to internal dependency uncertainty and can increase defect risk. As upstream work becomes complete, that uncertainty falls to zero. Coordination support modestly reduces concurrency-related overhead and risk, but does not remove design dependencies.

When known rework exists, a configurable share of developer capacity is assigned to repairs. Correct repairs increase correct work; defective repairs return to unknown rework. Correct repairs create a configurable regression-testing demand. QA retesting consumes the same bounded testing queue. This causes repair and retest effort to extend the schedule and payroll instead of applying a flat cost penalty.

## Schedule pressure, overtime, fatigue, and morale

Schedule pressure is a bounded 0 to 1 indicator derived from manager-visible requirements/design/development load, testing load and backlog, known rework, role-specific effective capacity, and remaining deadline. It does not use unknown rework or true progress. The displayed pressure category is Low, Manageable, High, Severe, or Critical.

The selected work intensity uses configurable 40/48/60-hour default weeks and 1.00/1.17/1.35 effort multipliers. It affects current active staff capacity immediately and uses diminishing returns compared with hours alone. Overtime cost is a configurable premium proportional to hours above the standard week, rather than a blanket multiplier of all salary. Cost forecasts assume the currently selected intensity continues through the projected duration and do not anticipate future departures.

Fatigue is retained per employee on a 0 to 1 scale. Start-of-week fatigue modifies that week's work: productivity impact is nonlinear (squared fatigue), defect probability rises with fatigue, QA inspection capacity and detection effectiveness decline, mentoring capacity falls modestly, and fatigue reduces the coordination contribution of experienced managers. Fatigue grows gradually with Increased and Crunch work, elevated schedule pressure, prior overtime streak, and nonlinear accumulation at higher fatigue. Sustainable work reduces fatigue gradually; one recovery week cannot reset severe fatigue.

End-of-week work intensity and schedule pressure update each active employee's fatigue and morale. Morale responds to fatigue, intensity, schedule pressure, and overtime streak, and recovers gradually under sustainable low-pressure work. Dashboard summaries show average fatigue and team morale categories, not exact individual values.

## Scope changes and project events

The event generator uses the same seeded random generator as work and turnover. Configurable event probability, event-family weights, scenario risk factors, minimum spacing, cooldown, and unresolved-event limits avoid event spam. Context changes event weights: customer requests vary with elapsed project time; dependency and integration risks depend on phase maturity, concurrency, DevOps support, technical debt, pressure, and hidden defects; security events respond to debt, fatigue, and engineering shortcuts. The engine does not generate a new decision event mid-week.

Each important event has an ID, type, title, player-facing description, generation week, available options, and a resolved result. Results retain the resolution week and internal outcome metadata for simulation history. A pending decision blocks advancement. The gameplay DTO exposes only visible descriptions, choices, results, and qualitative impact, not probabilities, feature work units, hidden rework, internal outcome metadata, or risk formulas. The internal event and decision history is retained in weekly snapshots for later causal analysis.

Customer feature requests offer Accept, Defer, and Reject. Acceptance adds nonnegative phase-specific work to both the project's total scope and remaining work. ScopeChangeModel calculates ripple rework from feature size and the amount of existing work completed in affected phases, then adds regression-testing demand to the existing QA backlog. Thus an otherwise equivalent request accepted later tends to be more disruptive without using a fixed week cutoff. Deferral records the feature for a future release without current work; rejection records the declined opportunity. A request is resolved once and is not re-offered.

Requirements misunderstandings, dependency problems, failed integrations, security vulnerabilities, and technical-debt issues use the same event and decision pathway. Their choices add known rework, regression demand, or bounded technical debt. These effects flow into normal capacity, testing, forecast, schedule pressure, staffing, and cost models instead of applying a direct schedule or budget penalty.

## Technical debt and engineering approach

Technical debt is a bounded project-level value from 0 to 1 representing shortcuts that make future changes harder. It is distinct from hidden defects: a project can have substantial debt with few known defects. Start-of-week debt applies a modest nonlinear productivity and quality penalty, including harder repairs and greater integration/event risk. It does not directly subtract money. Its cost emerges through additional work and longer payroll.

Careful engineering slightly reduces immediate throughput while limiting debt and defect creation. Balanced is the baseline. Cut Corners provides an immediate throughput benefit and higher defect risk, while accumulating more debt. Technical Debt Priority can be Ignore, Normal, or Pay Down. Pay Down reserves configured developer capacity; debt is reduced gradually at week end, so the lower debt benefits later weeks rather than the current week's calculations. High pressure, crunch, and immature concurrent work can also contribute to debt accumulation. All debt remains bounded.

Turnover risk has a small baseline and rises with squared fatigue, morale below its configured baseline, schedule pressure, and consecutive overtime weeks. Each active employee's departure decision uses the simulation's seeded generator. Fatigue thresholds alone do not remove staff. Departures take effect for future weeks, stop future payroll and capacity, and reduce mentoring and coordination through the existing team models. The current week's payroll includes team members who worked that week before leaving. Replacement hiring still uses recruiting delays, payroll activation, onboarding, and mentoring from Phase 2. Overtime affects turnover and quality through the existing models; events are generated separately at eligible week ends.

## Fatigue

Fatigue is normalized from 0 to 1. It accumulates under increased or crunch work and recovers gradually under sustainable work. High fatigue reduces productivity and increases defect probability, which creates delayed effects.

## Hiring and onboarding

The Week 0 team builder creates fully integrated initial staff using configurable default experience levels by role. In-project hires are selected by role, experience, and quantity. A configured one-time hiring fee is charged when the decision is accepted. Employees remain pending and incur no payroll or productivity until their recruiting delay expires; when they join, their weekly salary begins and onboarding starts at the configured initial effectiveness. Onboarding moves toward full effectiveness over the experience-specific duration, with progress slowed by low mentoring coverage. Junior employees and onboarding staff create mentoring demand; mid-level and senior employees provide capacity, with a bounded direct-productivity cost for mentoring.

## Coordination and schedule pressure

Coordination overhead follows a bounded square-root curve based on team pairs. Project managers and senior staff reduce the penalty. Schedule pressure rises when work remains high and time is short. This influences morale and overtime cost, and is a contextual factor for selected event risks. Payroll uses role- and experience-specific salary settings; the staffing view includes active payroll, pending hires, one-time hiring costs, and a cost forecast.

## Forecast and completion

Quality Health uses only visible known rework, testing backlog, and current discoveries. It never reads unknown rework. The schedule forecast uses current perceived remaining work, known rework, active developer, QA, and DevOps capacity, current technical-debt productivity, concurrency and engineering policies, and regression-testing backlog. It does not use unknown rework, unresolved future events, future scope requests, or turnover. It is allowed to be wrong. A project is release-ready when all current base work is attempted, known rework is cleared, testable work is inspected, and deployment scope is completed. Unknown rework does not block release; its released-defect count is retained internally.

## Scoring

Scoring runs only after termination and is calculated in Java from the completed run history. The 100-point score has five bounded categories: Schedule (25), Budget (25), Quality (25), Team Sustainability (15), and Customer Value (10). The score and category explanations are shown with the final report; formulas and interpretation are documented in [scoring.md](scoring.md).

The final report is assembled from recorded weekly snapshots and event/decision history. It does not simulate additional weeks. Historical charts include manager-visible and internal series; internal truth is available only after the run ends in the instructor view. Deterministic causal findings summarize supported patterns with the observed weeks and values as evidence. They are rule-based explanations, not proof that an individual decision caused an outcome.

The report also records the scenario, seed, initial team summary, ending week, termination reason, and headline metrics. Copying the seed and replaying creates a fresh run with the same scenario/team/seed but resets prior player decisions. Exporting/importing run files and keeping a collection of historical runs are not implemented.
