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
package sk.stuba.fiit.kvasnicka.topologyvisual.serialisation.transform;

import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.topologyvisual.serialisation.dto.EdgeDTO;

/**
 *
 * @author Igor Kvasnicka
 */
public class EdgeTransform {

    public static EdgeDTO transformToDTO(Edge e) {
        EdgeDTO ed = new EdgeDTO();
        ed.setLength(e.getLength());
        ed.setSpeed(e.getSpeed());
        ed.setNode1(e.getNode1().getName());
        ed.setNode2(e.getNode2().getName());
        return ed;
    }
    
    
}
