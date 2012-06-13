/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.utils;

import java.util.List;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.SimulationRuleBean;
import sk.stuba.fiit.kvasnicka.qsimsimulation.facade.SimulationFacade;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.wizard.SimulationRuleIterator;
import sk.stuba.fiit.kvasnicka.topologyvisual.route.RoutingHelper;

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
    private static SimulationRuleBean createSimulationRule(SimulationRuleIterator.Data data) {
        SimulationRuleBean rule = new SimulationRuleBean(data.getSourceVertex().getDataModel(), data.getDestinationVertex().getDataModel(), data.getPacketCount(), data.getPacketSize(), data.getActivationDelay(), data.getPacketType(), data.getLayer4protocol(), data.isPing());
        List<NetworkNode> route = RoutingHelper.createRouteFromEdgeList(data.getSourceVertex().getDataModel(), data.getDestinationVertex().getDataModel(), data.getRoute());

        rule.addRoute(route);
        return rule;
    }

    /**
     * creates new simulation rule and adds it to simulation rule manager
     *
     * @param data
     */
    public static SimulationRuleBean newSimulationRule(SimulationFacade simulationFacade, SimulationRuleIterator.Data data) {
        SimulationRuleBean rule = createSimulationRule(data);
        if (!rule.isPing()) {
            simulationFacade.addSimulationRule(rule);
        } else {
            simulationFacade.addPingRule(rule, data.getPingRepeats());
        }
        return rule;
    }
}
