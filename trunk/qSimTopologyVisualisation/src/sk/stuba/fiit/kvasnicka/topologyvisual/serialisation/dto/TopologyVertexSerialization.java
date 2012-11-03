/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.serialisation.dto;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.topologyvisual.palette.PaletteActionEnum;
import sk.stuba.fiit.kvasnicka.topologyvisual.resources.ImageType;

/**
 *
 * @author Igor Kvasnicka
 */
@Getter
@Setter
public class TopologyVertexSerialization implements Serializable {

    private static final long serialVersionUID = -403250646215465050L;
    private NetworkNode node;
    private double x;
    private double y;
    private ImageType imageType;

    public TopologyVertexSerialization(NetworkNode node, double x, double y, ImageType imageType) {
        this.node = node;
        this.x = x;
        this.y = y;
        this.imageType = imageType;
    }
}
