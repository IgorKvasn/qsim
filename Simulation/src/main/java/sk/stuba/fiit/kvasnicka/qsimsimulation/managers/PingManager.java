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

package sk.stuba.fiit.kvasnicka.qsimsimulation.managers;

import org.apache.log4j.Logger;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.ping.PingPacketDeliveredEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.ping.PingPacketDeliveredListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.pingrule.PingRuleEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.pingrule.PingRuleListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.simulationrule.SimulationRuleListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Igor Kvasnicka
 */
public class PingManager implements PingPacketDeliveredListener {

    private static Logger logg = Logger.getLogger(PingManager.class);

    private Map<String, PingDefinition> pingDefinitions = new HashMap<String, PingDefinition>();
    private transient javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();
    private List<SimulationRuleBean> rules = new LinkedList<SimulationRuleBean>();

    public PingManager() {
    }


    public void addPing(SimulationRuleBean rule, int repetitions) {
        rule.addPingPacketDeliveredListener(this);
        SimulationRuleBean backSimRule = createBackPingSimulationRule(rule);
        PingDefinition def = new PingDefinition(repetitions, backSimRule, rule);
        pingDefinitions.put(rule.getUniqueID(), def);

        rules.add(rule);

        firePingRuleAddedEvent(new PingRuleEvent(this, rule));
    }

    public void removePing(SimulationRuleBean rule) {

        if (pingDefinitions.remove(rule.getUniqueID()) != null) {
            firePingRuleRemovedEvent(new PingRuleEvent(this, rule));
        } else {
            logg.warn("no matching ping simulation rule found to be deleted");
        }

        if (rules.remove(rule)) {
            firePingRuleRemovedEvent(new PingRuleEvent(this, rule));
        } else {
            logg.warn("no matching ping simulation rule found to be deleted");
        }
    }

    /**
     * returns list of all defined ping simulation beans
     *
     * @return
     */
    public List<SimulationRuleBean> getPingSimulationRules() {
        return rules;
    }

    /**
     * from a given simulation rule creates simulation rule when packet is returning
     * e.g ping from A to B: packet came to B and now I need
     *
     * @param simulationRule
     * @return
     */
    private SimulationRuleBean createBackPingSimulationRule(SimulationRuleBean simulationRule) {
        if (! simulationRule.isPing()) {
            throw new IllegalStateException("this method may be used only for ping simulation rules");
        }

        //just switch source and destination nodes
        SimulationRuleBean rule = new SimulationRuleBean(simulationRule.getName(), simulationRule.getDestination(), simulationRule.getSource(), simulationRule.getNumberOfPackets(), simulationRule.getPacketSize(), 0, simulationRule.getPacketTypeEnum(), simulationRule.getLayer4Type(), simulationRule.getIpPrecedence(), 0, 0);
        List<NetworkNode> newRoute = new LinkedList<NetworkNode>(simulationRule.getRoute());
        Collections.reverse(newRoute);//also switch route
        rule.setRoute(newRoute);
        return rule;
    }

    @Override
    public void packetDeliveredOccurred(PingPacketDeliveredEvent evt) {
        SimulationRuleBean rule = evt.getPacket().getSimulationRule();

        if (pingDefinitions.containsKey(rule.getUniqueID())) {//check if new ping packet should be created
            PingDefinition def = pingDefinitions.get(rule.getUniqueID());
            def.decreaseRepetitions();
            if (def.repetitions == 0) {//this was the last packet in ping definition
                pingDefinitions.remove(rule.getUniqueID());
                rule.removePingPacketDeliveredListener(this);
                return; //no more ping
            }
            //reset simulation rule
            rule.setActivationTime(evt.getPacket().getSimulationTime()); //when the last ping packet came, new is created
            rule.resetNumberOfPacketsToOne();
            rule.setActive(true);
        }
    }

    public void addPingRuleListener(PingRuleListener listener) {
        listenerList.add(PingRuleListener.class, listener);
    }

    public void removePingRuleListener(PingRuleListener listener) {
        listenerList.remove(PingRuleListener.class, listener);
    }

    private void firePingRuleAddedEvent(PingRuleEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i].equals(SimulationRuleListener.class)) {
                ((PingRuleListener) listeners[i + 1]).pingRuleAdded(evt);
            }
        }
    }

    private void firePingRuleRemovedEvent(PingRuleEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i].equals(SimulationRuleListener.class)) {
                ((PingRuleListener) listeners[i + 1]).pingRuleRemoved(evt);
            }
        }
    }

    public SimulationRuleBean createBackRule(SimulationRuleBean simulationRule) {
        PingDefinition def = pingDefinitions.get(simulationRule.getUniqueID());
        if (def == null) throw new IllegalStateException("cannot find simulation rule for ping");
        return def.backSimRule;
    }

    public SimulationRuleBean getOriginalRule(String simulationRuleId) {
        PingDefinition def = pingDefinitions.get(simulationRuleId);
        if (def == null) throw new IllegalStateException("cannot find simulation rule for ping");
        return def.originalSimRule;
    }

    private static final class PingDefinition {
        private int repetitions; //-1 if infinity
        private SimulationRuleBean backSimRule;
        private SimulationRuleBean originalSimRule;


        private PingDefinition(int repetitions, SimulationRuleBean backSimRule, SimulationRuleBean originalSimRule) {
            this.originalSimRule = originalSimRule;
            this.repetitions = repetitions;
            this.backSimRule = backSimRule;
        }


        private void decreaseRepetitions() {
            if (repetitions == - 1) { //infinite repetitions
                return;
            }
            repetitions--;
        }
    }
}
