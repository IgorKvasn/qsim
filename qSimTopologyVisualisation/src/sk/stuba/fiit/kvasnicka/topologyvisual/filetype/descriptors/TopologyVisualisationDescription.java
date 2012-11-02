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
package sk.stuba.fiit.kvasnicka.topologyvisual.filetype.descriptors;

import java.awt.Image;
import java.io.Serializable;
import lombok.Getter;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import sk.stuba.fiit.kvasnicka.topologyvisual.filetype.TopologyFileTypeDataObject;
import sk.stuba.fiit.kvasnicka.topologyvisual.filetype.gui.TopologyVisualisation;

/**
 *
 * @author Igor Kvasnicka
 */
@NbBundle.Messages({"display_name_topol_visual=Topology"})
public class TopologyVisualisationDescription implements MultiViewDescription, Serializable {

    private final TopologyFileTypeDataObject obj;
    @Getter
    private transient TopologyVisualisation topologyVisualisation;

    public TopologyVisualisationDescription(TopologyFileTypeDataObject obj) {
        this.obj = obj;
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(TopologyVisualisationDescription.class, "display_name_topol_visual");
    }

    @Override
    public Image getIcon() {
//        "sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/qsimFileType.png"
        return null;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    public String preferredID() {
        return "TopologyVisualisationDescription"; //when changing this, change it also in NetbeansWindowHelper - method getActiveTopologyVisualisation
    }

    @Override
    public MultiViewElement createElement() {       
        topologyVisualisation = new TopologyVisualisation(obj);
        return topologyVisualisation;
    }
}
