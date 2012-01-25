/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
