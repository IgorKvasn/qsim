package sk.stuba.fiit.kvasnicka.topologyvisual.graph.utils;

import edu.uci.ics.jung.visualization.LayeredIcon;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import edu.uci.ics.jung.visualization.decorators.DefaultVertexIconTransformer;
import edu.uci.ics.jung.visualization.picking.PickedState;
import java.awt.event.MouseEvent;
import org.apache.log4j.Logger;

import javax.swing.Icon;
import org.openide.windows.WindowManager;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.AddSimulationTopComponent;
import sk.stuba.fiit.kvasnicka.topologyvisual.palette.PaletteActionEnum;
import sk.stuba.fiit.kvasnicka.topologyvisual.palette.gui.TopologyMultiviewElement;
import sk.stuba.fiit.kvasnicka.topologyvisual.resources.ImageResourceHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.resources.ImageType;

/**
 * @author Igor Kvasnicka
 */
public class VertexPickedListener implements GraphMouseListener<TopologyVertex> {

    private static Logger logg = Logger.getLogger(VertexPickedListener.class);
    private DefaultVertexIconTransformer<TopologyVertex> imager;
    private TopologyMultiviewElement topComponent;
    private PickedState<TopologyVertex> ps;
    private AddSimulationTopComponent component;

    public VertexPickedListener(DefaultVertexIconTransformer<TopologyVertex> imager, TopologyMultiviewElement topComponent, PickedState<TopologyVertex> ps) {
        this.imager = imager;
        this.topComponent = topComponent;
        this.ps = ps;
        component = (AddSimulationTopComponent) WindowManager.getDefault().findTopComponent("AddSimulationTopComponent");
        if (component == null) {
            throw new IllegalStateException("Could not ind window: AddSimulationTopComponent");
        }
    }

    /**
     * well, this method really needs some refactoring...maybe next time :)
     */
    @Override
    public void graphClicked(TopologyVertex v, MouseEvent me) {
        logg.debug("vertex picked");
        Icon icon = imager.transform(v);
        if (icon != null && icon instanceof LayeredIcon) {
            if (v.isSelected()) {//is selected
                if (!PaletteActionEnum.isEdgeAction(topComponent.getSelectedPaletteAction())) {//edge not creating
                    v.setSelected(false);
                    selectVertex(((LayeredIcon) icon), v.getImageType(), false);
                    topComponent.getVertexSelectionManager().removeSelectedVertex(v);
                }
            } else {//not selected
                if (PaletteActionEnum.isEdgeAction(topComponent.getSelectedPaletteAction())) { //new edge is being created
                    v.setSelected(false);
                    ((LayeredIcon) icon).setImage(ImageResourceHelper.loadCheckedImageVertexAsImage(v.getImageType()));
                    topComponent.getTopologyElementCreator().vertexSelected(v);
                    ps.pick(v, false);
                } else {//not selected - edge not creating
                    v.setSelected(true);
                    selectVertex(((LayeredIcon) icon), v.getImageType(), true);
                    topComponent.getVertexSelectionManager().addSelectedVertex(v);
                }
            }
        }
    }

    private void selectVertex(LayeredIcon icon, ImageType imageType, boolean select) {
        icon.setImage(ImageResourceHelper.loadImageVertexAsImage(imageType, select));
    }

    @Override
    public void graphPressed(TopologyVertex v, MouseEvent me) {
    }

    @Override
    public void graphReleased(TopologyVertex v, MouseEvent me) {
    }
}
