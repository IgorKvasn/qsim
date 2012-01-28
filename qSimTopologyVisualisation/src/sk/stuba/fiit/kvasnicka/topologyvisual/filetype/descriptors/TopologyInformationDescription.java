/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
