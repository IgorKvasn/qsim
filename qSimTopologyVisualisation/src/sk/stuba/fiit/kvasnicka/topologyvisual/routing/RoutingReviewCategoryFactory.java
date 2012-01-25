/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.routing;

import sk.stuba.fiit.kvasnicka.topologyvisual.lookuputils.RouteChanged;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComboBox;
import org.apache.log4j.Logger;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import sk.stuba.fiit.kvasnicka.topologyvisual.PreferenciesHelper;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.topologyvisual.dialogs.ConfirmDialogPanel;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.NetbeansWindowHelper;

/**
 *
 * @author Igor Kvasnicka
 */
public class RoutingReviewCategoryFactory extends ChildFactory<TopologyVertex> implements LookupListener, ActionListener {

    private static Logger logg = Logger.getLogger(RoutingReviewCategoryFactory.class);
    private Result<TopologyVertex> result;
    private Result<RouteChanged> resultRoute;
    private JComboBox combo;
    private RoutingReviewChildrenFactory routingReviewChildrenFactory;
    /**
     * set of network nodes that user wants to show in routing review - see
     * CheckComboBox component in RoutingTopComponent class
     */
    private Set<Class> allowedNetworkNodes = new HashSet<Class>();

    /**
     * this window is showing either next hop or final destination depending on
     * argument passed to this constructor
     *
     * @param showNextHop
     */
    public RoutingReviewCategoryFactory(final JComboBox combo) {
        result = Utilities.actionsGlobalContext().lookupResult(TopologyVertex.class);
        result.addLookupListener(this);

        resultRoute = Utilities.actionsGlobalContext().lookupResult(RouteChanged.class);
        resultRoute.addLookupListener(this);

        this.combo = combo;
        combo.addActionListener(this);


    }

    public void addAllowedNode(List<Class> node) {
        allowedNetworkNodes.addAll(node);
    }

    public void removeAllowedNode(List<Class> node) {
        allowedNetworkNodes.removeAll(node);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        refresh(true);
    }

    @Override
    protected boolean createKeys(List<TopologyVertex> toPopulate) {
        if (NetbeansWindowHelper.getInstance().getActiveTopology() == null) {
            return true;
        }
        for (TopologyVertex v : NetbeansWindowHelper.getInstance().getActiveTopology().getVertexFactory().getAllVertices()) {
            if (isNodeAllowed(v.getDataModel())) {
                toPopulate.add(v);
            }
        }
        return true;
    }

    @Override
    protected Node createNodeForKey(TopologyVertex key) {
        routingReviewChildrenFactory = new RoutingReviewChildrenFactory(combo, key);
        AbstractNode node = new AbstractNode(Children.create(routingReviewChildrenFactory, true));
        node.setDisplayName(NbBundle.getMessage(RoutingReviewCategoryFactory.class, "source") + " " + key.getName());
        return node;
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        refresh(true);
        if (routingReviewChildrenFactory != null) {
            routingReviewChildrenFactory.refresh();
        }
    }

    /**
     *
     * @param dataModel
     * @return
     */
    private boolean isNodeAllowed(NetworkNode node) {
        for (Class c : allowedNetworkNodes) {
            if (c.isInstance(node)) {
                return true;
            }
        }
        return false;
    }

    private class RoutingReviewChildrenFactory extends ChildFactory<String> implements LookupListener, ActionListener {

        private TopologyVertex vertex;
        private JComboBox combo;
        private Result<RouteChanged> resultRoute;

        public RoutingReviewChildrenFactory(final JComboBox combo, TopologyVertex vertex) {
            this.vertex = vertex;
            this.combo = combo;
            combo.addActionListener(this);
            resultRoute = Utilities.actionsGlobalContext().lookupResult(RouteChanged.class);
            resultRoute.addLookupListener(this);
        }

        public void refresh() {
            refresh(true);
        }

        @Override
        protected boolean createKeys(List<String> toPopulate) {
            if (combo.getSelectedIndex() == 0) {
                for (NetworkNode node : RoutingHelper.getAvailableNextHops(vertex.getDataModel())) {
                    if (isNodeAllowed(node)) {
                        toPopulate.add(NbBundle.getMessage(RoutingReviewCategoryFactory.class, "nextHop") + " " + node.getName());
                    }
                }
            } else {
                for (NetworkNode node : RoutingHelper.getAvailableDestinations(vertex.getDataModel())) {
                    if (isNodeAllowed(node)) {
                        toPopulate.add(NbBundle.getMessage(RoutingReviewCategoryFactory.class, "destination") + " " + node.getName());
                    }
                }
            }
            return true;
        }

        @Override
        protected Node createNodeForKey(String key) {
            ChildNode lookupKey = new ChildNode(vertex, key.substring((NbBundle.getMessage(RoutingReviewCategoryFactory.class, "destination") + " ").length()));
            AbstractNode node = new AbstractNode(Children.LEAF, Lookups.singleton(lookupKey)) {

                @Override
                public Action[] getActions(boolean context) {
                    if (combo.getSelectedIndex() == 1) { //only when destinations are showing
                        AbstractAction deleteAction = new AbstractAction(NbBundle.getMessage(RoutingReviewCategoryFactory.class, "delete_route")) {

                            @Override
                            public void actionPerformed(ActionEvent e) {
                                ChildNode selected = getLookup().lookup(ChildNode.class);
                                if (selected == null) {
                                    logg.warn("nothing is selected, however actionPerformed() was called - something is wring with lookups and selection management");
                                    return;
                                }
                                if (!checkDirectlyConnected(selected)) {
                                    return;
                                }
                                boolean result = RoutingHelper.deleteRoute(selected.category, selected.childName);
                                if (result) {//route was successfully deleted
                                    refresh();//refresh children in BeanTreeView component
                                }
                            }
                        };
                        return new Action[]{deleteAction};
                    } else {
                        return super.getActions(context);
                    }
                }
            };

            node.setDisplayName(key);
            return node;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            refresh(true);
        }

        @Override
        public void resultChanged(LookupEvent ev) {
            refresh();
        }

        /**
         * returns true when user really wants to delete directly connected
         * route
         */
        private boolean checkDirectlyConnected(ChildNode selected) {
            boolean directly = RoutingHelper.isDirectlyConnected(selected.category, selected.childName);
            if (directly) {
                ConfirmDialogPanel panel = new ConfirmDialogPanel(NbBundle.getMessage(RoutingReviewCategoryFactory.class, "edge_delete_directly_question"));
                NotifyDescriptor descriptor = new NotifyDescriptor(
                        panel, // instance of your panel
                        NbBundle.getMessage(RoutingReviewCategoryFactory.class, "delete_confirm_title"), // title of the dialog
                        NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.QUESTION_MESSAGE, null,
                        NotifyDescriptor.YES_OPTION // default option is "Yes"
                        );
                if (DialogDisplayer.getDefault().notify(descriptor) != NotifyDescriptor.YES_OPTION) {
                    return false;
                }
                if (panel.isNeverShow()) {
                    PreferenciesHelper.setNeverShowDirectlyConnectedDeleteConfirmation(panel.isNeverShow());
                }
            }
            return true;
        }
    }

    private static class ChildNode {

        private TopologyVertex category;
        private String childName;

        public ChildNode(TopologyVertex category, String childName) {
            this.category = category;
            this.childName = childName;
        }
    }
}
