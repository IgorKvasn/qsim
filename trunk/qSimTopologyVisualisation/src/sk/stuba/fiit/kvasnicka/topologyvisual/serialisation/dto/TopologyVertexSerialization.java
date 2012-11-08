/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.serialisation.dto;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;

/**
 *
 * @author Igor Kvasnicka
 */
@Getter
@Setter
public class TopologyVertexSerialization implements Serializable {

    private static final long serialVersionUID = -403250646325465050L;
    private TopologyVertex node;
    private double x;
    private double y;

    public TopologyVertexSerialization(TopologyVertex node, double x, double y) {
        this.node = node;
        this.x = x;
        this.y = y;
    }
}
