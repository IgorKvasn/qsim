/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
