import {
  applyInterfaceLayout,
  defaultInterfaceLayout,
  getInterfaceLayout,
  layoutDescriptions,
  setInterfaceLayout,
  type InterfaceLayout
} from './ui/layouts';

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
let currentSimulationState: SimulationState | undefined;
let activeInterfaceLayout = defaultInterfaceLayout;
let activeDashboardPage = 'Dashboard';
let modalReturnFocus: HTMLElement | null = null;
let activeModalId: string | null = null;

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

function openModal(modalId: string): void {
  if (currentSimulationState?.pendingEvents.length && modalId !== 'eventDecisionModal') {
    openModal('eventDecisionModal');
    return;
  }
  const modal = document.getElementById(modalId);
  if (!modal) {
    return;
  }
  if (modalId === 'projectDecisionsModal' && currentSimulationState) {
    syncProjectDecisionControls(currentSimulationState);
    document.getElementById('projectDecisionsError')!.textContent = '';
  }
  if (activeModalId) {
    document.getElementById(activeModalId)!.hidden = true;
  } else {
    modalReturnFocus = document.activeElement instanceof HTMLElement
      ? document.activeElement
      : null;
  }

  activeModalId = modalId;
  modal.hidden = false;
  document.getElementById('modalBackdrop')!.hidden = false;
  const firstControl = modal.querySelector<HTMLElement>(
    'button:not([disabled]), input:not([disabled]), select:not([disabled]), [tabindex="0"]'
  );
  firstControl?.focus();
}

function syncProjectDecisionControls(state: SimulationState): void {
  (document.getElementById('workIntensity') as HTMLSelectElement).value = state.workIntensity;
  (document.getElementById('testingPriority') as HTMLSelectElement).value = state.testingPriority;
  (document.getElementById('concurrencyPolicy') as HTMLSelectElement).value = state.concurrencyPolicy;
  (document.getElementById('engineeringApproach') as HTMLSelectElement).value =
    state.engineeringApproach;
  (document.getElementById('technicalDebtPriority') as HTMLSelectElement).value =
    state.technicalDebtPriority;
}

function closeModal(force = false): void {
  if (!activeModalId) {
    return;
  }
  if (!force && currentSimulationState?.pendingEvents.length) {
    openModal('eventDecisionModal');
    return;
  }
  document.getElementById(activeModalId)!.hidden = true;
  document.getElementById('modalBackdrop')!.hidden = true;
  activeModalId = null;
  const returnFocus = modalReturnFocus;
  modalReturnFocus = null;
  returnFocus?.focus();
}

function initializeModalBehavior(): void {
  document.addEventListener('click', (event) => {
    const target = event.target;
    if (!(target instanceof Element)) return;
    const opener = target.closest<HTMLElement>('[data-open-modal]');
    if (opener) {
      openModal(opener.dataset.openModal!);
      return;
    }
    if (target.closest('[data-close-modal]')) {
      closeModal();
      return;
    }
    if (target.id === 'modalBackdrop' && activeModalId !== 'eventDecisionModal') {
      closeModal();
    }
  });
  document.addEventListener('keydown', (event) => {
    if (!activeModalId) return;
    if (event.key === 'Escape') {
      event.preventDefault();
      closeModal();
      return;
    }
    if (event.key !== 'Tab') return;
    const modal = document.getElementById(activeModalId)!;
    const controls = Array.from(modal.querySelectorAll<HTMLElement>(
      'button:not([disabled]), input:not([disabled]), select:not([disabled]), [tabindex="0"]'
    ));
    if (controls.length === 0) return;
    const first = controls[0];
    const last = controls[controls.length - 1];
    if (event.shiftKey && document.activeElement === first) {
      event.preventDefault();
      last.focus();
    } else if (!event.shiftKey && document.activeElement === last) {
      event.preventDefault();
      first.focus();
    }
  });
}

const dashboardPages = ['Dashboard', 'Team', 'Project', 'Quality', 'Finance', 'Events', 'Analysis'];

