package sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices;

import sk.stuba.fiit.kvasnicka.topologyvisual.data.Computer;
import sk.stuba.fiit.kvasnicka.topologyvisual.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.topologyvisual.resources.ImageType;

/**
 * User: Igor Kvasnicka Date: 9/2/11 Time: 1:33 PM
 */
/**
 * vertex in topology that represents computer
 */
public class ComputerVertex extends TopologyVertex {

    private Computer computer;

    /**
     * creates instance
     *
     * @param computer computer associated with this vertex
     */
    public ComputerVertex(NetworkNode computer) {
        super(ImageType.TOPOLOGY_VERTEX_COMPUTER, computer.getName());
        if (computer instanceof Computer) {
            this.computer = (Computer) computer;
        } else {
            throw new IllegalArgumentException("ComputerVertex must have reference to underlying Computer object");
        }
    }

    @Override
    public NetworkNode getDataModel() {
        return computer;
    }
}
