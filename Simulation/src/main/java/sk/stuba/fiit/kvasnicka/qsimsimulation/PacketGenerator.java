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
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.ruleactivation.SimulationRuleActivationEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.ruleactivation.SimulationRuleActivationListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.helpers.DelayHelper;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PacketManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PingManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.PingPacket;
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
    private javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();

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
            if (! rule.isCanCreateNewPacket()) continue; //this rule simply cannot generate new packet

            if (rule.isActive()) {//rule has been activated and it is not finished yet
                addPacketsToNetworkNode(timeQuantum, rule, simulationTime);
            } else {//check if the time came to activate this rule
                if (checkRuleActivate(rule, simulationTime)) {//yes, I should activate it
                    fireSimulationRuleActivatedEvent(new SimulationRuleActivationEvent(this, rule));
                    rule.setActive(true);
                    addPacketsToNetworkNode(timeQuantum, rule, simulationTime);
                    rule.increaseActivationTime(timeQuantum);
                }
            }

            //new packet has been generated - now it's a good time to decide if other new packets can be created (this is only for ping rules)
            if (rule.isPing()) {
                rule.setCanCreateNewPacket(false);
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
        double timeSpent = 0 - rule.getCreationTimeSaved();
        double creationTime = rule.getActivationTime() % timeQuantum;

        while (timeSpent <= creationTime && (rule.getNumberOfPackets() > 0 || rule.getNumberOfPackets() == - 1)) {
            if (rule.isPing() && packets.size() == 1) { //there can be created only 1 ICMP ping at a time
                break;
            }

            double creationDelay = DelayHelper.calculatePacketCreationDelay(rule, rule.getPacketSize(), simulationTime + timeSpent);
            if (timeSpent + creationDelay > timeQuantum) break; //no time left to spent
            packets.add(createPacket(rule, rule.getActivationTime() % timeQuantum + simulationTime));
            timeSpent += creationDelay;//I have spent some time
            rule.decreaseNumberOfPackets();

            if (rule.isFinished()) fireSimulationRuleFinishedEvent(new SimulationRuleActivationEvent(this, rule));
        }

        if (logg.isDebugEnabled()) {
            logg.debug("Packets created: " + packets.size());
        }
        rule.setCreationTimeSaved(timeQuantum-timeSpent);
        return packets;
    }

    /**
     * creates one packet
     *
     * @param rule         simulation rule that is associated with this packet
     * @param creationTime simulation time, when this packet was created
     * @return a new packet
     */
    private Packet createPacket(SimulationRuleBean rule, double creationTime) {
        if (rule.isPing()) {
            return new PingPacket(pingManager, rule.getPacketSize(), packetManager, rule, creationTime);
        }
        return new Packet(rule.getPacketSize(), packetManager, rule, creationTime);
    }

    public void addSimulationRuleActivationListener(SimulationRuleActivationListener listener) {
        listenerList.add(SimulationRuleActivationListener.class, listener);
    }

    public void removeSimulationRuleActivationListener(SimulationRuleActivationListener listener) {
        listenerList.remove(SimulationRuleActivationListener.class, listener);
    }

    private void fireSimulationRuleActivatedEvent(SimulationRuleActivationEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i].equals(SimulationRuleActivationListener.class)) {
                ((SimulationRuleActivationListener) listeners[i + 1]).simulationRuleActivatedOccurred(evt);
            }
        }
    }

    private void fireSimulationRuleFinishedEvent(SimulationRuleActivationEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i].equals(SimulationRuleActivationListener.class)) {
                ((SimulationRuleActivationListener) listeners[i + 1]).simulationRuleFinishedOccurred(evt);
            }
        }
    }
}
