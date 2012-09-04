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

package sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.impl;

import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.PacketClassificationInterf;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * each flow gets its own queue
 * flow is determined according its: src port, dest port, src Network node and dest Network node
 * all 4 if them must be equal to say, that 2 flows are equal
 *
 * @author Igor Kvasnicka
 */
public class FlowBasedClassification implements PacketClassificationInterf {
    private static final long serialVersionUID = 15345388523958772L;
    /**
     * key = flow
     * value = queue number
     */
    private transient Map<Flow, Integer> flows;
    /**
     * next available queue number for a flow
     */
    private transient int nextQueueNumber;

    public FlowBasedClassification() {
        flows = new HashMap<Flow, Integer>();
        nextQueueNumber = 0;
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        flows = new HashMap<Flow, Integer>();
        nextQueueNumber = 0;
    }

    @Override
    public int classifyAndMarkPacket(NetworkNode networkNode, Packet packet) {
        if (packet == null) throw new IllegalArgumentException("packet is NULL");
        if (packet.getSimulationRule() == null) throw new IllegalArgumentException("simulation rule is NULL");

        SimulationRuleBean rule = packet.getSimulationRule();
        Flow flow = new Flow(rule);
        if (flows.containsKey(flow)) {
            return flows.get(flow);
        }

        int queue = nextQueueNumber;
        nextQueueNumber++;
        flows.put(flow, queue);
        return queue;
    }

    private static class Flow {
        int srcPort;
        int destPort;
        NetworkNode srcNode;
        NetworkNode destNode;

        private Flow(SimulationRuleBean rule) {
            this.srcPort = rule.getSrcPort();
            this.destPort = rule.getDestPort();
            this.srcNode = rule.getSource();
            this.destNode = rule.getDestination();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Flow flow = (Flow) o;

            if (destPort != flow.destPort) return false;
            if (srcPort != flow.srcPort) return false;
            if (! destNode.equals(flow.destNode)) return false;
            if (! srcNode.equals(flow.srcNode)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = srcPort;
            result = 31 * result + destPort;
            result = 31 * result + srcNode.hashCode();
            result = 31 * result + destNode.hashCode();
            return result;
        }
    }
}
