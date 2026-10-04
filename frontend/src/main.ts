type TeamCounts = Record<string, number>;

type SetupState = {
  scenarioId: string;
  scenarioName: string;
  deadlineWeeks: number;
  budget: number;
  teamCounts: TeamCounts;
  weeklyPayroll: number;
  projectedPayroll: number;
  contingency: number;
};

type SimulationState = {
  week: number;
  seed: string;
  deadline: number;
  budget: number;
  spent: number;
  remainingBudget: number;
  burnRate: number;
  forecastCost: number;
  estimatedCompletionWeek: number;
  perceivedProgress: number;
  teamCounts: TeamCounts;
  scheduleHealth: string;
  budgetHealth: string;
  qualityHealth: string;
  moraleHealth: string;
  recentMessages: string[];
  workIntensity: string;
  complete: boolean;
  phaseProgress: Record<string, number>;
  knownRework: number;
  defectsDiscoveredThisWeek: number;
  testingBacklogStatus: string;
  testingPriority: string;
  qaCapacity: number;
  workIntensityHours: number;
  averageFatigueHealth: string;
  turnoverRisk: string;
  employeesDepartedThisWeek: number;
  overtimeCost: number;
  scopeExpansion: number;
  acceptedFeatureCount: number;
  deferredFeatureCount: number;
  rejectedFeatureCount: number;
  technicalDebtHealth: string;
  concurrencyPolicy: string;
  engineeringApproach: string;
  technicalDebtPriority: string;
  pendingEvents: ProjectEventView[];
  eventHistory: ProjectEventView[];
};

type ProjectEventView = {
  id: string;
  type: string;
  title: string;
  description: string;
  week: number;
  options: Array<{ id: string; label: string; description: string }>;
  impactEstimate: string;
  resolved: boolean;
  selectedOption: string | null;
  result: string | null;
};

type FinalReport = {
  metadata: {
    scenarioId: string;
    scenarioName: string;
    seed: string;
    applicationVersion: string;
    configurationVersion: string;
    deadlineWeeks: number;
    budget: number;
    finalWeek: number;
    terminationReason: string;
    initialTeam: Record<string, number>;
  };
  outcome: { status: string; label: string; released: boolean };
  scores: {
    total: number;
    performanceBand: string;
    categories: Array<{ name: string; score: number; maximum: number; explanation: string }>;
  };
  metrics: {
    totalSpent: number;
    perceivedProgress: number;
    trueProgress: number;
    releasedDefects: number;
    knownRework: number;
    unknownRework: number;
    testingBacklog: number;
    technicalDebt: number;
    finalTeamSize: number;
    departures: number;
    peakFatigue: number;
    averageFatigue: number;
    averageMorale: number;
    scopeExpansion: number;
    acceptedFeatures: number;
    deferredFeatures: number;
    rejectedFeatures: number;
    hiringCost: number;
  };
  decisionSummary: {
    reviews: Array<{
      decision: {
        week: number;
        decisionType: string;
        description: string;
        relatedId: string;
      };
      immediateEffect: string;
      laterConsequence: string;
      evidence: string[];
    }>;
  };
  eventSummary: {
    total: number;
    resolved: number;
    details: Array<{
      id: string;
      type: string;
      title: string;
      generationWeek: number;
      resolutionWeek: number;
      selectedOption: string;
      result: string;
      internalEffects: Record<string, number>;
      featureWorkByPhase: Record<string, number>;
      customerValuePotential: number;
    }>;
  };
  timeline: Array<{
    week: number;
    type: string;
    title: string;
    description: string;
    relatedId: string;
  }>;
  findings: Array<{
    title: string;
    summary: string;
    evidence: string[];
    startingWeek: number;
    consequenceWeek: number;
    importance: number;
  }>;
  charts: Array<{
    id: string;
    title: string;
    description: string;
    series: Array<{
      name: string;
      unit: string;
      points: Array<{ week: number; value: number }>;
    }>;
  }>;
  instructorWeeks: Array<{
    week: number;
    visibleDuringProject: {
      perceivedProgress: number;
      schedulePressure: number;
      spent: number;
      knownRework: number;
      testingBacklog: number;
      teamSize: number;
      qualityHealth: string;
      fatigueCategory: string;
      phaseProgress: Record<string, number>;
      testingPriority: string;
      workIntensity: string;
      concurrencyPolicy: string;
      engineeringApproach: string;
      technicalDebtPriority: string;
      scopeExpansion: number;
      activity: string[];
    };
    hiddenStateAtTime: {
      trueProgress: number;
      averageFatigue: number;
      maximumFatigue: number;
      averageMorale: number;
      unknownRework: number;
      knownRework: number;
      unknownReworkByPhase: Record<string, number>;
      knownReworkByPhase: Record<string, number>;
      trueProgressByPhase: Record<string, number>;
      perceivedProgressByPhase: Record<string, number>;
      correctWorkByPhase: Record<string, number>;
      defectsCreatedByPhase: Record<string, number>;
      defectsDiscoveredByPhase: Record<string, number>;
      testingBacklog: number;
      qaCapacity: number;
      averageProductivity: number;
      mentoringCoverage: number;
      coordinationOverhead: number;
      turnoverRiskCategory: string;
      technicalDebt: number;
      dependencyUncertainty: number;
      outOfSequenceWork: number;
    };
  }>;
  reflectionPrompts: string[];
};

type HiringOption = {
  weeklySalary: number;
  oneTimeHiringCost: number;
  hiringDelayWeeks: number;
  onboardingWeeks: number;
};

type TeamManagementState = {
  activeEmployees: number;
  onboardingEmployees: number;
  pendingHires: number;
  experienceCounts: Record<string, Record<string, number>>;
  onboarding: Array<{ role: string; experience: string; status: string }>;
  pending: Array<{ role: string; experience: string; weeksUntilStart: number }>;
  hiringOptions: Record<string, Record<string, HiringOption>>;
  weeklyPayroll: number;
  projectedFinalCost: number;
  totalHiringCosts: number;
  mentoringLoad: string;
  coordinationHealth: string;
};