function pagesForLayout(layout: InterfaceLayout): Array<{ page: string; label: string }> {
  if (layout === 'COMMAND_CENTER') {
    return [
      { page: 'Dashboard', label: 'Overview' },
      { page: 'Team', label: 'Team' },
      { page: 'Project', label: 'Project' },
      { page: 'Quality', label: 'Quality' },
      { page: 'Finance', label: 'Finances' },
      { page: 'Analysis', label: 'Reports' }
    ];
  }
  if (layout === 'TOP_NAVIGATION_MANAGER') {
    return dashboardPages.filter((page) => page !== 'Events')
      .map((page) => ({ page, label: page === 'Dashboard' ? 'Dashboard' : page }));
  }
  return dashboardPages.map((page) => ({ page, label: page }));
}

function setDashboardPage(page: string): void {
  activeDashboardPage = page;
  for (const section of document.querySelectorAll<HTMLElement>('.dashboard-page[data-page]')) {
    section.hidden = section.dataset.page !== page;
  }
  for (const navigation of [
    document.getElementById('dashboardNavigation'),
    document.getElementById('topDashboardNavigation')
  ]) {
    if (!navigation) continue;
    for (const button of navigation.querySelectorAll<HTMLButtonElement>('[data-page]')) {
      if (button.dataset.page === page) {
        button.setAttribute('aria-current', 'page');
      } else {
        button.removeAttribute('aria-current');
      }
    }
  }
}

function renderLayoutNavigation(state = currentSimulationState): void {
  const pages = pagesForLayout(activeInterfaceLayout);
  if (!pages.some(({ page }) => page === activeDashboardPage)) {
    activeDashboardPage = 'Dashboard';
  }
  const pendingCount = state?.pendingEvents.length ?? 0;
  for (const hostId of ['dashboardNavigation', 'topDashboardNavigation']) {
    const navigation = document.getElementById(hostId)!;
    navigation.replaceChildren();
    for (const { page, label } of pages) {
      const button = document.createElement('button');
      button.type = 'button';
      button.dataset.page = page;
      button.textContent = label;
      if (page === 'Events' && pendingCount > 0) {
        const badge = document.createElement('span');
        badge.className = 'nav-badge';
        badge.textContent = String(pendingCount);
        badge.setAttribute('aria-label',
          `${pendingCount} unresolved ${pendingCount === 1 ? 'event' : 'events'}`);
        button.appendChild(badge);
      }
      button.addEventListener('click', () => setDashboardPage(page));
      navigation.appendChild(button);
    }
  }
  setDashboardPage(activeDashboardPage);
}

function renderLayoutPreference(layout: InterfaceLayout): void {
  activeInterfaceLayout = layout;
  const setupSelect = document.getElementById('interfaceLayout') as HTMLSelectElement;
  setupSelect.value = layout;
  document.getElementById('interfaceLayoutDescription')!.textContent = layoutDescriptions[layout];
  const preview = document.getElementById('layoutPreview')!;
  preview.dataset.layout = layout;
  (document.getElementById('runtimeInterfaceLayout') as HTMLSelectElement).value = layout;
  applyInterfaceLayout(dashboardScreen, layout);
  renderLayoutNavigation();
}

