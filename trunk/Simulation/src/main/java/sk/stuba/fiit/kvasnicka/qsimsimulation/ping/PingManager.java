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

package sk.stuba.fiit.kvasnicka.qsimsimulation.ping;

import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.SimulationRuleBean;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.ping.PingPacketDeliveredEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.ping.PingPacketDeliveredListener;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Igor Kvasnicka
 */
public class PingManager implements PingPacketDeliveredListener {

    private Map<String, PingDefinition> pingDefinitions = new HashMap<String, PingDefinition>();

    public PingManager() {
    }


    public void addPing(SimulationRuleBean rule, int repetitions) {
        rule.addPingPacketDeliveredListener(this);
        SimulationRuleBean backSimRule = createBackPingSimulationRule(rule);
        PingDefinition def = new PingDefinition(repetitions, backSimRule, rule);
        pingDefinitions.put(rule.getUniqueID(), def);
    }

    /**
     * from a given simulation rule creates simulation rule when packet is returning
     * e.g ping from A to B: packet came to B and now I need
     *
     * @param simulationRule
     * @return
     */
    private SimulationRuleBean createBackPingSimulationRule(SimulationRuleBean simulationRule) {
        //just switch source and destination nodes
        SimulationRuleBean rule = new SimulationRuleBean(simulationRule.getDestination(), simulationRule.getSource(), simulationRule.getNumberOfPackets(), simulationRule.getPacketSize(), 0, simulationRule.getPacketTypeEnum(), simulationRule.getLayer4Type(), true);
        List<NetworkNode> newRoute = new LinkedList<NetworkNode>(simulationRule.getRoute());
        Collections.reverse(newRoute);//also switch route
        rule.addRoute(newRoute);
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
                return; //no more ping
            }
            //reset simulation rule
            rule.setActivationTime(evt.getPacket().getSimulationTime()); //when the last ping packet came, new is created
            rule.resetNumberOfPacketsToOne();
            rule.setActive(true);
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
