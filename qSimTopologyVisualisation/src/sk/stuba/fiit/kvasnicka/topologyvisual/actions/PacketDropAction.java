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
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.Presenter;
import sk.stuba.fiit.kvasnicka.topologyvisual.actions.buttons.ButtonEnum;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.NetbeansWindowHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.resources.ImageResourceHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.topology.TopologyStateEnum;

@ActionID(
        category = "Simulation",
        id = "sk.stuba.fiit.kvasnicka.topologyvisual.actions.PacketDropAction")
@ActionRegistration(
        displayName = "#CTL_PacketDropAction", lazy = false)
@ActionReference(path = "Toolbars/Simulation", position = 3633)
@Messages("CTL_PacketDropAction=Packet drop rate")
public final class PacketDropAction extends AbstractAction implements Presenter.Toolbar {

    private JButton button = new JButton();
    private static PacketDropAction INSTANCE;

    public PacketDropAction() {
        button.setEnabled(false);
        button.setIcon(ImageResourceHelper.loadImage("/sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/packet_drop.png"));
        button.addActionListener(this);
        INSTANCE = this;
    }

    public static PacketDropAction getInstance() {
        return INSTANCE;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        NetbeansWindowHelper.getInstance().getActiveTopologyVisualisation().openDropTopComponent();
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

        if (state.isButtonEnabled(ButtonEnum.DROP_RATE)) {
            button.setEnabled(true);
        }
    }
}
