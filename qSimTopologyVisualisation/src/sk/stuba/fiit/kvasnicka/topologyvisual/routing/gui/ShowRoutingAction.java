/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.routing.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import sk.stuba.fiit.kvasnicka.topologyvisual.TopologyState;
import sk.stuba.fiit.kvasnicka.topologyvisual.resources.ImageResourceHelper;

@ActionID(category = "Edit",
id = "sk.stuba.fiit.kvasnicka.topologyvisual.routing.gui.ShowRoutingAction")
@ActionRegistration(iconBase = "sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/topology.png",
displayName = "#CTL_ShowRoutingAction")
@ActionReferences({
    @ActionReference(path = "Toolbars/Topology", position = 3233)
})
public final class ShowRoutingAction extends AbstractAction implements Presenter.Toolbar {

    private JButton button;
    private Icon normalIcon = new ImageIcon(ImageResourceHelper.class.getResource("files/topology.png"));
    private Icon cancelIcon = new ImageIcon(ImageResourceHelper.class.getResource("files/topology_cancel.png"));

    public ShowRoutingAction() {

        button = new JButton(normalIcon);
        button.addActionListener(this);
        button.setToolTipText(NbBundle.getMessage(ShowRoutingAction.class, "CTL_ShowRoutingAction"));
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        if (TopologyState.State.ROUTE_EDITING.equals(TopologyState.getTopologyState())//the cancel button is shown
                || TopologyState.State.ROUTING_SOURCE_SELECTED.equals(TopologyState.getTopologyState())) {
            button.setIcon(normalIcon);
            button.setToolTipText(NbBundle.getMessage(ShowRoutingAction.class, "CTL_ShowRoutingAction"));
            TopologyState.setTopologyState(TopologyState.State.NORMAL);

            TopComponent tc = WindowManager.getDefault().findTopComponent("RoutingTopComponent");
            if (tc != null) {
                tc.close();
            }
        } else {
            button.setIcon(cancelIcon);
            button.setToolTipText(NbBundle.getMessage(ShowRoutingAction.class, "CTL_ShowRoutingAction_cancel"));

            TopologyState.setTopologyState(TopologyState.State.ROUTE_EDITING);
            TopComponent tc = WindowManager.getDefault().findTopComponent("RoutingTopComponent");
            if (tc != null) {
                tc.open();
                tc.requestActive();
            }

            TopComponent tc2 = WindowManager.getDefault().findTopComponent("RoutingTableTopComponent");
            if (tc2 != null) {
                tc2.open();
                 tc2.requestActive();
            }
        }
    }

    @Override
    public Component getToolbarPresenter() {
        return button;
    }
}
