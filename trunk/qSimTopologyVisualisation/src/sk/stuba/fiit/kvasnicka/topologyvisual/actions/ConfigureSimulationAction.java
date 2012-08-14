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
package sk.stuba.fiit.kvasnicka.topologyvisual.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import org.apache.log4j.Logger;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.Presenter;
import sk.stuba.fiit.kvasnicka.topologyvisual.actions.buttons.ButtonEnum;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.NetbeansWindowHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.resources.ImageResourceHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.topology.TopologyStateEnum;

@ActionID(category = "Simulation",
id = "sk.stuba.fiit.kvasnicka.topologyvisual.actions.ConfigureSimulationAction")
@ActionRegistration(lazy = false,
displayName = "#CTL_ConfigureSimulationAction")
@ActionReferences({
    @ActionReference(path = "Toolbars/Simulation", position = 3433)
})
@Messages("CTL_ConfigureSimulationAction=ConfigureSimulation")
public final class ConfigureSimulationAction extends AbstractAction implements Presenter.Toolbar {

    private static Logger logg = Logger.getLogger(RunSimulationAction.class);
    private JButton button = new JButton();
    private static ConfigureSimulationAction INSTANCE;

    public ConfigureSimulationAction() {
        button.setEnabled(false);
        button.setIcon(ImageResourceHelper.loadImage("/sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/settings_16.png"));
        button.addActionListener(this);
        INSTANCE = this;
    }

    public static ConfigureSimulationAction getInstance() {
        return INSTANCE;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        NetbeansWindowHelper.getInstance().getActiveTopologyVisualisation().configureSimulation();
    }

    @Override
    public Component getToolbarPresenter() {
        return button;
    }

    public void updateState(TopologyStateEnum state) {
        if (state == null) {
            button.setEnabled(false);
            return;
        }
        button.setEnabled(false);

        if (state.isButtonEnabled(ButtonEnum.CONFIGURE)) {
            button.setEnabled(true);
        }
    }
}
