package sk.stuba.fiit.kvasnicka.topologyvisual.graph.utils;

import edu.uci.ics.jung.visualization.LayeredIcon;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import edu.uci.ics.jung.visualization.decorators.DefaultVertexIconTransformer;
import edu.uci.ics.jung.visualization.picking.PickedState;
import java.awt.event.MouseEvent;
import org.apache.log4j.Logger;

import javax.swing.Icon;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.VertexSelectionManager;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.palette.PaletteActionEnum;
import sk.stuba.fiit.kvasnicka.topologyvisual.palette.gui.TopolElementTopComponent;

/**
 * @author Igor Kvasnicka
 */
public class VertexPickedListener implements GraphMouseListener<TopologyVertex> {

    private static Logger logg = Logger.getLogger(VertexPickedListener.class);
    private DefaultVertexIconTransformer<TopologyVertex> imager;
    private TopolElementTopComponent topComponent;
    private PickedState<TopologyVertex> ps;

    public VertexPickedListener(DefaultVertexIconTransformer<TopologyVertex> imager, TopolElementTopComponent topComponent, PickedState<TopologyVertex> ps) {
        this.imager = imager;
        this.topComponent = topComponent;
        this.ps = ps;
    }

    @Override
    public void graphClicked(TopologyVertex v, MouseEvent me) {
        logg.debug("vertex picked");
        Icon icon = imager.transform(v);
        if (icon != null && icon instanceof LayeredIcon) {
            if (v.isSelected() && !PaletteActionEnum.isEdgeAction(topComponent.getSelectedPaletteAction())) {
                //    pickedState.pick((TopologyVertex) e.getItem(), false); cannto be used, otherwise moving with vertices won't work at all
                v.setSelected(false);
                ((LayeredIcon) icon).remove(TopologyVertex.selectedIcon);
                VertexSelectionManager.getInstance().removeSelectedVertex(v);
            } else {//not selected
                if (PaletteActionEnum.isEdgeAction(topComponent.getSelectedPaletteAction())) { //new edge is being created
                    v.setSelected(false);
                    ((LayeredIcon) icon).add(TopologyVertex.routeCreationIcon);
                    topComponent.getTopologyElementCreator().vertexSelected(v);
                    ps.pick(v, false);
                } else {
                    v.setSelected(true);
                    ((LayeredIcon) icon).add(TopologyVertex.selectedIcon);
                    VertexSelectionManager.getInstance().addSelectedVertex(v);
                }
            }
        }
    }

    @Override
    public void graphPressed(TopologyVertex v, MouseEvent me) {
    }

    @Override
    public void graphReleased(TopologyVertex v, MouseEvent me) {
    }
}