type JavaBridge = {
  getSetupState(): string;
  updateInitialTeam(teamJson: string): string;
  startSimulation(scenarioId: string, seedText: string): string;
  getSimulationState(): string;
  advanceWeek(): string;
  setWorkIntensity(intensity: string): string;
  setTestingPriority(priority: string): string;
  setConcurrencyPolicy(policy: string): string;
  setEngineeringApproach(approach: string): string;
  setTechnicalDebtPriority(priority: string): string;
  resolveEvent(eventId: string, optionId: string): string;
  getFinalReport(): string;
  runAgainWithSameSeed(): string;
  startNewSimulation(): string;
  getTeamManagementState(): string;
  hireEmployee(role: string, experience: string, quantity: number): string;
};

export {};

declare global {
  interface Window {
    javaBridge?: JavaBridge;
  }
}

const roleRows = Array.from(document.querySelectorAll<HTMLElement>('.team-row'));
const setupScreen = document.getElementById('setupScreen')!;
const dashboardScreen = document.getElementById('dashboardScreen')!;
const finalReportScreen = document.getElementById('finalReportScreen')!;
let latestTeamManagement: TeamManagementState | undefined;
let currentFinalReport: FinalReport | undefined;

function formatCurrency(value: number): string {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    maximumFractionDigits: 0
  }).format(value);
}

function displayError(id: string, error: unknown): void {
  const element = document.getElementById(id)!;
  element.textContent = error instanceof Error ? error.message : String(error);
}

function parseResponse<T>(response: string): T {
  return JSON.parse(response) as T;
}

function renderSetup(state: SetupState): void {
  document.getElementById('scenarioSummary')!.textContent = state.scenarioName;
  document.getElementById('setupDeadline')!.textContent = `${state.deadlineWeeks} weeks`;
  document.getElementById('setupBudget')!.textContent = formatCurrency(state.budget);
  document.getElementById('weeklyPayroll')!.textContent = formatCurrency(state.weeklyPayroll);
  document.getElementById('projectedPayroll')!.textContent = formatCurrency(state.projectedPayroll);
  const contingency = document.getElementById('contingency')!;
  contingency.textContent = `Contingency after projected payroll: ${formatCurrency(state.contingency)}`;
  contingency.classList.toggle('cost-warning', state.contingency < 0);

  for (const row of roleRows) {
    const role = row.dataset.role!;
    const count = state.teamCounts[role] ?? 0;
    row.querySelector('.count')!.textContent = String(count);
    const decrement = row.querySelector<HTMLButtonElement>('[data-change="-1"]')!;
    const increment = row.querySelector<HTMLButtonElement>('[data-change="1"]')!;
    decrement.disabled = count <= 0;
    increment.disabled = count >= 12;
  }
}

