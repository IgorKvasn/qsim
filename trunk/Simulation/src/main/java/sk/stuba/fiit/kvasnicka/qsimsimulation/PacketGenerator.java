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

package sk.stuba.fiit.kvasnicka.qsimsimulation;

import org.apache.log4j.Logger;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.helpers.DelayHelper;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PacketManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.PingPacket;
import sk.stuba.fiit.kvasnicka.qsimsimulation.ping.PingManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

import java.util.LinkedList;
import java.util.List;

/**
 * this class is used to generate packets according to simulation rules
 *
 * @author Igor Kvasnicka
 */
public class PacketGenerator {
    private static Logger logg = Logger.getLogger(PacketGenerator.class);

    private List<SimulationRuleBean> simulationRules;
    private PacketManager packetManager;
    private PingManager pingManager;


    public PacketGenerator(List<SimulationRuleBean> simulationRules, SimulationTimer simulationTimer) {
        this.simulationRules = simulationRules;
        packetManager = simulationTimer.getPacketManager();
        pingManager = simulationTimer.getPingManager();
    }

    /**
     * creates new packets for all network nodes
     *
     * @param simulationTime current simulation time
     * @param timeQuantum    time quantum - this is used when creating new packets to not create all packets at once, but as many as time quantum allows
     */
    public void generatePackets(double simulationTime, double timeQuantum) {
        for (SimulationRuleBean rule : simulationRules) {
            if (rule.isFinished()) continue; //I don't care about finished simulation rules
            if (rule.isActive()) {//rule has been activated and it is not finished yet
                addPacketsToNetworkNode(timeQuantum, rule, simulationTime);
            } else {//check if the time came to activate this rule
                if (checkRuleActivate(rule, simulationTime)) {//yes, I should activate it
                    rule.setActive(true);
                    addPacketsToNetworkNode(timeQuantum, rule, simulationTime);
                    rule.increaseActivationTime(timeQuantum);
                }
            }
        }
    }


    private void addPacketsToNetworkNode(double timeQuantum, SimulationRuleBean rule, double simulationTime) {
        List<Packet> packets = generatePacketsFromSimulRule(rule, timeQuantum, simulationTime);
        packetManager.initPackets(rule.getSource(), packets);
    }

    /**
     * checks if not active rule should became active (initial delay has expired)
     *
     * @param rule
     * @param simulationTime
     * @return true if rule should be set to active state
     */
    private boolean checkRuleActivate(SimulationRuleBean rule, double simulationTime) {
        return simulationTime >= rule.getActivationTime();
    }


    /**
     * creates new packets for one simulation rule
     * <p/>
     * creates as much packets as possible - each packet takes some time ("serialisationDelay")
     * to create and there is only a small amount of time ("timeQuantum") to work with
     *
     * @param rule
     * @param timeQuantum
     * @return
     */
    private List<Packet> generatePacketsFromSimulRule(SimulationRuleBean rule, double timeQuantum, double simulationTime) {
        List<Packet> packets = new LinkedList<Packet>();
        double timeSpent = 0;
        double creationTime = rule.getActivationTime() % timeQuantum;
        while (timeSpent <= creationTime && rule.getNumberOfPackets() > 0) {
            double creationDelay = DelayHelper.calculatePacketCreationDelay(rule.getSource(), rule.getPacketSize(), rule.getPacketTypeEnum());
            if (timeSpent + creationDelay > timeQuantum) break; //no time left to spent
            packets.add(createPacket(rule.getPacketSize(), rule, rule.getLayer4Type(), rule.getActivationTime() + timeSpent + simulationTime));
            timeSpent += creationDelay;//I have spent some time
            rule.decreaseNumberOfPackets();
        }
        if (logg.isDebugEnabled()) {
            logg.debug("Packets created: " + packets.size());
        }
        return packets;
    }

    /**
     * creates one packet
     *
     * @param packetSize   size of packet in bytes
     * @param rule         simulation rule that is associated with this packet
     * @param creationTime simulation time, when this packet was created
     * @return a new packet
     */
    private Packet createPacket(int packetSize, SimulationRuleBean rule, Layer4TypeEnum layer4, double creationTime) {
        if (rule.isPing()) {
            return new PingPacket(pingManager, packetSize, layer4, packetManager, rule, creationTime);
        }
        return new Packet(packetSize, layer4, packetManager, rule, creationTime);
    }
}
