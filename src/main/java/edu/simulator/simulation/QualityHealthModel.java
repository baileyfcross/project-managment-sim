package edu.simulator.simulation;

import edu.simulator.configuration.SimulationConfiguration;

public class QualityHealthModel {
    public String assess(double knownRework, double testingBacklog,
                         double defectsDiscoveredThisWeek,
                         SimulationConfiguration configuration) {
        var thresholds = configuration.getTesting();
        if (knownRework >= thresholds.getAtRiskKnownRework()
                || testingBacklog >= thresholds.getCriticalBacklogThreshold()) {
            return "CRITICAL";
        }
        if (knownRework >= thresholds.getConcerningKnownRework()
                || testingBacklog >= thresholds.getHighBacklogThreshold()) {
            return "AT_RISK";
        }
        if (knownRework > 0.0 || testingBacklog >= thresholds.getModerateBacklogThreshold()
                || defectsDiscoveredThisWeek > 0.0) {
            return "CONCERNING";
        }
        return "HEALTHY";
    }
}
