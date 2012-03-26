package sk.stuba.fiit.kvasnicka.qsimdatamodel.data;

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

    private List<Packet> packets = new LinkedList<Packet>();

    /**
     * creates new instance of Edge object with speed parameter defined do not
     * forget to set length parameter later on
     *
     * @param speed bitrate [bit/s]
     */
    public Edge(long speed, NetworkNode node1, NetworkNode node2) {
        this.speed = speed;
        length = - 1;
        this.node1 = node1;
        this.node2 = node2;
    }

    /**
     * used when in time of creating new instance, speed and length parameters
     * are not known yet
     */
    public Edge(NetworkNode node1, NetworkNode node2) {
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

    public List<Packet> getAllPackets(){
        return packets;
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
}
