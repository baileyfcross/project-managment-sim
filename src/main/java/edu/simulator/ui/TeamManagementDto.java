package edu.simulator.ui;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record TeamManagementDto(
        int activeEmployees,
        int onboardingEmployees,
        int pendingHires,
        Map<String, Map<String, Integer>> experienceCounts,
        List<OnboardingEmployee> onboarding,
        List<PendingHire> pending,
        Map<String, Map<String, HiringOption>> hiringOptions,
        BigDecimal weeklyPayroll,
        BigDecimal projectedFinalCost,
        BigDecimal totalHiringCosts,
        String mentoringLoad,
        String coordinationHealth
) {
    public record OnboardingEmployee(String role, String experience, String status) {
    }

    public record PendingHire(String role, String experience, int weeksUntilStart) {
    }

    public record HiringOption(BigDecimal weeklySalary, BigDecimal oneTimeHiringCost,
                               int hiringDelayWeeks, int onboardingWeeks) {
    }
}