function renderDashboard(state: SimulationState): void {
  document.getElementById('weekValue')!.textContent = String(state.week);
  document.getElementById('deadlineValue')!.textContent = String(state.deadline);
  document.getElementById('spentValue')!.textContent = formatCurrency(state.spent);
  document.getElementById('remainingValue')!.textContent = formatCurrency(state.remainingBudget);
  document.getElementById('burnRateValue')!.textContent = formatCurrency(state.burnRate);
  document.getElementById('forecastCostValue')!.textContent = formatCurrency(state.forecastCost);
  const progress = Math.max(0, Math.min(1, state.perceivedProgress));
  document.getElementById('progressValue')!.textContent = `${(progress * 100).toFixed(1)}%`;
  document.getElementById('progressBar')!.setAttribute('style', `width: ${progress * 100}%`);
  document.getElementById('forecastValue')!.textContent = state.estimatedCompletionWeek < 0
    ? 'Unavailable'
    : `Week ${state.estimatedCompletionWeek}`;
  document.getElementById('scheduleHealth')!.textContent = state.scheduleHealth;
  document.getElementById('budgetHealth')!.textContent = state.budgetHealth;
  document.getElementById('qualityHealth')!.textContent = state.qualityHealth;
  document.getElementById('moraleHealth')!.textContent = state.moraleHealth;
  document.getElementById('fatigueHealth')!.textContent = state.averageFatigueHealth;
  document.getElementById('turnoverRisk')!.textContent = state.turnoverRisk;
  document.getElementById('departuresValue')!.textContent = String(state.employeesDepartedThisWeek);
  document.getElementById('overtimeCostValue')!.textContent = formatCurrency(state.overtimeCost);
  document.getElementById('scopeValue')!.textContent =
    `${state.scopeExpansion < 0.01 ? 'Original' : `+${(state.scopeExpansion * 100).toFixed(0)}%`}`;
  document.getElementById('acceptedFeaturesValue')!.textContent = String(state.acceptedFeatureCount);
  document.getElementById('deferredFeaturesValue')!.textContent = String(state.deferredFeatureCount);
  document.getElementById('rejectedFeaturesValue')!.textContent = String(state.rejectedFeatureCount);
  document.getElementById('technicalDebtValue')!.textContent = state.technicalDebtHealth;
  const intensityDescriptions: Record<string, string> = {
    SUSTAINABLE: 'Sustainable effort with normal recovery and no overtime premium.',
    INCREASED: 'More short-term capacity; fatigue and payroll cost can increase.',
    CRUNCH: 'Maximum short-term effort; sustained use raises fatigue and quality risk.'
  };
  document.getElementById('workIntensityHelp')!.textContent =
    `${state.workIntensityHours}-hour workweek. ${intensityDescriptions[state.workIntensity] ?? ''}`;
  document.getElementById('knownReworkValue')!.textContent = String(Math.round(state.knownRework));
  document.getElementById('defectsFoundValue')!.textContent = String(Math.round(state.defectsDiscoveredThisWeek));
  document.getElementById('testingBacklogValue')!.textContent = state.testingBacklogStatus;
  document.getElementById('qaCapacityValue')!.textContent = state.qaCapacity.toFixed(2);
  document.getElementById('seedValue')!.textContent = `Seed: ${state.seed}`;
  (document.getElementById('testingPriority') as HTMLSelectElement).value = state.testingPriority;

  const phaseNames: Record<string, string> = {
    REQUIREMENTS: 'Requirements',
    DESIGN: 'Design',
    DEVELOPMENT: 'Development',
    TESTING: 'Testing',
    DEPLOYMENT: 'Deployment'
  };
  const phases = document.getElementById('phaseProgressList')!;
  phases.replaceChildren();
  for (const [phase, progressValue] of Object.entries(state.phaseProgress)) {
    const progress = Math.max(0, Math.min(1, progressValue));
    const row = document.createElement('div');
    row.className = 'phase-progress-row';
    const label = document.createElement('span');
    label.textContent = phaseNames[phase] ?? phase;
    const track = document.createElement('div');
    track.className = 'progress-track';
    const fill = document.createElement('div');
    fill.className = 'progress-fill';
    fill.style.width = `${progress * 100}%`;
    track.appendChild(fill);
    const percentage = document.createElement('strong');
    percentage.textContent = `${(progress * 100).toFixed(0)}%`;
    row.append(label, track, percentage);
    phases.appendChild(row);
  }

  document.getElementById('devCount')!.textContent = String(state.teamCounts.DEVELOPER ?? 0);
  document.getElementById('qaCount')!.textContent = String(state.teamCounts.QA_ENGINEER ?? 0);
  document.getElementById('devopsCount')!.textContent = String(state.teamCounts.DEVOPS_ENGINEER ?? 0);
  document.getElementById('pmCount')!.textContent = String(state.teamCounts.PROJECT_MANAGER ?? 0);

  const messages = document.getElementById('messagesList')!;
  messages.replaceChildren();
  for (const message of state.recentMessages) {
    const item = document.createElement('li');
    item.textContent = message;
    messages.appendChild(item);
  }
  (document.getElementById('advanceButton') as HTMLButtonElement).disabled = state.complete;
  (document.getElementById('hireButton') as HTMLButtonElement).disabled = state.complete;
  (document.getElementById('workIntensity') as HTMLSelectElement).value = state.workIntensity;
  (document.getElementById('concurrencyPolicy') as HTMLSelectElement).value = state.concurrencyPolicy;
  (document.getElementById('engineeringApproach') as HTMLSelectElement).value = state.engineeringApproach;
  (document.getElementById('technicalDebtPriority') as HTMLSelectElement).value = state.technicalDebtPriority;
  renderProjectEvents(state);
  (document.getElementById('advanceButton') as HTMLButtonElement).disabled =
    state.complete || state.pendingEvents.length > 0;
  refreshTeamManagement();
  if (state.complete) {
    dashboardScreen.hidden = true;
    finalReportScreen.hidden = false;
    try {
      renderFinalReport(parseResponse<FinalReport>(window.javaBridge!.getFinalReport()));
    } catch (error) {
      displayError('reportError', error);
    }
  }
}

function renderFinalReport(report: FinalReport): void {
  currentFinalReport = report;
  const metrics = report.metrics;
  document.getElementById('reportOutcome')!.textContent = report.outcome.label;
  document.getElementById('reportScore')!.textContent = `${report.scores.total.toFixed(1)} / 100`;
  document.getElementById('reportWeek')!.textContent =
    `Week ${report.metadata.finalWeek} / ${report.metadata.deadlineWeeks}`;
  document.getElementById('reportSpent')!.textContent =
    `${formatCurrency(metrics.totalSpent)} / ${formatCurrency(report.metadata.budget)}`;
  document.getElementById('reportPerceived')!.textContent =
    `${(metrics.perceivedProgress * 100).toFixed(1)}%`;
  document.getElementById('reportTrue')!.textContent =
    `${(metrics.trueProgress * 100).toFixed(1)}%`;
  document.getElementById('reportDefects')!.textContent =
    String(metrics.releasedDefects);
  document.getElementById('reportTeam')!.textContent =
    `${metrics.finalTeamSize} active; ${metrics.departures} departures`;
  document.getElementById('reportMetadata')!.textContent =
    `${report.metadata.scenarioName} | Seed ${report.metadata.seed} | ` +
    `Application ${report.metadata.applicationVersion} | ` +
    `Configuration ${report.metadata.configurationVersion} | ` +
    `Termination: ${report.metadata.terminationReason.replace(/_/g, ' ').toLowerCase()}`;
  document.getElementById('scoreBand')!.textContent =
    `Overall performance: ${report.scores.performanceBand}`;
  renderScores(report);
  renderDecisionReviews(report);
  renderFindings(report);
  renderCharts(report);
  renderTimeline(report);
  renderReflections(report);
  renderInstructor(report);
  document.getElementById('reportError')!.textContent = '';
}

function renderScores(report: FinalReport): void {
  const container = document.getElementById('scoreCategories')!;
  container.replaceChildren();
  for (const category of report.scores.categories) {
    const row = document.createElement('article');
    row.className = 'score-card';
    const heading = document.createElement('div');
    heading.className = 'score-row';
    const name = document.createElement('strong');
    name.textContent = category.name;
    const track = document.createElement('div');
    track.className = 'score-track';
    const fill = document.createElement('div');
    fill.className = 'score-fill';
    const ratio = category.maximum > 0
      ? Math.max(0, Math.min(1, category.score / category.maximum))
      : 0;
    fill.style.width = `${ratio * 100}%`;
    track.appendChild(fill);
    const score = document.createElement('strong');
    score.textContent = `${category.score.toFixed(1)} / ${category.maximum}`;
    heading.append(name, track, score);
    const explanation = document.createElement('p');
    explanation.className = 'muted';
    explanation.textContent = category.explanation;
    row.append(heading, explanation);
    container.appendChild(row);
  }
}

