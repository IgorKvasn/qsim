/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.utils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import lombok.EqualsAndHashCode;
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
     * returns all simulation datas that contains certain TopologyVertex in
     * route
     *
     * @param vertex
     * @return
     */
    public List<SimulationData.Data> getSimulationDataContainingVertex(TopologyVertex vertex) {
        List<SimulationData.Data> list = new LinkedList<Data>();
        for (Data data : dataRules) {

            if (data.getSourceVertex().getName().equals(vertex.getName())) {
                list.add(data);
                continue;
            }
            if (data.getDestinationVertex().getName().equals(vertex.getName())) {
                list.add(data);
                continue;
            }

            for (TopologyVertex v : data.getFixedVertices()) {//checkcing all vertices in this simulation rule
                if (v.getName().equals(vertex.getName())) {
                    list.add(data);
                    break;
                }
            }

        }
        return list;
    }

    /**
     * return all simulation rules that are temporary stored here <br>
     * <b>WARNING</b> <br> make sure, finalizeSimulationRules() method has been
     * called - otherwise routing will not be set
     *
     * @see #finalizeSimulationRules()
     */
    public List<SimulationRuleBean> getSimulationRulesFinalised() throws RoutingException {
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
    private void finalizeSimulationRuleBean(SimulationRuleBean rule, Data data) throws RoutingException {
        boolean distanceVector = dataObject.getLoadSettings().isDistanceVectorRouting();

        //1. calculate route using TopologyFacade or anythong else (see route highlighting)
        List<TopologyEdge> edges = RoutingHelper.retrieveEdges(topology.getG(), data.getSourceVertex(), data.getDestinationVertex(), distanceVector, data.getFixedVertices());
        //2. set the route
        List<NetworkNode> route = RoutingHelper.createRouteFromEdgeList(data.getSourceVertex().getDataModel(), data.getDestinationVertex().getDataModel(), edges);
        rule.setRoute(route);

    }

    /**
     * returns simulation data that will be used to create simulation rules
     */
    public List<Data> getSimulationData() {
        return dataRules;
    }

    /**
     * removes simulation rule data depending on its ID
     *
     * @param dataID
     */
    public void removeSimulationData(String dataID) {
        for (Iterator<Data> it = dataRules.iterator(); it.hasNext();) {
            Data data = it.next();
            if (data.getId().equals(dataID)) {
                it.remove();
                return;
            }
        }

    }

    /**
     * finds simulation rule data depending on its ID
     *
     * @param dataID
     * @return
     */
    public Data findSimulationData(String dataID) {
        for (Data d : dataRules) {
            if (d.getId().equals(dataID)) {
                return d;
            }
        }
        throw new IllegalStateException("no Data object found for ID: " + dataID);
    }

    @Getter
    @Setter
    @EqualsAndHashCode(of = "id")
    public static class Data {

        private final String id = UUID.randomUUID().toString();
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
