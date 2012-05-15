package sk.stuba.fiit.kvasnicka.qsimdatamodel.data;

import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Fragment;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Igor Kvasnicka
 */
public class Edge {

    private long speed;
    private int length;
    private NetworkNode node1, node2;
    private int mtu;

    private List<Packet> packets = new LinkedList<Packet>();//todo toto tu asi je zbytocne, lebo teraz uz su fragmenty
    /**
     * all fragments that are on the wire
     */
    private List<Fragment> fragments = new LinkedList<Fragment>();

    /**
     * creates new instance of Edge object with speed parameter defined do not
     * forget to set length parameter later on
     *
     * @param speed bitrate [bit/s]
     * @param mtu   maximum transfer unit
     */
    public Edge(long speed, NetworkNode node1, NetworkNode node2, int mtu) {//fixme mozno mtu nebude ako argument, ale podobne ako speed a length sa bude nastavovat neskor
        this.speed = speed;
        this.mtu = mtu;
        length = - 1;
        this.node1 = node1;
        this.node2 = node2;
    }

    /**
     * used when in time of creating new instance, speed and length parameters
     * are not known yet
     */
    public Edge(NetworkNode node1, NetworkNode node2, int mtu) { //fixme ako v tom druhom konstruktore
        this.mtu = mtu;
        speed = - 1;
        length = - 1;
        this.node1 = node1;
        this.node2 = node2;
    }

    public void addPacket(Packet packet) {
        packets.add(packet);
    }

    public void removePacket(Packet packet) {
        packets.remove(packet);
    }

    public List<Packet> getPackets(double simulationTime) {
        List<Packet> list = new LinkedList<Packet>();
        for (Packet packet : packets) {
            if (packet.getTimeWhenNextStateOccures() <= simulationTime) {
                list.add(packet);
            }
        }
        return list;
    }

    public List<Packet> getAllPackets() {
        return packets;
    }

    public int getMtu() {
        if (mtu == - 1) {
            throw new IllegalStateException("speed of this edge us not defined");
        }
        return mtu;
    }

    public void setMtu(int mtu) {
        this.mtu = mtu;
    }

    /**
     * returns links bitrate [bit/s]
     *
     * @return bitrate
     */
    public long getSpeed() {
        if (speed == - 1) {
            throw new IllegalStateException("speed of this edge us not defined");
        }
        return speed;
    }

    /**
     * sets link's bitrate [bit/s]
     *
     * @param speed bitrate
     */
    public void setSpeed(long speed) {
        this.speed = speed;
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

    /**
     * sets length of link [m]
     *
     * @param length lenght of the link
     */
    public void setLength(int length) {
        this.length = length;
    }

    public NetworkNode getNode1() {
        return node1;
    }

    public NetworkNode getNode2() {
        return node2;
    }

    public void addFragment(Fragment fragment) {
        fragments.add(fragment);
    }

    public void moveFragmentsToNetworkNode(double simulationTime) {
        for (int i = 0, fragmentsSize = fragments.size(); i < fragmentsSize; i++) {
            Fragment fragment = fragments.get(i);

            if (fragment.getSimulationTime() <= simulationTime) { //this packet was propagated and serialised on the destination (next-hop) network node

                //remove fragment from the edge
                fragments.remove(fragment);//critical kvoli tomuto otestovat
                i--;

                //add fragment to the appropriate network node
                fragment.getTo().addToRxBuffer(fragment, fragment.getSimulationTime());
            }
        }
    }
}