function renderDecisionReviews(report: FinalReport): void {
  const list = document.getElementById('decisionReviewList')!;
  list.replaceChildren();
  for (const review of report.decisionSummary.reviews) {
    const item = document.createElement('li');
    const heading = document.createElement('strong');
    heading.textContent = `Week ${review.decision.week}: ${review.decision.description}`;
    const immediate = document.createElement('p');
    immediate.textContent = `Immediate effect: ${review.immediateEffect}`;
    const later = document.createElement('p');
    later.textContent = `Later consequence: ${review.laterConsequence}`;
    item.append(heading, immediate, later);
    if (review.evidence.length > 0) {
      const evidence = document.createElement('ul');
      for (const point of review.evidence) {
        const detail = document.createElement('li');
        detail.textContent = point;
        evidence.appendChild(detail);
      }
      item.appendChild(evidence);
    }
    list.appendChild(item);
  }
  if (report.decisionSummary.reviews.length === 0) {
    list.textContent = 'No management decisions changed during this run.';
  }
}

function renderFindings(report: FinalReport): void {
  const container = document.getElementById('causalFindings')!;
  container.replaceChildren();
  for (const finding of report.findings) {
    const card = document.createElement('article');
    card.className = 'metric';
    const heading = document.createElement('h3');
    heading.textContent = finding.title;
    const summary = document.createElement('p');
    summary.textContent = finding.summary;
    const timing = document.createElement('p');
    timing.className = 'muted';
    timing.textContent = finding.startingWeek === finding.consequenceWeek
      ? `Week ${finding.startingWeek} | Importance ${finding.importance} of 5`
      : `Weeks ${finding.startingWeek}-${finding.consequenceWeek} | Importance ${finding.importance} of 5`;
    const evidence = document.createElement('ul');
    evidence.className = 'report-list';
    for (const text of finding.evidence) {
      const item = document.createElement('li');
      item.textContent = text;
      evidence.appendChild(item);
    }
    card.append(heading, summary, timing, evidence);
    container.appendChild(card);
  }
  if (report.findings.length === 0) {
    container.textContent = 'No causal pattern crossed the report thresholds. Review the weekly charts and decision history for discussion.';
  }
}

function renderCharts(report: FinalReport): void {
  const container = document.getElementById('reportCharts')!;
  container.replaceChildren();
  for (const chart of report.charts) {
    const card = document.createElement('article');
    card.className = 'card chart-card';
    const heading = document.createElement('h3');
    heading.textContent = chart.title;
    const description = document.createElement('p');
    description.className = 'muted';
    description.textContent = chart.description;
    card.append(heading, description);
    const chartContents = buildSvgChart(chart);
    card.append(chartContents.svg, chartContents.legend, chartContents.summary);
    container.appendChild(card);
  }
}

function buildSvgChart(chart: FinalReport['charts'][number]): {
  svg: SVGSVGElement;
  legend: HTMLUListElement;
  summary: HTMLParagraphElement;
} {
  const namespace = 'http://www.w3.org/2000/svg';
  const svg = document.createElementNS(namespace, 'svg');
  svg.setAttribute('class', 'chart-svg');
  svg.setAttribute('viewBox', '0 0 720 250');
  svg.setAttribute('role', 'img');
  svg.setAttribute('aria-label', `${chart.title}. ${chart.description}`);
  const title = document.createElementNS(namespace, 'title');
  title.textContent = chart.title;
  svg.appendChild(title);
  const points = chart.series.flatMap((series) => series.points);
  const legend = document.createElement('ul');
  legend.className = 'chart-legend';
  const summary = document.createElement('p');
  summary.className = 'muted';
  if (points.length === 0) {
    summary.textContent = 'No weekly chart data is available.';
    return { svg, legend, summary };
  }

  const left = 58;
  const right = 700;
  const top = 20;
  const bottom = 195;
  const maximum = Math.max(1, ...points.map((point) => point.value));
  const minWeek = Math.min(...points.map((point) => point.week));
  const maxWeek = Math.max(...points.map((point) => point.week));
  const x = (week: number): number => maxWeek === minWeek
    ? (left + right) / 2
    : left + ((week - minWeek) / (maxWeek - minWeek)) * (right - left);
  const y = (value: number): number =>
    bottom - (Math.max(0, value) / maximum) * (bottom - top);
  const tickCount = 4;
  for (let index = 0; index <= tickCount; index++) {
    const value = maximum * index / tickCount;
    const line = document.createElementNS(namespace, 'line');
    const tickY = y(value);
    line.setAttribute('x1', String(left));
    line.setAttribute('x2', String(right));
    line.setAttribute('y1', String(tickY));
    line.setAttribute('y2', String(tickY));
    line.setAttribute('stroke', '#40536d');
    line.setAttribute('stroke-width', '1');
    svg.appendChild(line);
    const label = document.createElementNS(namespace, 'text');
    label.setAttribute('x', String(left - 8));
    label.setAttribute('y', String(tickY + 4));
    label.setAttribute('text-anchor', 'end');
    label.setAttribute('fill', '#bac7d9');
    label.setAttribute('font-size', '12');
    label.textContent = Number(value.toFixed(2)).toString();
    svg.appendChild(label);
  }
  const firstWeekLabel = document.createElementNS(namespace, 'text');
  firstWeekLabel.setAttribute('x', String(left));
  firstWeekLabel.setAttribute('y', '225');
  firstWeekLabel.setAttribute('fill', '#bac7d9');
  firstWeekLabel.setAttribute('font-size', '12');
  firstWeekLabel.textContent = `Week ${minWeek}`;
  svg.appendChild(firstWeekLabel);
  const lastWeekLabel = document.createElementNS(namespace, 'text');
  lastWeekLabel.setAttribute('x', String(right));
  lastWeekLabel.setAttribute('y', '225');
  lastWeekLabel.setAttribute('text-anchor', 'end');
  lastWeekLabel.setAttribute('fill', '#bac7d9');
  lastWeekLabel.setAttribute('font-size', '12');
  lastWeekLabel.textContent = `Week ${maxWeek}`;
  svg.appendChild(lastWeekLabel);

  const palette = ['#61b8ff', '#ffca66', '#87d7ac', '#ed8ba0', '#bd9bff'];
  const finalValues: string[] = [];
  chart.series.forEach((series, index) => {
    const color = palette[index % palette.length];
    const polyline = document.createElementNS(namespace, 'polyline');
    polyline.setAttribute('fill', 'none');
    polyline.setAttribute('stroke', color);
    polyline.setAttribute('stroke-width', '3');
    polyline.setAttribute('stroke-linecap', 'round');
    polyline.setAttribute('stroke-linejoin', 'round');
    polyline.setAttribute('points', series.points.map((point) =>
      `${x(point.week).toFixed(2)},${y(point.value).toFixed(2)}`).join(' '));
    svg.appendChild(polyline);
    for (const point of series.points) {
      const marker = document.createElementNS(namespace, 'circle');
      marker.setAttribute('cx', String(x(point.week)));
      marker.setAttribute('cy', String(y(point.value)));
      marker.setAttribute('r', '3.5');
      marker.setAttribute('fill', color);
      svg.appendChild(marker);
    }
    const item = document.createElement('li');
    const swatch = document.createElement('span');
    swatch.className = 'legend-swatch';
    swatch.style.backgroundColor = color;
    const label = document.createElement('span');
    label.textContent = `${series.name} (${series.unit})`;
    item.append(swatch, label);
    legend.appendChild(item);
    const lastPoint = series.points[series.points.length - 1];
    if (lastPoint) {
      finalValues.push(`${series.name}: ${lastPoint.value.toFixed(1)} ${series.unit} at Week ${lastPoint.week}`);
    }
  });
  summary.textContent = finalValues.join('. ');
  return { svg, legend, summary };
}

