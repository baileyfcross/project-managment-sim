package edu.simulator.simulation;

import edu.simulator.configuration.SimulationConfiguration;
import edu.simulator.model.Employee;
import edu.simulator.model.Team;

import java.util.Random;

public class TurnoverModel {
    public int calculateTurnover(Team team, double fatigue, double morale, double schedulePressure,
                                SimulationConfiguration config, Random random) {
        int count = 0;
        for (Employee employee : team.allEmployees()) {
            double probability = config.getTurnover().getBaseRate();
            probability += fatigue * config.getTurnover().getFatigueWeight();
            probability += (1.0 - morale) * config.getTurnover().getMoraleWeight();
            probability += schedulePressure * config.getTurnover().getPressureWeight();
            probability = Math.min(config.getTurnover().getMaxRate(), Math.max(0.0, probability));
            if (random.nextDouble() < probability) {
                employee.setActive(false);
                count++;
            }
        }
        return count;
    }
}
