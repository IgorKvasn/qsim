/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.graph.utils;

import edu.uci.ics.jung.visualization.LayeredIcon;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import edu.uci.ics.jung.visualization.decorators.DefaultVertexIconTransformer;
import edu.uci.ics.jung.visualization.picking.PickedState;
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
 * Vertex Picked Listener used when user is creating simulation rules mode
 *
 * @author Igor Kvasnicka
 */
public class VertexPickedSimulRulesListener implements GraphMouseListener<TopologyVertex> {

    private static Logger logg = Logger.getLogger(VertexPickedTopolCreationListener.class);
    private DefaultVertexIconTransformer<TopologyVertex> imager;
    private TopologyVisualisation topComponent;
    private PickedState<TopologyVertex> ps;
    private AddSimulationTopComponent component;

    public VertexPickedSimulRulesListener(DefaultVertexIconTransformer<TopologyVertex> imager, TopologyVisualisation topComponent, PickedState<TopologyVertex> ps) {
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
        //this listener is active only when defining simulation rules
        //this is a workaround for JUNG, because it do not provide removal of listeners... very odd, indeed
        if (Topology.TopologyModeEnum.SIMULATION_RULES != topComponent.getTopology().getTopologyMode()) {
            return;
        }
        logg.debug("vertex picked");
        Icon icon = imager.transform(v);
        if (icon != null && icon instanceof LayeredIcon) {
            topComponent.simulationRulesVertexPicked(v);
        }
    }

    /**
     * vertex is selected
     *
     * @param v
     * @param icon
     */
    private void vertexSelected(TopologyVertex v, Icon icon) {
        //is selected
        if (VertexPickActionEnum.CREATING_EDGE != topComponent.getSelectedAction().getVertexPickActionEnum()) {//edge not creating
            v.setSelected(false);
            selectVertex(((LayeredIcon) icon), v.getImageType(), false);
            topComponent.getVertexSelectionManager().removeSelectedVertex(v);
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