function renderTimeline(report: FinalReport): void {
  const list = document.getElementById('reportTimeline')!;
  list.replaceChildren();
  for (const entry of report.timeline) {
    const item = document.createElement('li');
    const title = document.createElement('strong');
    title.textContent = `Week ${entry.week}: ${entry.title}`;
    const description = document.createElement('p');
    description.textContent = `${entry.type.replace(/_/g, ' ')} - ${entry.description}`;
    item.append(title, description);
    list.appendChild(item);
  }
  if (report.timeline.length === 0) {
    list.textContent = 'No significant decisions or events were recorded.';
  }
}

function renderReflections(report: FinalReport): void {
  const list = document.getElementById('reflectionPrompts')!;
  list.replaceChildren();
  for (const prompt of report.reflectionPrompts) {
    const item = document.createElement('li');
    item.textContent = prompt;
    list.appendChild(item);
  }
}

function renderInstructor(report: FinalReport): void {
  const selector = document.getElementById('instructorWeek') as HTMLSelectElement;
  selector.replaceChildren();
  for (const snapshot of report.instructorWeeks) {
    const option = document.createElement('option');
    option.value = String(snapshot.week);
    option.textContent = `Week ${snapshot.week}`;
    selector.appendChild(option);
  }
  selector.onchange = () => renderInstructorWeek(report, Number(selector.value));
  const latestWeek = report.instructorWeeks[report.instructorWeeks.length - 1]?.week ?? 0;
  if (latestWeek > 0) {
    selector.value = String(latestWeek);
    renderInstructorWeek(report, latestWeek);
  } else {
    document.getElementById('instructorDetails')!.textContent = 'No weekly details are available.';
  }

  const events = document.getElementById('instructorEvents')!;
  events.replaceChildren();
  for (const event of report.eventSummary.details) {
    const item = document.createElement('li');
    const option = event.selectedOption
      ? ` Choice: ${event.selectedOption}. ${event.result}`
      : ' No decision was recorded.';
    const effects = Object.entries(event.internalEffects)
      .map(([name, value]) => `${name}: ${value.toFixed(2)}`).join('; ');
    const work = Object.entries(event.featureWorkByPhase)
      .filter(([, value]) => value > 0)
      .map(([phase, value]) => `${phase}: ${value.toFixed(1)}`).join(', ');
    item.textContent = `Week ${event.generationWeek}: ${event.title} (${event.type}).${option}` +
      (event.resolutionWeek > 0 ? ` Resolved Week ${event.resolutionWeek}.` : '') +
      (effects ? ` Recorded effects: ${effects}.` : '') +
      (work ? ` Feature work: ${work}.` : '');
    events.appendChild(item);
  }
  if (report.eventSummary.details.length === 0) {
    events.textContent = 'No project events were recorded.';
  }
}