function initializeLayoutPreference(): void {
  activeInterfaceLayout = getInterfaceLayout();
  const runtimeSelect = document.getElementById('runtimeInterfaceLayout') as HTMLSelectElement;
  runtimeSelect.replaceChildren();
  for (const [layout, description] of Object.entries(layoutDescriptions)) {
    const option = document.createElement('option');
    option.value = layout;
    option.textContent = {
      COMMAND_CENTER: 'Command Center',
      TOP_NAVIGATION_MANAGER: 'Top Navigation Manager',
      THREE_COLUMN_DESK: 'Three-Column Manager Desk',
      SIDEBAR_MANAGER: 'Sidebar Manager',
      HYBRID_SIDEBAR_MANAGER: 'Hybrid Sidebar Manager'
    }[layout as InterfaceLayout];
    option.title = description;
    runtimeSelect.appendChild(option);
  }
  renderLayoutPreference(activeInterfaceLayout);
  document.getElementById('interfaceLayout')!.addEventListener('change', (event) => {
    renderLayoutPreference(setInterfaceLayout((event.currentTarget as HTMLSelectElement).value));
  });
  runtimeSelect.addEventListener('change', (event) => {
    const layout = setInterfaceLayout((event.currentTarget as HTMLSelectElement).value);
    closeModal();
    renderLayoutPreference(layout);
    const bridge = window.javaBridge;
    if (bridge) {
      try {
        renderDashboard(parseResponse<SimulationState>(bridge.getSimulationState()));
      } catch (error) {
        displayError('dashboardError', error);
      }
    }
  });
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
  currentSimulationState = state;
  renderLayoutNavigation(state);
  document.getElementById('weekValue')!.textContent = String(state.week);
  document.getElementById('deadlineValue')!.textContent = String(state.deadline);
  document.getElementById('spentValue')!.textContent = formatCurrency(state.spent);
  document.getElementById('remainingValue')!.textContent = formatCurrency(state.remainingBudget);
  document.getElementById('financeRemainingValue')!.textContent = formatCurrency(state.remainingBudget);
  document.getElementById('financeStartingBudget')!.textContent = formatCurrency(state.budget);
  document.getElementById('burnRateValue')!.textContent = formatCurrency(state.burnRate);
  document.getElementById('forecastCostValue')!.textContent = formatCurrency(state.forecastCost);
  const progress = Math.max(0, Math.min(1, state.perceivedProgress));
  document.getElementById('progressValue')!.textContent = `${(progress * 100).toFixed(1)}%`;
  document.getElementById('projectProgressValue')!.textContent = `${(progress * 100).toFixed(1)}%`;
  document.getElementById('analysisProgress')!.textContent = `${(progress * 100).toFixed(1)}%`;
  document.getElementById('progressBar')!.setAttribute('style', `width: ${progress * 100}%`);
  const forecastValue = state.estimatedCompletionWeek < 0
    ? 'Unavailable'
    : `Week ${state.estimatedCompletionWeek}`;
  document.getElementById('forecastValue')!.textContent = forecastValue;
  document.getElementById('forecastValueCard')!.textContent = forecastValue;
  document.getElementById('analysisForecast')!.textContent = forecastValue;
  document.getElementById('scheduleHealth')!.textContent = state.scheduleHealth;
  document.getElementById('budgetHealth')!.textContent = state.budgetHealth;
  document.getElementById('qualityHealth')!.textContent = state.qualityHealth;
  document.getElementById('qualityHealthCard')!.textContent = state.qualityHealth;
  document.getElementById('qualityHealthPage')!.textContent = state.qualityHealth;
  document.getElementById('moraleHealth')!.textContent = state.moraleHealth;
  document.getElementById('teamMoralePage')!.textContent = state.moraleHealth;
  document.getElementById('analysisSchedule')!.textContent = state.scheduleHealth;
  document.getElementById('analysisBudget')!.textContent = state.budgetHealth;
  document.getElementById('analysisQuality')!.textContent = state.qualityHealth;
  document.getElementById('analysisMorale')!.textContent = state.moraleHealth;
  document.getElementById('fatigueHealth')!.textContent = state.averageFatigueHealth;
  document.getElementById('teamFatiguePage')!.textContent = state.averageFatigueHealth;
  document.getElementById('turnoverRisk')!.textContent = state.turnoverRisk;
  document.getElementById('departuresValue')!.textContent = String(state.employeesDepartedThisWeek);
  document.getElementById('overtimeCostValue')!.textContent = formatCurrency(state.overtimeCost);
  document.getElementById('scopeValue')!.textContent =
    `${state.scopeExpansion < 0.01 ? 'Original' : `+${(state.scopeExpansion * 100).toFixed(0)}%`}`;
  document.getElementById('acceptedFeaturesValue')!.textContent = String(state.acceptedFeatureCount);
  document.getElementById('deferredFeaturesValue')!.textContent = String(state.deferredFeatureCount);
  document.getElementById('rejectedFeaturesValue')!.textContent = String(state.rejectedFeatureCount);
  document.getElementById('technicalDebtValue')!.textContent = state.technicalDebtHealth;
  document.getElementById('projectDebtSummary')!.textContent = state.technicalDebtHealth;
  document.getElementById('projectConcurrencySummary')!.textContent = state.concurrencyPolicy.replace(/_/g, ' ');
  document.getElementById('projectEngineeringSummary')!.textContent = state.engineeringApproach.replace(/_/g, ' ');
  document.getElementById('projectDebtPrioritySummary')!.textContent = state.technicalDebtPriority.replace(/_/g, ' ');
  const intensityDescriptions: Record<string, string> = {
    SUSTAINABLE: 'Sustainable effort with normal recovery and no overtime premium.',
    INCREASED: 'More short-term capacity; fatigue and payroll cost can increase.',
    CRUNCH: 'Maximum short-term effort; sustained use raises fatigue and quality risk.'
  };
  document.getElementById('workIntensityHelp')!.textContent =
    `${state.workIntensityHours}-hour workweek. ${intensityDescriptions[state.workIntensity] ?? ''}`;
  document.getElementById('knownReworkValue')!.textContent = String(Math.round(state.knownRework));
  document.getElementById('currentWeekRework')!.textContent = String(Math.round(state.knownRework));
  document.getElementById('defectsFoundValue')!.textContent = String(Math.round(state.defectsDiscoveredThisWeek));
  document.getElementById('testingBacklogValue')!.textContent = state.testingBacklogStatus;
  document.getElementById('currentWeekTesting')!.textContent = state.testingBacklogStatus;
  document.getElementById('currentWeekCost')!.textContent = formatCurrency(state.burnRate);
  document.getElementById('qaCapacityValue')!.textContent = state.qaCapacity.toFixed(2);
  document.getElementById('qualityTestingPriority')!.textContent = state.testingPriority;
  document.getElementById('analysisProgress')!.textContent = `${(progress * 100).toFixed(1)}%`;
  document.getElementById('seedValue')!.textContent = `Seed: ${state.seed}`;
  (document.getElementById('testingPriority') as HTMLSelectElement).value = state.testingPriority;
  document.getElementById('financePayrollValue')!.textContent = latestTeamManagement
    ? formatCurrency(latestTeamManagement.weeklyPayroll)
    : 'Loading';
  document.getElementById('financeHiringValue')!.textContent = latestTeamManagement
    ? formatCurrency(latestTeamManagement.totalHiringCosts)
    : 'Loading';
  document.getElementById('policySummary')!.replaceChildren();
  document.getElementById('persistentPolicySummary')!.replaceChildren();
  const policies = [
    `Work ${state.workIntensity.replace(/_/g, ' ')}`,
    `Testing ${state.testingPriority}`,
    `Concurrency ${state.concurrencyPolicy}`,
    `Engineering ${state.engineeringApproach.replace(/_/g, ' ')}`,
    `Debt ${state.technicalDebtPriority.replace(/_/g, ' ')}`
  ];
  for (const hostId of ['policySummary', 'persistentPolicySummary']) {
    const host = document.getElementById(hostId)!;
    for (const policy of policies) {
      const chip = document.createElement('span');
      chip.className = 'policy-chip';
      chip.textContent = policy;
      host.appendChild(chip);
    }
  }

  const phaseNames: Record<string, string> = {
    REQUIREMENTS: 'Requirements',
    DESIGN: 'Design',
    DEVELOPMENT: 'Development',
    TESTING: 'Testing',
    DEPLOYMENT: 'Deployment'
  };
  for (const containerId of ['phaseProgressList', 'projectPhaseProgressList']) {
    renderPhaseProgress(containerId, state.phaseProgress, phaseNames);
  }

  document.getElementById('devCount')!.textContent = String(state.teamCounts.DEVELOPER ?? 0);
  document.getElementById('qaCount')!.textContent = String(state.teamCounts.QA_ENGINEER ?? 0);
  document.getElementById('devopsCount')!.textContent = String(state.teamCounts.DEVOPS_ENGINEER ?? 0);
  document.getElementById('pmCount')!.textContent = String(state.teamCounts.PROJECT_MANAGER ?? 0);
  for (const countElement of document.querySelectorAll<HTMLElement>('[data-team-count]')) {
    countElement.textContent = String(state.teamCounts[countElement.dataset.teamCount!] ?? 0);
  }

  const messages = document.getElementById('messagesList')!;
  messages.replaceChildren();
  for (const message of state.recentMessages) {
    const item = document.createElement('li');
    item.textContent = message;
    messages.appendChild(item);
  }
  for (const hostId of ['qualityActivity', 'analysisActivity']) {
    const activity = document.getElementById(hostId)!;
    activity.replaceChildren();
    for (const message of state.recentMessages) {
      const item = document.createElement('li');
      item.textContent = message;
      activity.appendChild(item);
    }
  }
  (document.getElementById('advanceButton') as HTMLButtonElement).disabled =
    state.complete || state.pendingEvents.length > 0;
  syncProjectDecisionControls(state);
  renderProjectEvents(state);
  const blocked = state.pendingEvents.length > 0;
  document.getElementById('advanceBlockedMessage')!.hidden = !blocked;
  (document.getElementById('eventActionButton') as HTMLButtonElement).hidden = !blocked;
  (document.getElementById('openEventButton') as HTMLButtonElement).hidden = !blocked;
  document.getElementById('pendingEventStatus')!.textContent = blocked
    ? `${state.pendingEvents.length} unresolved event${state.pendingEvents.length === 1 ? '' : 's'} must be resolved before advancing.`
    : 'No event is blocking project advancement.';
  document.getElementById('acceptedFeaturesPage')!.textContent = String(state.acceptedFeatureCount);
  document.getElementById('deferredFeaturesPage')!.textContent = String(state.deferredFeatureCount);
  document.getElementById('rejectedFeaturesPage')!.textContent = String(state.rejectedFeatureCount);
  if (blocked) {
    openModal('eventDecisionModal');
  } else if (activeModalId === 'eventDecisionModal') {
    closeModal(true);
  }
  refreshTeamManagement();
  if (state.complete) {
    closeModal(true);
    dashboardScreen.hidden = true;
    finalReportScreen.hidden = false;
    try {
      renderFinalReport(parseResponse<FinalReport>(window.javaBridge!.getFinalReport()));
    } catch (error) {
      displayError('reportError', error);
    }
  }
}

