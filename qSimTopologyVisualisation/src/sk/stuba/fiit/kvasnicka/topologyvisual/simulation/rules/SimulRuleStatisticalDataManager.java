/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.simulation.rules;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.ping.PingPacketDeliveredEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.ping.PingPacketDeliveredListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.pingrule.PingRuleEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.pingrule.PingRuleListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.simulationrule.SimulationRuleEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.simulationrule.SimulationRuleListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

/**
 *
 * gathers statistical data (SimulRuleStatisticalData object) for all simulation
 * rules
 *
 * @author Igor Kvasnicka
 */
public final class SimulRuleStatisticalDataManager implements PingRuleListener, SimulationRuleListener, PingPacketDeliveredListener {

    private Map<String, SimulRuleStatisticalData> dataMap;//key=unique ID of simul rule; value = statistical data

    public SimulRuleStatisticalDataManager(List<SimulationRuleBean> rules) {
        dataMap = new HashMap<String, SimulRuleStatisticalData>(rules.size() * 4 / 3);
        for (SimulationRuleBean rule : rules) {
            addData(rule);
        }
    }

    public Collection<SimulRuleStatisticalData> getStatisticalData() {
        return dataMap.values();
    }

    private void addData(SimulationRuleBean rule) {
        if (dataMap.containsKey(rule.getUniqueID())) {
            throw new IllegalStateException("such simulation rule already exists in StatisticalDataManager");
        }
        dataMap.put(rule.getUniqueID(), createStatisticalData(rule));
    }

    private void removeData(SimulationRuleBean rule) {
        if (!dataMap.containsKey(rule.getUniqueID())) {
            throw new IllegalStateException("no such simulation rule already exists in StatisticalDataManager");
        }
        dataMap.remove(rule.getUniqueID());

    }

    private SimulRuleStatisticalData createStatisticalData(SimulationRuleBean rule) {
        return new SimulRuleStatisticalData(rule);
    }

    public SimulRuleStatisticalData getStatisticalData(SimulationRuleBean rule) {
        if (rule == null) {
            throw new IllegalArgumentException("rule is NULL");
        }
        if (!dataMap.containsKey(rule.getUniqueID())) {
            throw new IllegalStateException("could not find simulation rule");
        }
        return dataMap.get(rule.getUniqueID());
    }

    private void packetDelivered(Packet packet) {
        SimulRuleStatisticalData statisticalData = getStatisticalData(packet.getSimulationRule());
        statisticalData.addDelay(packet.getSimulationTime() - packet.getCreationTime(), packet.getSimulationTime());
    }

    @Override
    public void pingRuleAdded(PingRuleEvent event) {
        addData(event.getRule());
    }

    @Override
    public void pingRuleRemoved(PingRuleEvent event) {
        removeData(event.getRule());
    }

    @Override
    public void simulationRuleAdded(SimulationRuleEvent event) {
        addData(event.getRule());
    }

    @Override
    public void simulationRuleRemoved(SimulationRuleEvent event) {
        removeData(event.getRule());
    }

    @Override
    public void pingPacketDeliveredOccurred(PingPacketDeliveredEvent evt) {
        packetDelivered(evt.getPacket());
    }
}
