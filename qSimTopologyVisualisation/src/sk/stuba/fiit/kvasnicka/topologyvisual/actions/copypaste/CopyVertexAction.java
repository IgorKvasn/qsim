/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.actions.copypaste;

import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import org.apache.log4j.Logger;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.Presenter;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.NetbeansWindowHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.resources.ImageResourceHelper;

@ActionID(
    category = "Edit",
id = "sk.stuba.fiit.kvasnicka.topologyvisual.actions.copypaste.CopyVertexAction")
@ActionRegistration(lazy = false, displayName = "#CTL_CopyVertexAction")
@ActionReferences({
    @ActionReference(path = "Menu/Edit", position = 900),
    @ActionReference(path = "Toolbars/EditSimulation", position = 3333),
    @ActionReference(path = "Shortcuts", name = "D-C")
})
@Messages("CTL_CopyVertexAction=Copy vertex")
public class CopyVertexAction extends AbstractAction implements Presenter.Toolbar {

    private static Logger logg = Logger.getLogger(CopyVertexAction.class);
    private JButton button = new JButton();
    private static CopyVertexAction INSTANCE;

    public CopyVertexAction() {

        button.setEnabled(false);
        button.setIcon(ImageResourceHelper.loadImage("/sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/copy_16.png"));
        button.addActionListener(this);
        INSTANCE = this;
    }

    public static CopyVertexAction getInstance() {
        return INSTANCE;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //user wants to copy a vertex
        NetbeansWindowHelper.getInstance().getActiveTopologyVisualisation().performVertexCopyFromTopology();
    }

    @Override
    public Component getToolbarPresenter() {
        return button;
    }

    public void updateState(boolean enabled) {
        button.setEnabled(enabled);
    }
}
