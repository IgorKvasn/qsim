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
package sk.stuba.fiit.kvasnicka.topologyvisual.graph.utils;

/**
 * this enum lists all possible actions that may cause VertexPickedListener to
 * behave differently. e.g. when user is creating new edge, vertexPickedListener
 * must know about this
 *
 * @author Igor Kvasnicka
 */
public enum VertexPickActionEnum {

    CREATING_EDGE,//user is creating edges
    VERTEX_SELECTION_SIMULATION_RULE,//user is defining new simulation rule (destination/source vertex) by clicking on topology
    NONE//no special state
}
