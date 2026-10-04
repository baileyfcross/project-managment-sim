type State = {
  week: number;
  deadline: number;
  budget: number;
  spent: number;
  remainingBudget: number;
  burnRate: number;
  forecastCost: number;
  estimatedCompletionWeek: number;
  perceivedProgress: number;
  trueProgress: number;
  teamCounts: Record<string, number>;
  scheduleHealth: string;
  budgetHealth: string;
  qualityHealth: string;
  moraleHealth: string;
  recentMessages: string[];
  scenarioName: string;
  pendingEvent: string;
};

const emptyState: State = {
  week: 0,
  deadline: 20,
  budget: 400000,
  spent: 0,
  remainingBudget: 400000,
  burnRate: 0,
  forecastCost: 0,
  estimatedCompletionWeek: 0,
  perceivedProgress: 0,
  trueProgress: 0,
  teamCounts: { PROJECT_MANAGER: 0, DEVELOPER: 0, QA_ENGINEER: 0, DEVOPS_ENGINEER: 0 },
  scheduleHealth: 'Healthy',
  budgetHealth: 'Healthy',
  qualityHealth: 'Healthy',
  moraleHealth: 'Good',
  recentMessages: ['Project not started.'],
  scenarioName: 'Small Business Web Application',
  pendingEvent: 'No event'
};

function hydrateState(raw: Partial<State> | null | undefined): State {
  return { ...emptyState, ...(raw ?? {}) };
}

function formatCurrency(value: number): string {
  return `$${Math.round(value).toLocaleString()}`;
}

function percent(value: number): string {
  return `${(value * 100).toFixed(1)}%`;
}

function updateView(state: State) {
  const scenarioSummary = document.getElementById('scenarioSummary');
  scenarioSummary!.textContent = `${state.scenarioName} - Deadline ${state.deadline} weeks`;

  document.getElementById('weekValue')!.textContent = `Week ${state.week}`;
  document.getElementById('deadlineValue')!.textContent = `${state.deadline}`;
  document.getElementById('budgetValue')!.textContent = formatCurrency(state.remainingBudget);
  document.getElementById('burnRateValue')!.textContent = formatCurrency(state.burnRate);
  document.getElementById('forecastValue')!.textContent = `Week ${state.estimatedCompletionWeek}`;
  document.getElementById('perceivedValue')!.textContent = percent(state.perceivedProgress);

  document.getElementById('scheduleHealth')!.textContent = state.scheduleHealth;
  document.getElementById('budgetHealth')!.textContent = state.budgetHealth;
  document.getElementById('qualityHealth')!.textContent = state.qualityHealth;
  document.getElementById('moraleHealth')!.textContent = state.moraleHealth;

  document.getElementById('devCount')!.textContent = String(state.teamCounts.DEVELOPER ?? 0);
  document.getElementById('qaCount')!.textContent = String(state.teamCounts.QA_ENGINEER ?? 0);
  document.getElementById('devopsCount')!.textContent = String(state.teamCounts.DEVOPS_ENGINEER ?? 0);
  document.getElementById('pmCount')!.textContent = String(state.teamCounts.PROJECT_MANAGER ?? 0);

  const messagesList = document.getElementById('messagesList')!;
  messagesList.innerHTML = '';
  for (const message of state.recentMessages) {
    const li = document.createElement('li');
    li.textContent = message;
    messagesList.appendChild(li);
  }
}

async function requestState(): Promise<State> {
  const raw = window.javaBridge && typeof window.javaBridge.getSimulationState === 'function'
    ? window.javaBridge.getSimulationState()
    : null;
  if (!raw) {
    return emptyState;
  }
  try {
    return hydrateState(JSON.parse(raw));
  } catch {
    return emptyState;
  }
}

async function startScenario() {
  if (window.javaBridge && typeof window.javaBridge.startScenario === 'function') {
    const response = window.javaBridge.startScenario('small-web-app', 42);
    const state = hydrateState(JSON.parse(response));
    updateView(state);
  }
}

async function advanceWeek() {
  if (window.javaBridge && typeof window.javaBridge.advanceWeek === 'function') {
    const response = window.javaBridge.advanceWeek();
    const state = hydrateState(JSON.parse(response));
    updateView(state);
  }
}

function attachEvents() {
  document.getElementById('startButton')!.addEventListener('click', startScenario);
  document.getElementById('advanceButton')!.addEventListener('click', advanceWeek);
  const intensitySelect = document.getElementById('workIntensity') as HTMLSelectElement;
  intensitySelect.addEventListener('change', () => {
    if (window.javaBridge && typeof window.javaBridge.setWorkIntensity === 'function') {
      window.javaBridge.setWorkIntensity(intensitySelect.value);
    }
  });
}

declare global {
  interface Window {
    javaBridge?: {
      getSimulationState?: () => string;
      startScenario?: (scenarioId: string, seed: number) => string;
      advanceWeek?: () => string;
      setWorkIntensity?: (intensity: string) => string;
    };
  }
}

window.addEventListener('DOMContentLoaded', () => {
  attachEvents();
  updateView(emptyState);
  void requestState().then(updateView);
});
