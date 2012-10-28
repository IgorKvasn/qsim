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

import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Computer;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.topologyvisual.resources.ImageType;

/**
 * User: Igor Kvasnicka Date: 9/2/11 Time: 1:33 PM
 */
/**
 * vertex in topology that represents computer
 */
public class ComputerVertex extends TopologyVertex {

    /**
     * creates instance
     *
     * @param computer computer associated with this vertex
     */
    public ComputerVertex(NetworkNode computer) {
        super(ImageType.TOPOLOGY_VERTEX_COMPUTER, computer.getName(), computer.getDescription());
        if (computer instanceof Computer) {
            setDataModel(computer);
        } else {
            throw new IllegalArgumentException("ComputerVertex must have reference to underlying Computer object");
        }
    }
}
