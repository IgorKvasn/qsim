/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.graph.utils;

import sk.stuba.fiit.kvasnicka.topologyvisual.routing.RoutingHelper;
import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractPopupGraphMousePlugin;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import org.apache.log4j.Logger;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;
import sk.stuba.fiit.kvasnicka.topologyvisual.PreferenciesHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.topology.TopologyCreation;
import sk.stuba.fiit.kvasnicka.topologyvisual.TopologyState;
import sk.stuba.fiit.kvasnicka.topologyvisual.exceptions.RoutingException;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.edges.TopologyEdge;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.routing.gui.RoutingTopComponent;
import sk.stuba.fiit.kvasnicka.topologyvisual.dialogs.ConfirmDialogPanel;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.NetbeansWindowHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.palette.gui.TopolElementTopComponent;

/**
 *
 * @author Igor Kvasnicka
 */
public class PopupVertexEdgeMenuMousePlugin extends AbstractPopupGraphMousePlugin {

    private static Logger logg = Logger.getLogger(PopupVertexEdgeMenuMousePlugin.class);
    private EdgePopupCollection edgePopup;
    private VertexPopupCollection vertexPopup;
    private RoutingHelper routingHelper;
    private TopologyVertex selectedVertex = null;
    private TopologyEdge selectedEdge = null;
    private TopologyCreation topology;

    /**
     * Creates a new instance of PopupVertexEdgeMenuMousePlugin
     */
    public PopupVertexEdgeMenuMousePlugin(TopologyCreation topology) {
        this(topology, MouseEvent.BUTTON3_MASK);
    }

    /**
     * Creates a new instance of PopupVertexEdgeMenuMousePlugin
     *
     * @param modifiers mouse event modifiers see the jung visualization Event
     * class.
     */
    public PopupVertexEdgeMenuMousePlugin(TopologyCreation topology, int modifiers) {
        super(modifiers);
        vertexPopup = new VertexPopupCollection();
        edgePopup = new EdgePopupCollection();
        routingHelper = new RoutingHelper();
        this.topology = topology;
    }

    /**
     * Implementation of the AbstractPopupGraphMousePlugin method. This is where
     * the work gets done. You shouldn't have to modify unless you really want
     * to...
     *
     * @param e
     */
    @Override
    protected void handlePopup(MouseEvent e) {
        final VisualizationViewer<TopologyVertex, TopologyEdge> vv =
                (VisualizationViewer<TopologyVertex, TopologyEdge>) e.getSource();
        Point2D p = e.getPoint();

        GraphElementAccessor<TopologyVertex, TopologyEdge> pickSupport = vv.getPickSupport();
        if (pickSupport != null) {
            final TopologyVertex v = pickSupport.getVertex(vv.getGraphLayout(), p.getX(), p.getY());
            if (v != null) {
                logg.debug("Vertex " + v + " was right clicked");
                selectedVertex = v;
                getVertexPopup().show(vv, e.getX(), e.getY());
            } else {
                final TopologyEdge edge = pickSupport.getEdge(vv.getGraphLayout(), p.getX(), p.getY());
                if (edge != null) {
                    logg.debug("Edge " + edge + " was right clicked");
                    selectedEdge = edge;
                    getEdgePopup().show(vv, e.getX(), e.getY());

                }
            }
        }
    }

    /**
     * Getter for the edge popup.
     *
     * @return
     */
    public JPopupMenu getEdgePopup() {
        switch (TopologyState.getTopologyState()) {
            case SIMULATION:
                return edgePopup.simulation;
            default:
                return edgePopup.normal;
        }

    }

    /**
     * Getter for the vertex popup.
     *
     * @return
     */
    public JPopupMenu getVertexPopup() {
        switch (TopologyState.getTopologyState()) {
            case SIMULATION:
                return vertexPopup.simulation;
            default:
                return vertexPopup.normal;
        }
    }

    private class VertexPopupCollection {

        private JPopupMenu normal, simulation, routeEditing;

        public VertexPopupCollection() {
            simulation = new JPopupMenu();
            simulation.add(new JMenuItem(NbBundle.getMessage(PopupVertexEdgeMenuMousePlugin.class, "properties")));
            createNormalPopup();
        }

