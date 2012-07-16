/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.utils;

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
}
