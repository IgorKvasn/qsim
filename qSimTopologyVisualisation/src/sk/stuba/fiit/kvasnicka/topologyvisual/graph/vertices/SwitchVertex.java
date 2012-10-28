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

import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Switch;
import sk.stuba.fiit.kvasnicka.topologyvisual.resources.ImageType;

/**
 * vertex in topology that represents switch
 *
 * @author Igor Kvasnicka
 */
public class SwitchVertex extends TopologyVertex {

    public SwitchVertex(NetworkNode sw) {
        super(ImageType.TOPOLOGY_VERTEX_SWITCH, sw.getName(), sw.getDescription());
        if (sw instanceof Switch) {
            setDataModel(sw);
        } else {
            throw new IllegalArgumentException("SwitchVertex must have reference to underlying Switch object");
        }
    }
}
