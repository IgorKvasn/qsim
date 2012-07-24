/*
 * This file is part of qSim.
 *
 * qSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * qSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with qSim.  If not, see <http://www.gnu.org/licenses/>.
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
import sk.stuba.fiit.kvasnicka.topologyvisual.topology.Topology;

/**
 * Vertex Picked Listener used when user is creating simulation rules mode
 *
 * @author Igor Kvasnicka
 */
public class VertexPickedSimulRulesListener implements GraphMouseListener<TopologyVertex> {

    private static Logger logg = Logger.getLogger(VertexPickedSimulRulesListener.class);
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

//    private void selectVertex(LayeredIcon icon, ImageType imageType, boolean select) {
//        icon.setImage(ImageResourceHelper.loadImageVertexAsImage(imageType, select));
//    }

    @Override
    public void graphPressed(TopologyVertex v, MouseEvent me) {
    }

    @Override
    public void graphReleased(TopologyVertex v, MouseEvent me) {
    }
}