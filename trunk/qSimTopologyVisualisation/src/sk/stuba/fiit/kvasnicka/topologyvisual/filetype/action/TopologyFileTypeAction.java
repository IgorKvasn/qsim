/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.filetype.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;
import sk.stuba.fiit.kvasnicka.topologyvisual.dialogs.RoutingSettings;
import sk.stuba.fiit.kvasnicka.topologyvisual.filetype.TopologyFileTypeDataObject;

@ActionID(category = "File",
id = "sk.stuba.fiit.kvasnicka.topologyvisual.filetype.action.TopologyFileTypeAction")
@ActionRegistration(displayName = "#CTL_TopologyFileTypeAction")
@ActionReferences({
    @ActionReference(path = "Loaders/text/qsim/Actions", position = 1500, separatorBefore = 1450)
})
@Messages("CTL_TopologyFileTypeAction=Configure routing")
public final class TopologyFileTypeAction implements ActionListener {

    private final List<TopologyFileTypeDataObject> context;

    public TopologyFileTypeAction(List<TopologyFileTypeDataObject> context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        //open dialog
        new RoutingSettings(WindowManager.getDefault().getMainWindow(), context).setVisible(true);
    }
}
