package sk.stuba.fiit.kvasnicka.topologyvisual.palette;

import lombok.Getter;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.utils.VertexPickActionEnum;

/**
 * Date: 9/3/11 Time: 3:44 PM
 * <p/>
 * what type of action is selected in task pane
 *
 * @author Igor Kvasnicka
 */
public enum PaletteActionEnum {

    NEW_VERTEX_ROUTER("router", VertexPickActionEnum.NONE),
    NEW_VERTEX_PC("PC", VertexPickActionEnum.NONE),
    NEW_VERTEX_SWITCH("Switch", VertexPickActionEnum.NONE),
    NEW_EDGE_ETHERNET("Ethernet", VertexPickActionEnum.NONE),
    NEW_EDGE_FAST_ETHERNET("Fast Ethernet", VertexPickActionEnum.CREATING_EDGE),
    NEW_EDGE_GIGA_ETHERNET("Giga Ethernet", VertexPickActionEnum.CREATING_EDGE),
    NEW_EDGE_CUSTOM("Custom link", VertexPickActionEnum.CREATING_EDGE);
    @Getter
    private final String displayableName;
    @Getter
    private final VertexPickActionEnum vertexPickActionEnum;

    /**
     * constructor
     *
     * @param displayableName name that will be displayed in palette window
     * @param vertexPickActionEnum action that will be used in
     * VertexPickedListener - edges should be
     * VertexPickActionEnum.CREATING_EDGE, vertices VertexPickActionEnum.NONE
     */
    PaletteActionEnum(String displayableName, VertexPickActionEnum vertexPickActionEnum) {
        this.displayableName = displayableName;
        this.vertexPickActionEnum = vertexPickActionEnum;
    }

    /**
     * determines if specified action is action to create new edge
     *
     * @param action action to be determined
     * @return true if new edge is being created
     */
    public boolean isEdgeAction() {
        return VertexPickActionEnum.CREATING_EDGE == this.vertexPickActionEnum;
    }
}
