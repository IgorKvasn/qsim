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
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import sk.stuba.fiit.kvasnicka.topologyvisual.filetype.TopologyFileTypeDataObject;
import sk.stuba.fiit.kvasnicka.topologyvisual.filetype.gui.TopologyInformation;

/**
 *
 * @author Igor Kvasnicka
 */
@NbBundle.Messages({"display_name_topol_info=Info"})
public class TopologyInformationDescription implements MultiViewDescription, Serializable {

    private final TopologyFileTypeDataObject dataObject;

    public TopologyInformationDescription(TopologyFileTypeDataObject dataObject) {
        this.dataObject = dataObject;
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ONLY_OPENED;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(TopologyInformationDescription.class, "display_name_topol_info");
    }

    @Override
    public Image getIcon() {
        return null;
        // return ImageResourceHelper.loadImage("sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/qsimFileType.png").getImage();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    public String preferredID() {
        return "TopologyInfoMultiview";//when changing this, change it also in NetbeansWindowHelper - method getActiveTopologyVisualisation
    }

    @Override
    public MultiViewElement createElement() {
        return new TopologyInformation(dataObject);
    }
}
