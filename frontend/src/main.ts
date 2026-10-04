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
let latestTeamManagement: TeamManagementState | undefined;

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
  document.getElementById('seedValue')!.textContent = `Seed: ${state.seed}`;

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
  refreshTeamManagement();
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

document.getElementById('startButton')!.addEventListener('click', startProject);
document.getElementById('advanceButton')!.addEventListener('click', advanceWeek);
document.getElementById('workIntensity')!.addEventListener('change', (event) => {
  updateWorkIntensity((event.currentTarget as HTMLSelectElement).value);
});
document.getElementById('hireButton')!.addEventListener('click', hireEmployees);
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
