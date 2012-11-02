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
id = "sk.stuba.fiit.kvasnicka.topologyvisual.actions.copypaste.PasteVertexAction")
@ActionRegistration(lazy = false, displayName = "#CTL_PasteVertexAction")
@ActionReferences({
    @ActionReference(path = "Menu/Edit", position = 1050),
    @ActionReference(path = "Toolbars/EditSimulation", position = 3333),
    @ActionReference(path = "Shortcuts", name = "S-INSERT"),
    @ActionReference(path = "Shortcuts", name = "D-V")
})
@Messages("CTL_PasteVertexAction=Paste vertex")
public class PasteVertexAction extends AbstractAction implements Presenter.Toolbar {

    private static Logger logg = Logger.getLogger(PasteVertexAction.class);
    private JButton button = new JButton();
    private static PasteVertexAction INSTANCE;

    public PasteVertexAction() {

        button.setEnabled(false);
        button.setIcon(ImageResourceHelper.loadImage("/sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/paste_16.png"));
        button.addActionListener(this);
        INSTANCE = this;
    }

    public static PasteVertexAction getInstance() {
        return INSTANCE;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //user wants to paste a vertex
        NetbeansWindowHelper.getInstance().getActiveTopologyVisualisation().performVertexPaste();
    }

    @Override
    public Component getToolbarPresenter() {
        return button;
    }

    public void updateState(boolean enabled) {
        button.setEnabled(enabled);
    }
}