function renderInstructorWeek(report: FinalReport, week: number): void {
  const snapshot = report.instructorWeeks.find((entry) => entry.week === week);
  const details = document.getElementById('instructorDetails')!;
  details.replaceChildren();
  if (!snapshot) {
    details.textContent = 'No snapshot is available for that week.';
    return;
  }
  const visible = snapshot.visibleDuringProject;
  const hidden = snapshot.hiddenStateAtTime;
  appendDetailPanel(details, 'Visible to Student at the Time', [
    ['Perceived progress', percent(visible.perceivedProgress)],
    ['Schedule pressure', percent(visible.schedulePressure)],
    ['Cumulative spend', formatCurrency(visible.spent)],
    ['Known rework', visible.knownRework.toFixed(1)],
    ['Testing backlog', visible.testingBacklog.toFixed(1)],
    ['Active team', String(visible.teamSize)],
    ['Quality health', visible.qualityHealth],
    ['Fatigue category', visible.fatigueCategory],
    ['Policy settings', `${visible.workIntensity}, ${visible.testingPriority}, ${visible.concurrencyPolicy}, ${visible.engineeringApproach}, debt ${visible.technicalDebtPriority}`],
    ['Phase progress', formatMap(visible.phaseProgress, true)],
    ['Activity', visible.activity.join('; ') || 'No activity message recorded']
  ]);
  appendDetailPanel(details, 'Hidden State at the Time', [
    ['True progress', percent(hidden.trueProgress)],
    ['Average fatigue', percent(hidden.averageFatigue)],
    ['Maximum fatigue', percent(hidden.maximumFatigue)],
    ['Average morale', percent(hidden.averageMorale)],
    ['Unknown rework', hidden.unknownRework.toFixed(1)],
    ['Known rework', hidden.knownRework.toFixed(1)],
    ['Unknown rework by phase', formatMap(hidden.unknownReworkByPhase, false)],
    ['Known rework by phase', formatMap(hidden.knownReworkByPhase, false)],
    ['True progress by phase', formatMap(hidden.trueProgressByPhase, true)],
    ['Perceived progress by phase', formatMap(hidden.perceivedProgressByPhase, true)],
    ['Testing backlog', hidden.testingBacklog.toFixed(1)],
    ['QA capacity', hidden.qaCapacity.toFixed(2)],
    ['Average productivity', hidden.averageProductivity.toFixed(2)],
    ['Mentoring coverage', percent(hidden.mentoringCoverage)],
    ['Coordination overhead', percent(hidden.coordinationOverhead)],
    ['Turnover risk category', hidden.turnoverRiskCategory],
    ['Technical debt', percent(hidden.technicalDebt)],
    ['Dependency uncertainty', percent(hidden.dependencyUncertainty)],
    ['Out-of-sequence work', hidden.outOfSequenceWork.toFixed(1)],
    ['Defects created by phase', formatMap(hidden.defectsCreatedByPhase, false)],
    ['Defects discovered by phase', formatMap(hidden.defectsDiscoveredByPhase, false)]
  ]);
}

function appendDetailPanel(
  container: HTMLElement,
  titleText: string,
  rows: Array<[string, string]>
): void {
  const panel = document.createElement('section');
  panel.className = 'card';
  const heading = document.createElement('h3');
  heading.textContent = titleText;
  const list = document.createElement('dl');
  list.className = 'detail-grid';
  for (const [labelText, valueText] of rows) {
    const label = document.createElement('dt');
    label.textContent = labelText;
    const value = document.createElement('dd');
    value.textContent = valueText;
    list.append(label, value);
  }
  panel.append(heading, list);
  container.appendChild(panel);
}

function formatMap(values: Record<string, number>, asPercent: boolean): string {
  return Object.entries(values).sort(([left], [right]) => left.localeCompare(right))
    .map(([name, value]) => `${name}: ${asPercent ? percent(value) : value.toFixed(1)}`)
    .join('; ');
}

function percent(value: number): string {
  return `${(Math.max(0, Math.min(1, value)) * 100).toFixed(1)}%`;
}

function renderProjectEvents(state: SimulationState): void {
  const pendingEvents = document.getElementById('pendingEvents')!;
  pendingEvents.replaceChildren();
  pendingEvents.hidden = state.pendingEvents.length === 0;
  for (const event of state.pendingEvents) {
    const card = document.createElement('article');
    card.className = 'event-card';
    const heading = document.createElement('h3');
    heading.textContent = event.title;
    const description = document.createElement('p');
    description.textContent = event.description;
    const impact = document.createElement('p');
    impact.className = 'muted';
    impact.textContent = `Likely impact: ${event.impactEstimate}`;
    const choices = document.createElement('div');
    choices.className = 'actions';
    for (const option of event.options) {
      const button = document.createElement('button');
      button.textContent = option.label;
      button.title = option.description;
      button.addEventListener('click', () => resolveProjectEvent(event.id, option.id));
      choices.appendChild(button);
    }
    card.append(heading, description, impact, choices);
    pendingEvents.appendChild(card);
  }

  const history = document.getElementById('eventHistory')!;
  history.replaceChildren();
  for (const event of state.eventHistory.slice(-6).reverse()) {
    const item = document.createElement('li');
    const result = event.resolved ? ` - ${event.result}` : ' - Awaiting decision';
    item.textContent = `Week ${event.week}: ${event.title}${result}`;
    history.appendChild(item);
  }
}

function updateProjectPolicy(
  kind: 'concurrency' | 'engineering' | 'debt',
  value: string
): void {
  const bridge = window.javaBridge;
  if (!bridge) {
    displayError('dashboardError', 'The desktop bridge is not available.');
    return;
  }
  try {
    const update = {
      concurrency: bridge.setConcurrencyPolicy,
      engineering: bridge.setEngineeringApproach,
      debt: bridge.setTechnicalDebtPriority
    }[kind];
    renderDashboard(parseResponse<SimulationState>(update.call(bridge, value)));
    document.getElementById('dashboardError')!.textContent = '';
  } catch (error) {
    displayError('dashboardError', error);
  }
}

function resolveProjectEvent(eventId: string, optionId: string): void {
  const bridge = window.javaBridge;
  if (!bridge) {
    displayError('dashboardError', 'The desktop bridge is not available.');
    return;
  }
  try {
    renderDashboard(parseResponse<SimulationState>(bridge.resolveEvent(eventId, optionId)));
    document.getElementById('dashboardError')!.textContent = '';
  } catch (error) {
    displayError('dashboardError', error);
  }
}