function renderPhaseProgress(
  containerId: string,
  phaseProgress: Record<string, number>,
  phaseNames: Record<string, string>
): void {
  const container = document.getElementById(containerId)!;
  container.replaceChildren();
  for (const [phase, progressValue] of Object.entries(phaseProgress)) {
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
    container.appendChild(row);
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
  document.getElementById('financePayrollValue')!.textContent = formatCurrency(state.weeklyPayroll);
  document.getElementById('financeHiringValue')!.textContent = formatCurrency(state.totalHiringCosts);
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
  const modalSummary = document.getElementById('manageTeamSummary')!;
  modalSummary.replaceChildren();
  const modalMetrics = [
    ['Active staff', String(state.activeEmployees)],
    ['Onboarding', String(state.onboardingEmployees)],
    ['Pending hires', String(state.pendingHires)],
    ['Weekly payroll', formatCurrency(state.weeklyPayroll)]
  ];
  for (const [labelText, valueText] of modalMetrics) {
    const metric = document.createElement('div');
    metric.className = 'metric';
    const label = document.createElement('div');
    label.className = 'metric-label';
    label.textContent = labelText;
    const value = document.createElement('div');
    value.className = 'metric-value';
    value.textContent = valueText;
    metric.append(label, value);
    modalSummary.appendChild(metric);
  }
  document.getElementById('manageTeamRoster')!.textContent =
    `Active by role: ${Object.entries(state.experienceCounts).map(([role, levels]) =>
      `${roleLabels[role] ?? role} ${Object.values(levels).reduce((total, count) => total + count, 0)}`
    ).join(', ')}. Mentoring load: ${state.mentoringLoad}. Coordination: ${state.coordinationHealth}. ` +
    `Onboarding: ${state.onboarding.map((employee) =>
      `${experienceLabels[employee.experience] ?? employee.experience} ${roleLabels[employee.role] ?? employee.role}`
    ).join(', ') || 'none'}. ` +
    `Pending hires: ${state.pending.map((hire) => {
      const weeks = hire.weeksUntilStart;
      return `${experienceLabels[hire.experience] ?? hire.experience} ${roleLabels[hire.role] ?? hire.role}, joins in ${weeks} ${weeks === 1 ? 'week' : 'weeks'}`;
    }).join('; ') || 'none'}.`;
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
    displayError('hireError', 'The desktop bridge is not available.');
    return;
  }
  try {
    const role = (document.getElementById('hireRole') as HTMLSelectElement).value;
    const experience = (document.getElementById('hireExperience') as HTMLSelectElement).value;
    const quantity = Number((document.getElementById('hireQuantity') as HTMLSelectElement).value);
    renderDashboard(parseResponse<SimulationState>(bridge.hireEmployee(role, experience, quantity)));
    document.getElementById('dashboardError')!.textContent = '';
    document.getElementById('hireError')!.textContent = '';
    closeModal();
  } catch (error) {
    displayError('hireError', error);
  }
}

