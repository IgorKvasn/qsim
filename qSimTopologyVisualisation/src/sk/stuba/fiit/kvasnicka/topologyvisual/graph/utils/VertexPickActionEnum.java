/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
