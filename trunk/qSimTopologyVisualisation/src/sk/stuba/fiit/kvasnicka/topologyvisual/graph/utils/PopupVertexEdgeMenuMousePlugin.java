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
import org.openide.windows.Mode;
import org.openide.windows.WindowManager;
import sk.stuba.fiit.kvasnicka.topologyvisual.PreferenciesHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.events.topologystate.TopologyStateChangedEvent;
import sk.stuba.fiit.kvasnicka.topologyvisual.events.topologystate.TopologyStateChangedListener;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.edges.TopologyEdge;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.NetbeansWindowHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.ConfirmDialogPanel;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.deletion.EdgeDeletionDialog;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.deletion.VertexDeletionDialog;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.SimulationTopComponent;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.logs.SimulationLogTopComponent;
import sk.stuba.fiit.kvasnicka.topologyvisual.topology.TopologyStateEnum;
import sk.stuba.fiit.kvasnicka.topologyvisual.topology.Topology;
import sk.stuba.fiit.kvasnicka.topologyvisual.utils.SimulationData;
import sk.stuba.fiit.kvasnicka.topologyvisual.utils.SimulationData.Data;
import sk.stuba.fiit.kvasnicka.topologyvisual.utils.VerticesUtil;

/**
 *
 * @author Igor Kvasnicka
 */
public class PopupVertexEdgeMenuMousePlugin extends AbstractPopupGraphMousePlugin implements TopologyStateChangedListener {

    private static Logger logg = Logger.getLogger(PopupVertexEdgeMenuMousePlugin.class);
    private JPopupMenu vertexPopup, edgePopup;
    private TopologyVertex selectedVertex = null;
    private TopologyEdge selectedEdge = null;
    private Topology topology;
    private JMenuItem menuItemDeleteVertex;
    private JMenuItem menuItemDeleteEdge;

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
        topology.getTopolElementTopComponent().addTopologyStateChangedListener(this);
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
        menuItemDeleteVertex = new JMenuItem(NbBundle.getMessage(PopupVertexEdgeMenuMousePlugin.class, "delete"));
        menuItemDeleteVertex.setEnabled(true);

        vertexPopup.add(menuItemDeleteVertex);
        JMenuItem menuSimulLog = new JMenuItem(NbBundle.getMessage(PopupVertexEdgeMenuMousePlugin.class, "simul_log"));
        menuSimulLog.addActionListener(new ShowSimulationLogsMenuItem());
        vertexPopup.add(menuSimulLog);

        menuItemDeleteVertex.addActionListener(new VertexDeleteMenuItem());
    }

    private void createEdgePopup() {
        edgePopup = new JPopupMenu();
        edgePopup.add(new JMenuItem(NbBundle.getMessage(PopupVertexEdgeMenuMousePlugin.class, "properties")));
        edgePopup.addSeparator();
        menuItemDeleteEdge = new JMenuItem(NbBundle.getMessage(PopupVertexEdgeMenuMousePlugin.class, "delete"));
        menuItemDeleteEdge.addActionListener(new EdgeDeleteMenuItem());
        edgePopup.add(menuItemDeleteEdge);
    }

    @Override
    public void topologyStateChangeOccured(TopologyStateChangedEvent event) {
        if (!topology.getTopolElementTopComponent().getSimulationState().equals(TopologyStateEnum.NOTHING)) {
            menuItemDeleteVertex.setEnabled(false);
            menuItemDeleteEdge.setEnabled(false);
        } else {
            menuItemDeleteVertex.setEnabled(true);
            menuItemDeleteEdge.setEnabled(true);
        }
    }

    private class VertexDeleteMenuItem implements ActionListener {

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

                for (TopologyVertex v : affectedSimrules.keySet()) {
                    for (SimulationData.Data data : affectedSimrules.get(v)) {
                        if (VerticesUtil.isVertexSourceOrDestination(v, data)) {//topology vertex marked for removal is source or destination in some simulation rule
                            topology.getTopolElementTopComponent().getSimulationData().removeSimulationData(data.getId());
                        }
                    }
                }

               topology.getTopolElementTopComponent().reloadSimulationRuleData();
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
                if (!simulRulesThatContainsNode.isEmpty()) {
                    affectedRules.put(v, simulRulesThatContainsNode);
                }
            }
            return affectedRules;
        }
    }

    private class EdgeDeleteMenuItem implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (NetbeansWindowHelper.getInstance().getActiveTopology() == null) {
                return;
            }

            Map<TopologyEdge, List<SimulationData.Data>> affectedSimrules;
            if (topology.getSelectedVertices().isEmpty()) {//there may be some selected vertices or user just right-clicks on the vertex
                affectedSimrules = getAffectedSimrules(Arrays.asList(selectedEdge));
            } else {
                affectedSimrules = getAffectedSimrules(topology.getSelectedEdges());
            }

            if (affectedSimrules.isEmpty()) {//there are no affected simulation rules
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
            } else {//some simulation rules depend on this vertex

                EdgeDeletionDialog dialog = new EdgeDeletionDialog(affectedSimrules);
                dialog.setVisible(true);


                if (dialog.getReturnCode() == EdgeDeletionDialog.ReturnCode.CANCEL) {
                    return;
                }
            }

            if (topology.getSelectedEdges().isEmpty()) {//user right clicks on the vertex - this does not selects vertex
                topology.deleteEdge(selectedEdge);
            } else {
                topology.deleteEdge(topology.getSelectedEdges());
            }

            logg.debug("edge (edges)  deleted");
        }

        private Map<TopologyEdge, List<SimulationData.Data>> getAffectedSimrules(Collection<TopologyEdge> edges) {
            Map<TopologyEdge, List<SimulationData.Data>> affectedRules = new HashMap<TopologyEdge, List<SimulationData.Data>>();
            for (TopologyEdge e : edges) {
                List<Data> simulRulesThatContainsNode = topology.getTopolElementTopComponent().getSimulationData().getSimulationDataContainingEdge(e);
                if (!simulRulesThatContainsNode.isEmpty()) {
                    affectedRules.put(e, simulRulesThatContainsNode);
                }
            }
            return affectedRules;
        }
    }

    private class ShowSimulationLogsMenuItem implements ActionListener {

        private SimulationLogTopComponent logTopComponent = new SimulationLogTopComponent();

        @Override
        public void actionPerformed(ActionEvent e) {
            if (topology.getSelectedVertices().isEmpty()) {
                openSimulationLogTopcomponent(Arrays.asList(selectedVertex));
            } else {
                openSimulationLogTopcomponent(topology.getSelectedVertices());
            }
        }

        /**
         * opens new simulation log top component associated with this topology
         */
        private void openSimulationLogTopcomponent(Collection<TopologyVertex> vertices) {
            logTopComponent.setTopology(topology);
            logTopComponent.showVetices(vertices);
            Mode outputMode = WindowManager.getDefault().findMode("output");
            outputMode.dockInto(logTopComponent);
            logTopComponent.open();
            logTopComponent.requestActive();
        }
    }
}
