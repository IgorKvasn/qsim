package sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices;

import lombok.EqualsAndHashCode;
import sk.stuba.fiit.kvasnicka.topologyvisual.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.topologyvisual.data.Router;
import sk.stuba.fiit.kvasnicka.topologyvisual.resources.ImageType;

/**
 * User: Igor Kvasnicka Date: 9/2/11 Time: 12:32 PM
 */
/**
 * vertex in topology that represents router
 */
public class RouterVertex extends TopologyVertex {

    private Router router;

    /**
     * creates new instance
     *
     * @param router router associated with this vertex
     */
    public RouterVertex(NetworkNode router) {
        super(ImageType.TOPOLOGY_VERTEX_ROUTER, router.getName());
        if (router instanceof Router) {
            this.router = (Router) router;
        } else {
            throw new IllegalArgumentException("RouterVertex must have reference to underlying Router object");
        }
    }

    @Override
    public NetworkNode getDataModel() {
        return router;
    }

    @Override
    public boolean isRoutingAllowed() {
        return true;
    }
}
