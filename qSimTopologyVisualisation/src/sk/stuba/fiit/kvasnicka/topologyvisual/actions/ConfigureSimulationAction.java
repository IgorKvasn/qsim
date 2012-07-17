/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
import sk.stuba.fiit.kvasnicka.topologyvisual.topology.SimulationStateEnum;

@ActionID(category = "Simulation",
id = "sk.stuba.fiit.kvasnicka.topologyvisual.actions.ConfigureSimulationAction")
@ActionRegistration(iconBase = "sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/settings_16.png",
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

    public void updateState(SimulationStateEnum state) {
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
