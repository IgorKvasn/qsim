/*******************************************************************************
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
 ******************************************************************************/

package sk.stuba.fiit.kvasnicka.qsimsimulation.packet;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.IpPrecedence;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.exceptions.RoutingException;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PacketManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.utils.dscp.DscpValuesEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.utils.dscp.PacketDscpClassificationInterf;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

/**
 * @author Igor Kvasnicka
 */
@Getter
@EqualsAndHashCode
public class Packet implements PacketDscpClassificationInterf {

    private int packetSize;

    /**
     * number of queue where this packet belongs
     * this number is calculated during "marking" phase
     * in general, high number means high priority
     * <p/>
     * the lowest value is 0
     */
    private int qosQueue;


    private PacketManager packetManager;
    /**
     * simulation time when this packets changes its state
     */

    private double simulationTime;
    @Setter
    private double timeWhenCameToQueue;
    private IpPrecedence ipPrecedence;
    private DscpValuesEnum dscpValuesEnum;


    private final double creationTime;
    protected SimulationRuleBean simulationRule;


    /**
     * creates new Packet object
     *
     * @param size          size in Bytes
     * @param packetManager reference to packet manager class
     * @param creationTime  simulation time, when this packet changes its state
     */
    public Packet(int size, PacketManager packetManager, SimulationRuleBean simulationRule, double creationTime) {
        this.packetSize = size;
        this.packetManager = packetManager;
        this.simulationRule = simulationRule;
        this.simulationTime = creationTime;

        this.qosQueue = - 1;
        this.creationTime = creationTime;
    }

    public void setMarking(IpPrecedence ipPrecedence, DscpValuesEnum dscpValuesEnum){
        if (dscpValuesEnum!=null && ipPrecedence!=null){
            return;
        }
        this.dscpValuesEnum = dscpValuesEnum;
        this.ipPrecedence = ipPrecedence;
    }

    public Layer4TypeEnum getLayer4() {
        return simulationRule.getLayer4Type();
    }

    public Layer4TypeEnum getProtocol() {
        return getLayer4();
    }

    public void setSimulationTime(double simulationTime) {
        if (this.simulationTime > simulationTime) {
            throw new IllegalStateException("new packet simulation time is lower then current simulation time: " + this.simulationTime + ">" + simulationTime);
        }
        this.simulationTime = simulationTime;
    }

    public void setQosQueue(int queue, int queueCount) {
        if (queue <= - 1) throw new IllegalArgumentException("queue number cannot be set to -1 or below");
        if (queueCount == 0) {
            this.qosQueue = queue;
        } else {
            this.qosQueue = queue % queueCount;
        }
    }

    public IpPrecedence getIpPrecedenceEnum() {
        return simulationRule.getIpPrecedence();
    }

    public int getIpTos() {
        return simulationRule.getIpPrecedence().getIntRepresentation();
    }

    public NetworkNode getDestination() {
        return simulationRule.getDestination();
    }

    public int getSrcPort() {
        return simulationRule.getSrcPort();
    }

    public int getDestPort() {
        return simulationRule.getDestPort();
    }

    @Override
    public DscpValuesEnum getDscp() {
        return simulationRule.getDscpValue();
    }

    /**
     * this method is used by classification criteria to access getSource() method that is not accessible directly from Packet class,
     * but indirectly using SimulationRule
     *
     * @return
     */
    public NetworkNode getSource() {
        return simulationRule.getSource();
    }

    /**
     * this method is used by classification criteria to access getPacketSize() method that is not accessible directly from Packet class,
     * but indirectly using SimulationRule
     *
     * @return
     */
    public int getSize() {
        return simulationRule.getPacketSize();
    }

    /**
     * answers the question: Is this packet finally delivered to his destination?
     *
     * @param currentNode NetworkNode where is this packet now; may be null (e.g. if packet is placed on the edge)
     * @return is packet in final destination?
     */
    public boolean isPacketDelivered(NetworkNode currentNode) {
        if (currentNode == null) return false;
        if (currentNode.equals(getDestination())) {
            return true;
        }
        return false;
    }


    public NetworkNode getNextHopNetworkNode(NetworkNode currentNode) {
        if (simulationRule == null) {
            throw new IllegalStateException("simulation rule for this packet is NULL - it has not been properly initialised");
        }
        return simulationRule.getNextHopFromRoutingTable(currentNode);
    }

    /**
     * finds previous network node according to routing rules
     *
     * @param currentNode current network node
     * @return previous network node
     * @throws RoutingException if unable to find previous network node - this happens if there is no previous network node
     */
    public NetworkNode getPreviousHopNetworkNode(NetworkNode currentNode) throws RoutingException {
        if (simulationRule == null) {
            throw new IllegalStateException("simulation rule for this packet is NULL - it has not been properly initialised");
        }
        return simulationRule.getPreviousHopFromRoutingTable(currentNode);
    }
}
