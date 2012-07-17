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
id = "sk.stuba.fiit.kvasnicka.topologyvisual.actions.PauseSimulationAction")
@ActionRegistration(iconBase = "sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/pause_16.png",
displayName = "#CTL_PauseSimulationAction")
@ActionReferences({
    @ActionReference(path = "Toolbars/Simulation", position = 3433)
})
@Messages("CTL_PauseSimulationAction=Pause simulation")
public final class PauseSimulationAction extends AbstractAction implements Presenter.Toolbar {

    private static Logger logg = Logger.getLogger(RunSimulationAction.class);
    private JButton button = new JButton();
    private static PauseSimulationAction INSTANCE;

    public PauseSimulationAction() {
        button.setEnabled(false);
        button.setIcon(ImageResourceHelper.loadImage("/sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/pause_16.png"));
        button.addActionListener(this);
        INSTANCE = this;
    }

    public static PauseSimulationAction getInstance() {
        return INSTANCE;
    }

    public void actionPerformed(ActionEvent e) {
        System.out.println("stlacil som pause " + (NetbeansWindowHelper.getInstance().getActiveTopologyVisualisation() != null));

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

        if (state.isButtonEnabled(ButtonEnum.PAUSE)) {
            button.setEnabled(true);
        }
    }
}
