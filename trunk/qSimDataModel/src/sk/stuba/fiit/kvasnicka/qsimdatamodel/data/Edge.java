package sk.stuba.fiit.kvasnicka.qsimdatamodel.data;

/**
 * @author Igor Kvasnicka
 */
public class Edge {

    private long speed;
    private int length;
    private NetworkNode node1, node2;

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