function applyProjectDecisions(): void {
  const bridge = window.javaBridge;
  if (!bridge) {
    displayError('projectDecisionsError', 'The desktop bridge is not available.');
    return;
  }
  try {
    let response = bridge.setWorkIntensity(
      (document.getElementById('workIntensity') as HTMLSelectElement).value);
    response = bridge.setTestingPriority(
      (document.getElementById('testingPriority') as HTMLSelectElement).value);
    response = bridge.setConcurrencyPolicy(
      (document.getElementById('concurrencyPolicy') as HTMLSelectElement).value);
    response = bridge.setEngineeringApproach(
      (document.getElementById('engineeringApproach') as HTMLSelectElement).value);
    response = bridge.setTechnicalDebtPriority(
      (document.getElementById('technicalDebtPriority') as HTMLSelectElement).value);
    renderDashboard(parseResponse<SimulationState>(response));
    document.getElementById('projectDecisionsError')!.textContent = '';
    closeModal();
  } catch (error) {
    displayError('projectDecisionsError', error);
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
    activeDashboardPage = 'Dashboard';
    currentSimulationState = undefined;
    setupScreen.hidden = true;
    dashboardScreen.hidden = false;
    finalReportScreen.hidden = true;
    document.querySelector<HTMLElement>('.app')!.classList.add('simulation-active');
    document.getElementById('appMasthead')!.hidden = true;
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

function runAgainWithSameSeed(): void {
  const bridge = window.javaBridge;
  if (!bridge) {
    displayError('reportError', 'The desktop bridge is not available.');
    return;
  }
  try {
    const state = parseResponse<SimulationState>(bridge.runAgainWithSameSeed());
    activeDashboardPage = 'Dashboard';
    finalReportScreen.hidden = true;
    setupScreen.hidden = true;
    dashboardScreen.hidden = false;
    document.querySelector<HTMLElement>('.app')!.classList.add('simulation-active');
    document.getElementById('appMasthead')!.hidden = true;
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
    document.querySelector<HTMLElement>('.app')!.classList.remove('simulation-active');
    document.getElementById('appMasthead')!.hidden = false;
    currentFinalReport = undefined;
    currentSimulationState = undefined;
    activeDashboardPage = 'Dashboard';
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

initializeLayoutPreference();
initializeModalBehavior();
document.getElementById('startButton')!.addEventListener('click', startProject);
document.getElementById('advanceButton')!.addEventListener('click', advanceWeek);
document.getElementById('applyProjectDecisionsButton')!.addEventListener('click', applyProjectDecisions);
document.getElementById('eventActionButton')!.addEventListener('click', () => openModal('eventDecisionModal'));
document.getElementById('openEventButton')!.addEventListener('click', () => openModal('eventDecisionModal'));
document.getElementById('hireButton')!.addEventListener('click', hireEmployees);
document.getElementById('copySeedButton')!.addEventListener('click', () => {
  void copyRunSeed();
});
document.getElementById('replayButton')!.addEventListener('click', runAgainWithSameSeed);
document.getElementById('newRunButton')!.addEventListener('click', startNewSimulation);
for (const id of ['hireRole', 'hireExperience', 'hireQuantity']) {
  document.getElementById(id)!.addEventListener('change', updateHirePreview);
}
for (const id of ['workIntensity', 'testingPriority', 'concurrencyPolicy',
  'engineeringApproach', 'technicalDebtPriority']) {
  document.getElementById(id)!.addEventListener('change', () => {
    document.getElementById('projectDecisionsError')!.textContent = '';
  });
}

for (const row of roleRows) {
  for (const button of row.querySelectorAll<HTMLButtonElement>('[data-change]')) {
    button.addEventListener('click', () => updateTeam(row, Number(button.dataset.change)));
  }
}

window.addEventListener('javaBridgeReady', loadSetup);
window.addEventListener('DOMContentLoaded', loadSetup);
