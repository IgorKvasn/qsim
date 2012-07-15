/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.graph.utils;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractPopupGraphMousePlugin;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.*;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.apache.log4j.Logger;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;
import sk.stuba.fiit.kvasnicka.topologyvisual.PreferenciesHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.ConfirmDialogPanel;
import sk.stuba.fiit.kvasnicka.topologyvisual.filetype.gui.TopologyVisualisation;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.edges.TopologyEdge;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.NetbeansWindowHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.deletion.VertexDeletionDialog;
import sk.stuba.fiit.kvasnicka.topologyvisual.topology.Topology;
import sk.stuba.fiit.kvasnicka.topologyvisual.utils.SimulationData;
import sk.stuba.fiit.kvasnicka.topologyvisual.utils.SimulationData.Data;
import sk.stuba.fiit.kvasnicka.topologyvisual.utils.VerticesUtil;

/**
 *
 * @author Igor Kvasnicka
 */
public class PopupVertexEdgeMenuMousePlugin extends AbstractPopupGraphMousePlugin {

    private static Logger logg = Logger.getLogger(PopupVertexEdgeMenuMousePlugin.class);
    private JPopupMenu vertexPopup, edgePopup;
    private TopologyVertex selectedVertex = null;
    private TopologyEdge selectedEdge = null;
    private Topology topology;

    /**
     * Creates a new instance of PopupVertexEdgeMenuMousePlugin
     */
    public PopupVertexEdgeMenuMousePlugin(Topology topology) {
        this(topology, MouseEvent.BUTTON3_MASK);
    }

    /**
     * Creates a new instance of PopupVertexEdgeMenuMousePlugin
     *
     * @param modifiers mouse event modifiers see the jung visualization Event
     * class.
     */
    public PopupVertexEdgeMenuMousePlugin(Topology topology, int modifiers) {
        super(modifiers);
        createVertexPopup();
        createEdgePopup();
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
                vertexPopup.show(vv, e.getX(), e.getY());
            } else {
                final TopologyEdge edge = pickSupport.getEdge(vv.getGraphLayout(), p.getX(), p.getY());
                if (edge != null) {
                    logg.debug("Edge " + edge + " was right clicked");
                    selectedEdge = edge;
                    edgePopup.show(vv, e.getX(), e.getY());
                }
            }
        }
    }

    private void createVertexPopup() {
        vertexPopup = new JPopupMenu();
        vertexPopup.add(new JMenuItem(NbBundle.getMessage(PopupVertexEdgeMenuMousePlugin.class, "properties")));
        vertexPopup.addSeparator();
        vertexPopup.addSeparator();
        JMenuItem menuItemDelete = new JMenuItem(NbBundle.getMessage(PopupVertexEdgeMenuMousePlugin.class, "delete"));
        menuItemDelete.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (NetbeansWindowHelper.getInstance().getActiveTopology() == null) {
                    return;
                }

                Map<TopologyVertex, List<SimulationData.Data>> affectedSimrules;
                if (topology.getSelectedVertices().isEmpty()) {//there may be some selected vertices or user just right-clicks on the vertex
                    affectedSimrules = getAffectedSimrules(Arrays.asList(selectedVertex));
                } else {
                    affectedSimrules = getAffectedSimrules(topology.getSelectedVertices());
                }

                if (affectedSimrules.isEmpty()) {//there are no affected simulation rules
                    if (!PreferenciesHelper.isNeverShowVertexDeleteConfirmation()) {
                        ConfirmDialogPanel panel = new ConfirmDialogPanel(NbBundle.getMessage(PopupVertexEdgeMenuMousePlugin.class, "vertex_delete_question") + " " + VerticesUtil.getVerticesNames(topology.getSelectedVertices()));
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
                } else {//some simulation rules depend on this vertex

                    VertexDeletionDialog dialog = new VertexDeletionDialog(affectedSimrules);
                    dialog.setVisible(true);

                    if (dialog.getReturnCode() == VertexDeletionDialog.ReturnCode.CANCEL) {
                        return;
                    }
                }

                if (topology.getSelectedVertices().isEmpty()) {//user right clicks on the vertex - this does not selects vertex
                    topology.deleteVertex(selectedVertex);
                } else {
                    topology.deleteVertex(topology.getSelectedVertices());
                }

                logg.debug("vertex deletion: " + VerticesUtil.getVerticesNames(topology.getSelectedVertices()));
            }

            private Map<TopologyVertex, List<SimulationData.Data>> getAffectedSimrules(Collection<TopologyVertex> selectedVertices) {
                Map<TopologyVertex, List<SimulationData.Data>> affectedRules = new HashMap<TopologyVertex, List<SimulationData.Data>>();
                for (TopologyVertex v : selectedVertices) {
                    List<Data> simulRulesThatContainsNode = topology.getTopolElementTopComponent().getSimulationData().getSimulationDataContainingVertex(v);
                    affectedRules.put(v, simulRulesThatContainsNode);
                }
                return affectedRules;
            }
        });
        vertexPopup.add(menuItemDelete);

    }

    private void createEdgePopup() {
        edgePopup = new JPopupMenu();
        edgePopup.add(new JMenuItem(NbBundle.getMessage(PopupVertexEdgeMenuMousePlugin.class, "properties")));
        edgePopup.addSeparator();
        JMenuItem menuItemDelete = new JMenuItem(NbBundle.getMessage(PopupVertexEdgeMenuMousePlugin.class, "delete"));
        menuItemDelete.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (NetbeansWindowHelper.getInstance().getActiveTopology() == null) {
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
                //passing information about route deletion to TopologyVisualisation
                TopologyVisualisation topolComponent = NetbeansWindowHelper.getInstance().getActiveTopologyVisualisation();
                topolComponent.routesChanged();
            }
        });

        edgePopup.add(menuItemDelete);
    }
}
