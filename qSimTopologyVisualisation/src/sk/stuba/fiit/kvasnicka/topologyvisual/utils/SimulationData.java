/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.utils;

import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.PacketTypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.facade.SimulationFacade;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;
import sk.stuba.fiit.kvasnicka.topologyvisual.exceptions.RoutingException;
import sk.stuba.fiit.kvasnicka.topologyvisual.filetype.TopologyFileTypeDataObject;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.edges.TopologyEdge;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.NetbeansWindowHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.utils.SimulationRuleHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.wizard.SimulationRuleIterator;
import sk.stuba.fiit.kvasnicka.topologyvisual.route.RoutingHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.topology.Topology;

/**
 * here are stored all information regarding simulation rules and routing just
 * before simulation timer starts, all simulation rules needs to be finalised
 * <br> simulation rule finalisation means, that route is calculated and set
 *
 * @author Igor Kvasnicka
 */
public class SimulationData {

    private final TopologyFileTypeDataObject dataObject;
    private final Topology topology;
    private List<Data> dataRules;

    public SimulationData(TopologyFileTypeDataObject dataObject, Topology topology) {
        this.dataObject = dataObject;
        this.topology = topology;
        dataRules = new LinkedList<Data>();
    }

    /**
     * return all simulation rules that are temporary stored here <br>
     * <b>WARNING</b> <br> make sure, finalizeSimulationRules() method has been
     * called - otherwise routing will not be set
     *
     * @see #finalizeSimulationRules()
     */
    public List<SimulationRuleBean> getSimulationRulesFinalised() {
        List<SimulationRuleBean> list = new LinkedList<SimulationRuleBean>();
        SimulationFacade simulationFacade = NetbeansWindowHelper.getInstance().getActiveTopologyVisualisation().getSimulationFacade();

        for (Data data : dataRules) {
            SimulationRuleBean newRule = SimulationRuleHelper.newSimulationRule(simulationFacade, data);
            finalizeSimulationRuleBean(newRule, data);
            list.add(newRule);
        }
        return list;
    }

    /**
     * adds new simulation rule data to the temporary list
     *
     * @param rule
     * @param data
     */
    public void addSimulationData(Data data) {
        dataRules.add(data);
    }

    /**
     * this is called when simulation is about to start and no more changes in
     * routing can occur
     *
     * @return
     */
    private void finalizeSimulationRuleBean(SimulationRuleBean rule, Data data) {
        boolean distanceVector = dataObject.getLoadSettings().isDistanceVectorRouting();
        try {
            //1. calculate route using TopologyFacade or anythong else (see route highlighting)
            List<TopologyEdge> edges = RoutingHelper.retrieveEdges(topology.getG(), data.getSourceVertex(), data.getDestinationVertex(), distanceVector, data.getFixedVertices());
            //2. set the route
            List<NetworkNode> route = RoutingHelper.createRouteFromEdgeList(data.getSourceVertex().getDataModel(), data.getDestinationVertex().getDataModel(), edges);
            rule.setRoute(route);
        } catch (RoutingException ex) {
            throw new IllegalStateException("unable to calculate route - this should not happen, because route was calculated before and no error ocured");
        }
    }

    /**
     * returns simulation data that will be used to create simulation rules
     */
    public List<Data> getSimulationData() {
        return dataRules;
    }

    @Getter
    @Setter
    public static class Data {

        private TopologyVertex sourceVertex, destinationVertex;
        private List<TopologyVertex> fixedVertices;
        private Layer4TypeEnum layer4protocol;
        private int packetSize;
        private int packetCount;
        private PacketTypeEnum packetType;
        private int activationDelay;
        private boolean ping;
    }
}
