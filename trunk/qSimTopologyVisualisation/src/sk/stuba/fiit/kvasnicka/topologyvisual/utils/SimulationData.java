/*
 * This file is part of qSim.
 *
 * qSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * qSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with qSim.  If not, see <http://www.gnu.org/licenses/>.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.utils;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import javax.swing.event.EventListenerList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.openide.util.Exceptions;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.IpPrecedence;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.facade.SimulationFacade;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;
import sk.stuba.fiit.kvasnicka.topologyvisual.events.simulationrule.SimulationRuleChangedEvent;
import sk.stuba.fiit.kvasnicka.topologyvisual.events.simulationrule.SimulationRuleChangedListener;
import sk.stuba.fiit.kvasnicka.topologyvisual.events.topologystate.TopologyStateChangedListener;
import sk.stuba.fiit.kvasnicka.topologyvisual.exceptions.RoutingException;
import sk.stuba.fiit.kvasnicka.topologyvisual.filetype.TopologyFileTypeDataObject;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.edges.TopologyEdge;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.NetbeansWindowHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.utils.SimulationRuleHelper;
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

    private static Logger logg = Logger.getLogger(SimulationData.class);
    private final TopologyFileTypeDataObject dataObject;
    private final Topology topology;
    private List<Data> dataRules;
    private EventListenerList listenerList = new EventListenerList();

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

            for (TopologyVertex v : data.getFixedVertices()) {//checking all vertices in this simulation rule
                if (v.getName().equals(vertex.getName())) {
                    list.add(data);
                    break;
                }
            }

        }
        return list;
    }

    public List<Data> getSimulationDataContainingEdge(TopologyEdge e) {
        List<SimulationData.Data> list = new LinkedList<Data>();

        boolean distanceVector = dataObject.getLoadSettings().isDistanceVectorRouting();
        //find edge within these route edges
        for (Data data : dataRules) {
            try {
                //calculate route using TopologyFacade or anythong else (see route highlighting)
                List<TopologyEdge> edges = RoutingHelper.retrieveEdges(topology.getG(), data.getSourceVertex(), data.getDestinationVertex(), distanceVector, data.getFixedVertices());
                for (TopologyEdge edge : edges) {
                    if (EdgeUtils.isEdgesEqual(edge, e)) {
                        list.add(data);
                        break;
                    }
                }
            } catch (RoutingException ex) {
                //unable to find route
                //this should not happen
                logg.fatal("unable to find route", ex);
            }

        }

        return list;
    }

    /**
     * tries to calculate new simulation rules without specified edge <br/>
     * returns list of simulation rules that are unable to work without this
     * edge
     *
     * @param e
     * @return
     */
    public List<Data> calculateNewRoutesWithoutEdge(TopologyEdge e) {
        List<SimulationData.Data> list = new LinkedList<Data>();
        topology.getG().removeEdge(e);     //temporary remove the edge, so that new route can be calculated

        boolean distanceVector = dataObject.getLoadSettings().isDistanceVectorRouting();
        //find edge within these route edges
        for (Data data : dataRules) {
            try {
                //calculate route using TopologyFacade or anythong else (see route highlighting)
                RoutingHelper.retrieveEdges(topology.getG(), data.getSourceVertex(), data.getDestinationVertex(), distanceVector, data.getFixedVertices());
            } catch (RoutingException ex) {
                //unable to find new route
                Exceptions.printStackTrace(ex);
                list.add(data);
            }

        }
        topology.getG().addEdge(e, e.getVertex1(), e.getVertex2()); //add edge back to the topology

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
     * adds new (or modify existing) simulation rule data to the temporary list
     *
     * @param rule
     * @param data
     */
    public void addSimulationData(Data data) {
        if (data.getId() == null) {//new data
            data.setId();
            dataRules.add(data);
        } else {//data has been modified
            //find where is old data positioned within the list
            int simDataNumber;

            for (simDataNumber = 0; simDataNumber < dataRules.size(); simDataNumber++) {
                if (dataRules.get(simDataNumber).getId().equals(data.getId())) {
                    break;
                }
            }

            dataRules.remove(simDataNumber);
            dataRules.add(simDataNumber, data);
        }
        fireSimulationRuleChangedEvent(new SimulationRuleChangedEvent(this));
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
     * returns simulation data that will be used to create simulation rules
     */
    public void setSimulationData(List<Data> dataRules) {
        this.dataRules = dataRules;
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
        fireSimulationRuleChangedEvent(new SimulationRuleChangedEvent(this));
    }

    public void addSimulationRuleChangedListener(SimulationRuleChangedListener listener) {
        listenerList.add(SimulationRuleChangedListener.class, listener);
    }

    public void removeSimulationRuleChangedListener(SimulationRuleChangedListener listener) {
        listenerList.remove(SimulationRuleChangedListener.class, listener);
    }

    private void fireSimulationRuleChangedEvent(SimulationRuleChangedEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i].equals(TopologyStateChangedListener.class)) {
                ((SimulationRuleChangedListener) listeners[i + 1]).simulationRuleChangedOccured(evt);
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
    public static class Data implements Serializable {

        private static final long serialVersionUID = -20325075325465050L;
        private String id = null;
        private IpPrecedence ipPrecedence;
        private int srcPort, destPort;
        private String name;
        private TopologyVertex sourceVertex, destinationVertex;
        private List<TopologyVertex> fixedVertices;
        private Layer4TypeEnum layer4protocol;
        private int packetSize;
        private int packetCount;
        private int activationDelay;

        private void setId() {
            if (id != null) {
                throw new IllegalStateException("simulation data ID is already set");
            }
            id = UUID.randomUUID().toString();
        }

        public boolean isPing() {
            return Layer4TypeEnum.ICMP == layer4protocol;
        }
    }
}
