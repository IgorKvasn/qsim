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

package sk.stuba.fiit.kvasnicka.qsimdatamodel.data;

import org.apache.log4j.Logger;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.UsageStatistics;
import sk.stuba.fiit.kvasnicka.qsimsimulation.SimulationTimer;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Fragment;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * @author Igor Kvasnicka
 */
public class Edge implements Serializable, UsageStatistics {

    private static Logger logg = Logger.getLogger(Edge.class);
    private static final double EDGE_SPEED_INCREMENT = (double) 3 / 2; //speed is multiplied with this
    private static final long serialVersionUID = - 6445317212124772953L;
    private long maxSpeed;   //bit/s
    private int length;
    private NetworkNode node1, node2;
    private int mtu;

    /**
     * all fragments that are on the wire
     */
    private transient List<Fragment> fragments;
    /**
     * probability that packet will be erroneous
     * its value is from 0 (included) to 1 (included)
     * 0 means that all packets are OK; 1 means that all packets are CRC wrong
     */
    private double packetErrorRate;

    /**
     * each TCP simulation rule (= TCP flow) has its own speed because of TCP congestion avoidance
     */
    private transient Map<SimulationRuleBean, Long> speedMap;

    private static final long MIN_SPEED = 1;

    private transient TreeSet<CongestedInfo> congestedInfoSet;
    private static final int PERCENTAGE = 100;