function renderTeamManagement(state: TeamManagementState): void {
  latestTeamManagement = state;
  document.getElementById('activePayroll')!.textContent = formatCurrency(state.weeklyPayroll);
  document.getElementById('teamForecast')!.textContent = formatCurrency(state.projectedFinalCost);
  document.getElementById('totalHiringCost')!.textContent = formatCurrency(state.totalHiringCosts);
  document.getElementById('mentoringLoad')!.textContent = state.mentoringLoad;
  document.getElementById('coordinationHealth')!.textContent = state.coordinationHealth;
  document.getElementById('staffingSummary')!.textContent =
    `${state.activeEmployees} active; ${state.onboardingEmployees} onboarding; ` +
    `${state.pendingHires} pending hire${state.pendingHires === 1 ? '' : 's'}.`;
  const roleLabels: Record<string, string> = {
    DEVELOPER: 'Developers',
    QA_ENGINEER: 'QA Engineers',
    DEVOPS_ENGINEER: 'DevOps Engineers',
    PROJECT_MANAGER: 'Project Managers'
  };
  const experienceLabels: Record<string, string> = {
    JUNIOR: 'Junior',
    MID_LEVEL: 'Mid-level',
    SENIOR: 'Senior'
  };
  const experienceList = document.getElementById('experienceCounts')!;
  experienceList.replaceChildren();
  for (const [role, levels] of Object.entries(state.experienceCounts)) {
    const item = document.createElement('li');
    item.textContent = `${roleLabels[role] ?? role}: ${Object.entries(levels)
      .map(([level, count]) => `${experienceLabels[level] ?? level} ${count}`)
      .join(', ')}`;
    experienceList.appendChild(item);
  }
  const onboardingList = document.getElementById('onboardingList')!;
  onboardingList.replaceChildren();
  for (const employee of state.onboarding) {
    const item = document.createElement('li');
    item.textContent = `${experienceLabels[employee.experience] ?? employee.experience} ${roleLabels[employee.role] ?? employee.role}: ${employee.status.replace(/_/g, ' ').toLowerCase()}`;
    onboardingList.appendChild(item);
  }
  if (state.onboarding.length === 0) {
    onboardingList.textContent = 'No team members are onboarding.';
  }
  const pendingList = document.getElementById('pendingHiresList')!;
  pendingList.replaceChildren();
  for (const hire of state.pending) {
    const item = document.createElement('li');
    const weeks = hire.weeksUntilStart;
    item.textContent = `${experienceLabels[hire.experience] ?? hire.experience} ${roleLabels[hire.role] ?? hire.role}: joins in ${weeks} ${weeks === 1 ? 'week' : 'weeks'}`;
    pendingList.appendChild(item);
  }
  if (state.pending.length === 0) {
    pendingList.textContent = 'No pending hires.';
  }
  updateHirePreview();
}

function refreshTeamManagement(): void {
  const bridge = window.javaBridge;
  if (!bridge) {
    return;
  }
  renderTeamManagement(parseResponse<TeamManagementState>(bridge.getTeamManagementState()));
}

function updateHirePreview(): void {
  if (!latestTeamManagement) {
    return;
  }
  const role = (document.getElementById('hireRole') as HTMLSelectElement).value;
  const experience = (document.getElementById('hireExperience') as HTMLSelectElement).value;
  const quantity = Number((document.getElementById('hireQuantity') as HTMLSelectElement).value);
  const option = latestTeamManagement.hiringOptions[role]?.[experience];
  if (!option) {
    document.getElementById('hirePreview')!.textContent = 'Hiring option is unavailable.';
    return;
  }
  const start = option.hiringDelayWeeks === 0
    ? 'joins immediately'
    : `joins in ${option.hiringDelayWeeks} ${option.hiringDelayWeeks === 1 ? 'week' : 'weeks'}`;
  document.getElementById('hirePreview')!.textContent =
    `Weekly salary: ${formatCurrency(option.weeklySalary * quantity)}; ` +
    `one-time hiring cost: ${formatCurrency(option.oneTimeHiringCost * quantity)}; ` +
    `${start}; onboarding: ${option.onboardingWeeks} weeks.`;
}

function hireEmployees(): void {
  const bridge = window.javaBridge;
  if (!bridge) {
    displayError('dashboardError', 'The desktop bridge is not available.');
    return;
  }
  try {
    const role = (document.getElementById('hireRole') as HTMLSelectElement).value;
    const experience = (document.getElementById('hireExperience') as HTMLSelectElement).value;
    const quantity = Number((document.getElementById('hireQuantity') as HTMLSelectElement).value);
    renderDashboard(parseResponse<SimulationState>(bridge.hireEmployee(role, experience, quantity)));
    document.getElementById('dashboardError')!.textContent = '';
  } catch (error) {
    displayError('dashboardError', error);
  }
}

function loadSetup(): void {
  const bridge = window.javaBridge;
  if (!bridge) {
    displayError('setupError', 'The desktop bridge is not available.');
    return;
  }
  try {
    renderSetup(parseResponse<SetupState>(bridge.getSetupState()));
    document.getElementById('setupError')!.textContent = '';
  } catch (error) {
    displayError('setupError', error);
  }
}

function updateTeam(row: HTMLElement, delta: number): void {
  const bridge = window.javaBridge;
  if (!bridge) {
    displayError('setupError', 'The desktop bridge is not available.');
    return;
  }
  try {
    const setup = parseResponse<SetupState>(bridge.getSetupState());
    const role = row.dataset.role!;
    const count = setup.teamCounts[role] ?? 0;
    const updated = { ...setup.teamCounts, [role]: Math.max(0, Math.min(12, count + delta)) };
    renderSetup(parseResponse<SetupState>(bridge.updateInitialTeam(JSON.stringify(updated))));
    document.getElementById('setupError')!.textContent = '';
  } catch (error) {
    displayError('setupError', error);
  }
}

