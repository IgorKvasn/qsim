/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.utils;

import sk.stuba.fiit.kvasnicka.qsimsimulation.facade.SimulationFacade;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.wizard.SimulationRuleIterator;
import sk.stuba.fiit.kvasnicka.topologyvisual.utils.SimulationData;

/**
 *
 * @author Igor Kvasnicka
 */
public class SimulationRuleHelper {

    /**
     * creates new simulation rule
     *
     * @return simulation rule
     */
    private static SimulationRuleBean createSimulationRule(SimulationData.Data data) {
        SimulationRuleBean rule = new SimulationRuleBean(data.getSourceVertex().getDataModel(), data.getDestinationVertex().getDataModel(), data.getPacketCount(), data.getPacketSize(), data.getActivationDelay(), data.getPacketType(), data.getLayer4protocol(), data.isPing());
        return rule;
    }

    /**
     * creates new simulation rule
     *
     * @param data
     */
    public static SimulationRuleBean newSimulationRule(SimulationFacade simulationFacade, SimulationData.Data data) {
        if (simulationFacade == null) {
            throw new IllegalArgumentException("simulation facade is NULL");
        }

        if (data == null) {
            throw new IllegalArgumentException("simulation rule data is NULL");
        }

        SimulationRuleBean rule = createSimulationRule(data);
        return rule;
    }
}