        private void createNormalPopup() {
            normal = new JPopupMenu();
            normal.add(new JMenuItem(NbBundle.getMessage(PopupVertexEdgeMenuMousePlugin.class, "properties")));
            normal.addSeparator();
            normal.addSeparator();
            JMenuItem menuItemDelete = new JMenuItem(NbBundle.getMessage(PopupVertexEdgeMenuMousePlugin.class, "delete"));
            menuItemDelete.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (NetbeansWindowHelper.getInstance().getActiveTopComponentTopology() == null) {
                        return;
                    }
                    if (!PreferenciesHelper.isNeverShowVertexDeleteConfirmation()) {
                        ConfirmDialogPanel panel = new ConfirmDialogPanel(NbBundle.getMessage(PopupVertexEdgeMenuMousePlugin.class, "vertex_delete_question") + " " + selectedVertex.getName());
                        NotifyDescriptor descriptor = new NotifyDescriptor(
                                panel, // instance of your panel
                                NbBundle.getMessage(PopupVertexEdgeMenuMousePlugin.class, "delete_confirm_title"), // title of the dialog
                                NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.QUESTION_MESSAGE, null,
                                NotifyDescriptor.YES_OPTION // default option is "Yes"
                                );

                        if (DialogDisplayer.getDefault().notify(descriptor) != NotifyDescriptor.YES_OPTION) {
                            return;
                        }
                        if (panel.isNeverShow()) {
                            PreferenciesHelper.setNeverShowVertexDeleteConfirmation(panel.isNeverShow());
                        }
                    }
                    topology.deleteVertex(selectedVertex);
                    routingHelper.recalculateRoutes(NetbeansWindowHelper.getInstance().getActiveTopComponentTopology());
                    logg.debug("vertex: " + selectedVertex + " was deleted");
                    //passing information about route deletion to TopolElementTopComponent                    
                    TopolElementTopComponent topolComponent = NetbeansWindowHelper.getInstance().getActiveTopoloElementTopComp();
                    topolComponent.routesChanged();
                }
            });
            normal.add(menuItemDelete);

        }
    }

    private class EdgePopupCollection {

        private JPopupMenu normal, simulation;

        public EdgePopupCollection() {
            normal = new JPopupMenu();
            normal.add(new JMenuItem(NbBundle.getMessage(PopupVertexEdgeMenuMousePlugin.class, "properties")));
            normal.addSeparator();
            JMenuItem menuItemDelete = new JMenuItem(NbBundle.getMessage(PopupVertexEdgeMenuMousePlugin.class, "delete"));
            menuItemDelete.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (NetbeansWindowHelper.getInstance().getActiveTopComponentTopology() == null) {
                        return;
                    }
                    if (!PreferenciesHelper.isNeverShowEdgeDeleteConfirmation()) {
                        ConfirmDialogPanel panel = new ConfirmDialogPanel(NbBundle.getMessage(PopupVertexEdgeMenuMousePlugin.class, "edge_delete_question") + " " + selectedEdge.getVertex1().getName() + " " + NbBundle.getMessage(PopupVertexEdgeMenuMousePlugin.class, "and") + " " + selectedEdge.getVertex2().getName());
                        NotifyDescriptor descriptor = new NotifyDescriptor(
                                panel, // instance of your panel
                                NbBundle.getMessage(PopupVertexEdgeMenuMousePlugin.class, "delete_confirm_title"), // title of the dialog
                                NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.QUESTION_MESSAGE, null,
                                NotifyDescriptor.YES_OPTION // default option is "Yes"
                                );

                        if (DialogDisplayer.getDefault().notify(descriptor) != NotifyDescriptor.YES_OPTION) {
                            return;
                        }
                        if (panel.isNeverShow()) {
                            PreferenciesHelper.setNeverShowEdgeDeleteConfirmation(panel.isNeverShow());
                        }
                    }

                    topology.deleteEdge(selectedEdge);
                    logg.debug("edge: " + selectedEdge + " was deleted");
                    //passing information about route deletion to TopolElementTopComponent
                    TopolElementTopComponent topolComponent = NetbeansWindowHelper.getInstance().getActiveTopoloElementTopComp();
                    topolComponent.routesChanged();
                }
            });

            normal.add(menuItemDelete);

            simulation = new JPopupMenu();
            simulation.add(new JMenuItem(NbBundle.getMessage(PopupVertexEdgeMenuMousePlugin.class, "properties")));

        }
    }
}