    /**
     * creates new instance of Edge object with maxSpeed parameter defined do not
     * forget to set length parameter later on
     *
     * @param speed bitrate [bit/s]
     */
    public Edge(long speed, int mtu, int length, double packetErrorRate, NetworkNode node1, NetworkNode node2) {
        this.maxSpeed = speed;
        this.mtu = mtu;
        this.length = length;
        this.packetErrorRate = packetErrorRate;
        this.node1 = node1;
        this.node2 = node2;

        speedMap = new HashMap<SimulationRuleBean, Long>();
        congestedInfoSet = new TreeSet<CongestedInfo>();
        fragments = new LinkedList<Fragment>();
    }


    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        speedMap = new HashMap<SimulationRuleBean, Long>();
        congestedInfoSet = new TreeSet<CongestedInfo>();
        fragments = new LinkedList<Fragment>();
    }


    public double getPacketErrorRate() {
        return packetErrorRate;
    }


    public int getMtu() {
        if (mtu == - 1) {
            throw new IllegalStateException("maxSpeed of this edge us not defined");
        }
        return mtu;
    }

    /**
     * each simulation rule has its of speed for this link
     * this simulates TCP congestion avoidance mechanism
     *
     * @param packet
     * @return
     */
    public long getSpeed(Packet packet) {
        if (packet == null) throw new IllegalArgumentException("packet is NULL");
        if (packet.getSimulationRule() == null) throw new IllegalStateException("simulation rule in packet is NULL");

        if (Layer4TypeEnum.TCP != packet.getLayer4()) {//only TCP packets has got complicated edge speed calculations
            return maxSpeed;
        }

        //apply congestion information
        applyCongestion(packet.getSimulationTime());

        if (speedMap.containsKey(packet.getSimulationRule())) {
            return speedMap.get(packet.getSimulationRule());
        }
        speedMap.put(packet.getSimulationRule(), maxSpeed);
        return maxSpeed;
    }

    /**
     * check if there is congestion detected and apply them (decrease edge speed)
     *
     * @param simulationTime
     */
    private void applyCongestion(double simulationTime) {
        CongestedInfo congestedInfo;
        for (; ; ) {
            if (congestedInfoSet.isEmpty()) {
                break;
            }
            congestedInfo = congestedInfoSet.first();
            if (congestedInfo.simulationTime <= simulationTime) {//congestion should be applied now
                for (int i = 0; i < congestedInfo.count; i++) {
                    decreaseSpeed(congestedInfo.rule);
                }
                congestedInfoSet.remove(congestedInfo);
            } else {
                break;
            }
        }
    }

    /**
     * decreases edge speed by one half - according to TCP congestion avoidance algorithm
     * however this will happen not now, but after network node's TCP timer expires - this is determined by method's second argument
     *
     * @param rule
     * @param simulationTime simulation time, when congestion will be detected
     */
    public void decreaseSpeed(SimulationRuleBean rule, double simulationTime) {
        if (rule == null) throw new IllegalArgumentException("rule is NULL");

        CongestedInfo congInfo = new CongestedInfo(rule, simulationTime);

        if (congestedInfoSet.contains(congInfo)) { //congestion info with this simulation time already exists
            for (CongestedInfo c : congestedInfoSet) {//find this info
                if (congInfo.equals(c)) {
                    c.count++;//increase count of infos
                    if (logg.isDebugEnabled()) {
                        logg.debug("new congestion info for rule: " + rule.getName() + "; simulation time: " + simulationTime);
                    }
                    return;
                }
            }
        }

        congestedInfoSet.add(congInfo);
        if (logg.isDebugEnabled()) {
            logg.debug("new congestion info for rule: " + rule.getName() + "; simulation time: " + simulationTime);
        }
    }

    /**
     * actually decreases edge speed by one half
     *
     * @param rule
     */
    private void decreaseSpeed(SimulationRuleBean rule) {
        if (rule == null) throw new IllegalArgumentException("rule is NULL");

        if (speedMap.containsKey(rule)) {
            if (speedMap.get(rule) == MIN_SPEED) {
                return;
            }
        }
        if (speedMap.containsKey(rule)) {
            speedMap.put(rule, speedMap.get(rule) / 2);
        } else {
            logg.warn("speed is not in map (never queued before), but it is already decreasing - quite strange, isn't it?");
            speedMap.put(rule, maxSpeed / 2);
        }

        //link speed must not be less than MIN_SPEED
        if (speedMap.get(rule) < MIN_SPEED) speedMap.put(rule, MIN_SPEED);

        if (logg.isDebugEnabled()) {
            logg.debug("decreasing edge speed to: " + speedMap.get(rule) + "; max speed is: " + maxSpeed);
        }
    }

    /**
     * increases link speed by one quarter - according to TCP congestion avoidance algorithm
     *
     * @param rule
     */
    public void increaseSpeed(SimulationRuleBean rule) {
        if (rule == null) throw new IllegalArgumentException("rule is NULL");

        if (speedMap.containsKey(rule)) {
            if (speedMap.get(rule) == maxSpeed) {
                return;
            }
        }

        if (speedMap.containsKey(rule)) {
            speedMap.put(rule, Math.round(speedMap.get(rule) * EDGE_SPEED_INCREMENT));
        } else {
            logg.warn("speed is not in map (never queued before), but it is already increasing - quite strange, isn't it?");
            speedMap.put(rule, Math.round(maxSpeed * EDGE_SPEED_INCREMENT));
        }

        if (speedMap.get(rule) > maxSpeed) speedMap.put(rule, maxSpeed);

        if (logg.isDebugEnabled()) {
            logg.debug("increasing edge speed to: " + speedMap.get(rule) + "; max speed is: " + getMaxSpeed());
        }
    }

    /**
     * returns links bitrate [bit/s]
     *
     * @return bitrate
     */
    public long getMaxSpeed() {
        if (maxSpeed == - 1) {
            throw new IllegalStateException("maxSpeed of this edge us not defined");
        }
        return maxSpeed;
    }


    /**
     * sets how long is this link [m]
     *
     * @return length of the link
     */
    public int getLength() {
        if (length == - 1) {
            throw new IllegalStateException("length of this edge us not defined");
        }
        return length;
    }


    public NetworkNode getNode1() {
        return node1;
    }

    public NetworkNode getNode2() {
        return node2;
    }

    public List<Fragment> getFragments() {
        return fragments;
    }

    public void addFragment(Fragment fragment) {
        fragments.add(fragment);
    }

    public void moveFragmentsToNetworkNode(double simulationTime) {
        for (Iterator<Fragment> iterator = fragments.iterator(); iterator.hasNext(); ) {
            Fragment fragment = iterator.next();
            if (fragment.getReceivedTime() <= simulationTime) { //this packet was propagated and serialised on the destination (next-hop) network node

                //remove fragment from the edge
                iterator.remove();

                //add fragment to the appropriate network node
                fragment.getTo().addToRxBuffer(fragment);
            }
        }
    }

    /**
     * returns network node on the opposite end of the edge
     *
     * @param node
     * @return
     */
    public NetworkNode findOppositeNetworkNode(NetworkNode node) {
        if (node == null) throw new IllegalArgumentException("node is NULL");
        if (node1.getName().equals(node.getName())) return node2;
        if (node2.getName().equals(node.getName())) return node1;
        throw new IllegalStateException("node: " + node.getName() + " could not be found on the edge: " + node1.getName() + " <-> " + node2.getName());
    }

    /**
     * determines if this edge contains specified network node
     *
     * @param node
     * @return
     */
    public boolean containsNode(NetworkNode node) {
        if (node == null) throw new IllegalArgumentException("node is NULL");

        if (node1.getName().equals(node.getName())) return true;
        if (node2.getName().equals(node.getName())) return true;

        return false;
    }

    /**
     * returns percentage usage of the edge during last simulation quantum
     */
    @Override
    public double getUsage() {
        //calculate size of fragments on the wire
        long size = 0;
        for (Fragment fragment : fragments) {
            size += fragment.getFragmentSize();
        }
        double bytesPerSecond = size * (SimulationTimer.MILIS_IN_SECOND / (SimulationTimer.TIME_QUANTUM * SimulationTimer.NANOS_IN_MILIS));

        if (bytesPerSecond > getMaxSpeed()) {
            logg.error("edge usage is above 100% -> " + (bytesPerSecond / getMaxSpeed()) * PERCENTAGE);
            return 100;
        }

        return (bytesPerSecond / getMaxSpeed()) * PERCENTAGE;
    }


    private static final class CongestedInfo implements Comparable<CongestedInfo> {
        private final SimulationRuleBean rule;
        private final double simulationTime;
        private int count; //there can be multiple congestedInfo objects with the same simulation time (theoretically - quite handy in tests)

        private CongestedInfo(SimulationRuleBean rule, double simulationTime) {
            this.rule = rule;
            this.simulationTime = simulationTime;
            this.count = 1;
        }

        @Override
        public int compareTo(CongestedInfo o) {
            return ((Double) simulationTime).compareTo(o.simulationTime);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CongestedInfo that = (CongestedInfo) o;

            if (Double.compare(that.simulationTime, simulationTime) != 0) return false;

            return true;
        }

        @Override
        public int hashCode() {
            long temp = simulationTime != + 0.0d ? Double.doubleToLongBits(simulationTime) : 0L;
            return (int) (temp ^ (temp >>> 32));
        }
    }
}
