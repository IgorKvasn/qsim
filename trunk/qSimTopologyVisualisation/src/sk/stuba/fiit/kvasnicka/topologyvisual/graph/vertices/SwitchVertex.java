/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices;

import sk.stuba.fiit.kvasnicka.topologyvisual.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.topologyvisual.data.Switch;
import sk.stuba.fiit.kvasnicka.topologyvisual.resources.ImageType;

/**
 * vertex in topology that represents switch
 *
 * @author Igor Kvasnicka
 */
public class SwitchVertex extends TopologyVertex {

    private Switch sw;

    public SwitchVertex(NetworkNode sw) {
        super(ImageType.TOPOLOGY_VERTEX_SWITCH, sw.getName());
        if (sw instanceof Switch) {
            this.sw = (Switch) sw;
        } else {
            throw new IllegalArgumentException("SwitchVertex must have reference to underlying Switch object");
        }
    }

    @Override
    public NetworkNode getDataModel() {
        return sw;
    }
}
