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
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.PacketTypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PacketManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

/**
 * @author Igor Kvasnicka
 */
@Getter
@EqualsAndHashCode
public class Packet {

    private int packetSize;

    /**
     * number of queue where this packet belongs
     * this number is calculated during "marking" phase
     * in general, high number means high priority
     * <p/>
     * the lowest value is 0
     */
    @Setter
    private int qosQueue;
    @Getter
    private Layer4TypeEnum layer4;

    private PacketManager packetManager;
    /**
     * simulation time when this packets changes its state
     */

    private double simulationTime;
    @Setter
    private double timeWhenCameToQueue;


    private final double creationTime;
    protected SimulationRuleBean simulationRule;


    /**
     * creates new Packet object
     *
     * @param size          size in Bytes
     * @param layer4        TCP/IP layer 4 protocol
     * @param packetManager reference to packet manager class
     * @param creationTime  simulation time, when this packet changes its state
     */
    public Packet(int size, Layer4TypeEnum layer4, PacketManager packetManager, SimulationRuleBean simulationRule, double creationTime) {
        this.packetSize = size;
        this.layer4 = layer4;
        this.packetManager = packetManager;
        this.simulationRule = simulationRule;
        this.simulationTime = creationTime;

        this.qosQueue = - 1;
        this.creationTime = creationTime;
    }

    public void setSimulationTime(double simulationTime) {
        if (this.simulationTime > simulationTime) {
            throw new IllegalStateException("new packet simulation time is lower then current simulation time: " + this.simulationTime + ">" + simulationTime);
        }
        this.simulationTime = simulationTime;
    }

    /**
     * returns a type of packet - audio, video, data, ...
     * <p/>
     * this method is used by classification criteria to access getPacketType() method that is not accessible directly from Packet class,
     * but indirectly using SimulationRule
     *
     * @return type of packet
     */

    public PacketTypeEnum getPacketType() {
        return simulationRule.getPacketTypeEnum();
    }

    public NetworkNode getDestination() {
        return simulationRule.getDestination();
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

    public NetworkNode getPreviousHopNetworkNode(NetworkNode currentNode) {
        if (simulationRule == null) {
            throw new IllegalStateException("simulation rule for this packet is NULL - it has not been properly initialised");
        }
        return simulationRule.getPreviousHopFromRoutingTable(currentNode);
    }
}