function startProject(): void {
  const bridge = window.javaBridge;
  if (!bridge) {
    displayError('setupError', 'The desktop bridge is not available.');
    return;
  }
  try {
    const scenarioId = parseResponse<SetupState>(bridge.getSetupState()).scenarioId;
    const seed = (document.getElementById('seedInput') as HTMLInputElement).value.trim();
    const state = parseResponse<SimulationState>(bridge.startSimulation(scenarioId, seed));
    setupScreen.hidden = true;
    dashboardScreen.hidden = false;
    finalReportScreen.hidden = true;
    currentFinalReport = undefined;
    document.getElementById('dashboardError')!.textContent = '';
    renderDashboard(state);
  } catch (error) {
    displayError('setupError', error);
  }
}

function advanceWeek(): void {
  const bridge = window.javaBridge;
  if (!bridge) {
    displayError('dashboardError', 'The desktop bridge is not available.');
    return;
  }
  try {
    renderDashboard(parseResponse<SimulationState>(bridge.advanceWeek()));
    document.getElementById('dashboardError')!.textContent = '';
  } catch (error) {
    displayError('dashboardError', error);
  }
}

function updateWorkIntensity(value: string): void {
  const bridge = window.javaBridge;
  if (!bridge) {
    displayError('dashboardError', 'The desktop bridge is not available.');
    return;
  }
  try {
    renderDashboard(parseResponse<SimulationState>(bridge.setWorkIntensity(value)));
    document.getElementById('dashboardError')!.textContent = '';
  } catch (error) {
    displayError('dashboardError', error);
  }
}

function updateTestingPriority(value: string): void {
  const bridge = window.javaBridge;
  if (!bridge) {
    displayError('dashboardError', 'The desktop bridge is not available.');
    return;
  }
  try {
    renderDashboard(parseResponse<SimulationState>(bridge.setTestingPriority(value)));
    document.getElementById('dashboardError')!.textContent = '';
  } catch (error) {
    displayError('dashboardError', error);
  }
}

function runAgainWithSameSeed(): void {
  const bridge = window.javaBridge;
  if (!bridge) {
    displayError('reportError', 'The desktop bridge is not available.');
    return;
  }
  try {
    const state = parseResponse<SimulationState>(bridge.runAgainWithSameSeed());
    finalReportScreen.hidden = true;
    setupScreen.hidden = true;
    dashboardScreen.hidden = false;
    currentFinalReport = undefined;
    renderDashboard(state);
  } catch (error) {
    displayError('reportError', error);
  }
}

function startNewSimulation(): void {
  const bridge = window.javaBridge;
  if (!bridge) {
    displayError('reportError', 'The desktop bridge is not available.');
    return;
  }
  try {
    const setup = parseResponse<SetupState>(bridge.startNewSimulation());
    finalReportScreen.hidden = true;
    dashboardScreen.hidden = true;
    setupScreen.hidden = false;
    currentFinalReport = undefined;
    (document.getElementById('seedInput') as HTMLInputElement).value = '';
    renderSetup(setup);
  } catch (error) {
    displayError('reportError', error);
  }
}

async function copyRunSeed(): Promise<void> {
  if (!currentFinalReport) {
    return;
  }
  const seed = currentFinalReport.metadata.seed;
  try {
    if (navigator.clipboard?.writeText) {
      await navigator.clipboard.writeText(seed);
    } else {
      const input = document.createElement('textarea');
      input.value = seed;
      input.setAttribute('aria-label', 'Run seed to copy');
      input.style.position = 'fixed';
      input.style.opacity = '0';
      document.body.appendChild(input);
      input.select();
      const copied = document.execCommand('copy');
      input.remove();
      if (!copied) {
        throw new Error('Clipboard access is not available in this desktop window.');
      }
    }
    document.getElementById('reportError')!.textContent = 'Seed copied.';
  } catch (error) {
    displayError('reportError', error);
  }
}

document.getElementById('startButton')!.addEventListener('click', startProject);
document.getElementById('advanceButton')!.addEventListener('click', advanceWeek);
document.getElementById('workIntensity')!.addEventListener('change', (event) => {
  updateWorkIntensity((event.currentTarget as HTMLSelectElement).value);
});
document.getElementById('testingPriority')!.addEventListener('change', (event) => {
  updateTestingPriority((event.currentTarget as HTMLSelectElement).value);
});
document.getElementById('concurrencyPolicy')!.addEventListener('change', (event) => {
  updateProjectPolicy('concurrency', (event.currentTarget as HTMLSelectElement).value);
});
document.getElementById('engineeringApproach')!.addEventListener('change', (event) => {
  updateProjectPolicy('engineering', (event.currentTarget as HTMLSelectElement).value);
});
document.getElementById('technicalDebtPriority')!.addEventListener('change', (event) => {
  updateProjectPolicy('debt', (event.currentTarget as HTMLSelectElement).value);
});
document.getElementById('hireButton')!.addEventListener('click', hireEmployees);
document.getElementById('copySeedButton')!.addEventListener('click', () => {
  void copyRunSeed();
});
document.getElementById('replayButton')!.addEventListener('click', runAgainWithSameSeed);
document.getElementById('newRunButton')!.addEventListener('click', startNewSimulation);
for (const id of ['hireRole', 'hireExperience', 'hireQuantity']) {
  document.getElementById(id)!.addEventListener('change', updateHirePreview);
}

for (const row of roleRows) {
  for (const button of row.querySelectorAll<HTMLButtonElement>('[data-change]')) {
    button.addEventListener('click', () => updateTeam(row, Number(button.dataset.change)));
  }
}

window.addEventListener('javaBridgeReady', loadSetup);
window.addEventListener('DOMContentLoaded', loadSetup);
