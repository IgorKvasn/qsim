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
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.RoutingSettings;
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
