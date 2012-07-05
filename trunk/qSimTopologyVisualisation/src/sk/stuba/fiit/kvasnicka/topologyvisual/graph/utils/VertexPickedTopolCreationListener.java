package sk.stuba.fiit.kvasnicka.topologyvisual.graph.utils;

import edu.uci.ics.jung.visualization.LayeredIcon;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import edu.uci.ics.jung.visualization.decorators.DefaultVertexIconTransformer;
import edu.uci.ics.jung.visualization.picking.PickedState;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import org.apache.log4j.Logger;

import javax.swing.Icon;
import org.openide.windows.WindowManager;
import sk.stuba.fiit.kvasnicka.topologyvisual.filetype.gui.TopologyVisualisation;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.AddSimulationTopComponent;
import sk.stuba.fiit.kvasnicka.topologyvisual.resources.ImageResourceHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.resources.ImageType;
import sk.stuba.fiit.kvasnicka.topologyvisual.topology.Topology;

/**
 * Vertex Picked Listener used in Topology Creation mode
 *
 * @author Igor Kvasnicka
 */
public class VertexPickedTopolCreationListener implements ItemListener {

    private static Logger logg = Logger.getLogger(VertexPickedTopolCreationListener.class);
    private DefaultVertexIconTransformer<TopologyVertex> imager;
    private TopologyVisualisation topComponent;
    private PickedState<TopologyVertex> ps;

    public VertexPickedTopolCreationListener(DefaultVertexIconTransformer<TopologyVertex> imager, TopologyVisualisation topComponent, PickedState<TopologyVertex> ps) {
        this.imager = imager;
        this.topComponent = topComponent;
        this.ps = ps;
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        Object subject = e.getItem();
        // The graph uses Integers for vertices.
        if (subject instanceof TopologyVertex) {
            TopologyVertex vertex = (TopologyVertex) subject;
            if (ps.isPicked(vertex)) {
                //vertex is now selected
                vertexSelected(vertex);
            } else {
                //vertex is no longer selected
                vertexDeSelected(vertex);
            }
        }

    }

    /**
     * vertex is no longer selected
     *
     * @param vertex
     */
    private void vertexDeSelected(TopologyVertex vertex) {
        Icon icon = imager.transform(vertex);
        vertexDeSelected(vertex, icon);
    }

    /**
     * vertex is selected now
     *
     * @param v
     */
    private void vertexSelected(TopologyVertex v) {
        Icon icon = imager.transform(v);


        //new edge is being created
        if (topComponent.getSelectedAction() != null && VertexPickActionEnum.CREATING_EDGE == topComponent.getSelectedAction().getVertexPickActionEnum()) {
            v.setSelected(false);
            ((LayeredIcon) icon).setImage(ImageResourceHelper.loadCheckedImageVertexAsImage(v.getImageType()));
            topComponent.getTopologyElementCreator().vertexSelected(v);
            ps.pick(v, false);
        } else {//not selected - edge not creating (this is a first time user clickes on the vertex - it is a edge start)
            v.setSelected(true);
            selectVertex(((LayeredIcon) icon), v.getImageType(), true);
            topComponent.getVertexSelectionManager().addSelectedVertex(v);
        }
    }

//    /**
//     * well, this method really needs some refactoring...maybe next time :)
//     */
//    public void graphClicked(TopologyVertex v, MouseEvent me) {
//        //this listener is active only when crating topology
//        //this is a workaround for JUNG, because it do not provide removal of listeners... very odd, indeed
//        if (Topology.TopologyModeEnum.CREATION != topComponent.getTopology().getTopologyMode()) {
//            return;
//        }
//        logg.debug("vertex picked");
//        Icon icon = imager.transform(v);
//        if (icon != null && icon instanceof LayeredIcon) {
//            logg.debug("som v ife - je to ikona");
//            if (v.isSelected()) {
//            } else {//not selected
//            }
//        }
//
//    }
    /**
     * de select vertex
     *
     * @param v
     * @param icon
     */
    private void vertexDeSelected(TopologyVertex v, Icon icon) {
        if (topComponent.getSelectedAction() == null || VertexPickActionEnum.CREATING_EDGE != topComponent.getSelectedAction().getVertexPickActionEnum()) {//edge not creating
            v.setSelected(false);
            selectVertex(((LayeredIcon) icon), v.getImageType(), false);
            topComponent.getVertexSelectionManager().removeSelectedVertex(v);
        }
    }

    /**
     * make vertex selected or de selected
     *
     * @param icon icon representing a vertex
     * @param imageType image type of a vertex
     * @param select true if vertex should be marked as selected; false
     * otherwise
     */
    private void selectVertex(LayeredIcon icon, ImageType imageType, boolean select) {
        icon.setImage(ImageResourceHelper.loadImageVertexAsImage(imageType, select));
    }
}
