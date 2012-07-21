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
package sk.stuba.fiit.kvasnicka.topologyvisual.utils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.edges.TopologyEdge;

/**
 *
 * @author Igor Kvasnicka
 */
public class EdgeUtils {

    public static boolean isEdgesEqual(TopologyEdge e1, TopologyEdge e2) {

        if (e1 == null) {
            throw new IllegalStateException("edge is NULL");
        }
        if (e2 == null) {
            throw new IllegalStateException("edge is NULL");
        }

        if (e1.getVertex1().getName().equals(e2.getVertex1().getName())) {//the first vertex is the same        
            if (e1.getVertex2().getName().equals(e2.getVertex2().getName())) {//also the second
                return true;
            } else {
                return false;
            }
        }

        if (e1.getVertex1().getName().equals(e2.getVertex2().getName())) {//the first vertex is the same        
            if (e1.getVertex2().getName().equals(e2.getVertex1().getName())) {//also the second
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    /**
     * creates list of data model Edge
     *
     * @return
     */
    public static List<Edge> convertTopologyEdgeListToEdgeList(Collection<TopologyEdge> edgeList) {
        if (edgeList == null) {
            throw new IllegalArgumentException("edgeList is NULL");
        }
        List<Edge> list = new LinkedList<Edge>();
        for (TopologyEdge edge : edgeList) {
            list.add(edge.getEdge());
        }
        return list;
    }
}
