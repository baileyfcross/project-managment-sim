export const layoutDescriptions = {
  COMMAND_CENTER:
    'Dashboard-focused layout with major project information and management actions visible together.',
  TOP_NAVIGATION_MANAGER:
    'Category-based management interface with sections across the top and focused content in the center.',
  THREE_COLUMN_DESK:
    'Compact management view with project health, progress, and management controls visible side by side.',
  SIDEBAR_MANAGER:
    'Persistent left-side navigation with focused management pages and a compact project header.',
  HYBRID_SIDEBAR_MANAGER:
    'Persistent sidebar navigation, project status header, focused category pages, and modal-based management actions.'
} as const;

export type InterfaceLayout = keyof typeof layoutDescriptions;

export const defaultInterfaceLayout: InterfaceLayout = 'HYBRID_SIDEBAR_MANAGER';
const storageKey = 'spms.interfaceLayout';

const layoutClasses: Record<InterfaceLayout, string> = {
  COMMAND_CENTER: 'layout-command-center',
  TOP_NAVIGATION_MANAGER: 'layout-top-navigation',
  THREE_COLUMN_DESK: 'layout-three-column',
  SIDEBAR_MANAGER: 'layout-sidebar',
  HYBRID_SIDEBAR_MANAGER: 'layout-hybrid-sidebar'
};

let sessionLayout: InterfaceLayout = defaultInterfaceLayout;

export function isInterfaceLayout(value: string | null): value is InterfaceLayout {
  return value !== null && Object.prototype.hasOwnProperty.call(layoutDescriptions, value);
}

export function getInterfaceLayout(): InterfaceLayout {
  try {
    const storedLayout = window.localStorage.getItem(storageKey);
    if (isInterfaceLayout(storedLayout)) {
      sessionLayout = storedLayout;
    }
  } catch {
    // WebView storage can be unavailable for local resources; retain the session preference.
  }
  return sessionLayout;
}

export function setInterfaceLayout(value: string): InterfaceLayout {
  const layout = isInterfaceLayout(value) ? value : defaultInterfaceLayout;
  sessionLayout = layout;
  try {
    window.localStorage.setItem(storageKey, layout);
  } catch {
    // Keep the selected layout for this page session when persistent storage is unavailable.
  }
  return layout;
}

export function applyInterfaceLayout(element: HTMLElement, layout: InterfaceLayout): void {
  element.classList.remove(...Object.values(layoutClasses));
  element.classList.add(layoutClasses[layout]);
  element.dataset.layout = layout;
}
