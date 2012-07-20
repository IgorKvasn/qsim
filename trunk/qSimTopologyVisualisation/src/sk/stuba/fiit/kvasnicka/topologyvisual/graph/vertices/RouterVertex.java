/*
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
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices;

import lombok.EqualsAndHashCode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;
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
        super(ImageType.TOPOLOGY_VERTEX_ROUTER, router.getName(), router.getDescription());
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
}
