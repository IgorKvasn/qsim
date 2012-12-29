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
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import org.apache.log4j.Logger;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import sk.stuba.fiit.kvasnicka.topologyvisual.PreferenciesHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.events.topologystate.TopologyStateChangedEvent;
import sk.stuba.fiit.kvasnicka.topologyvisual.events.topologystate.TopologyStateChangedListener;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.edges.TopologyEdge;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.NetbeansWindowHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.ConfirmDialogPanel;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.deletion.EdgeDeletionDialog;
import sk.stuba.fiit.kvasnicka.topologyvisual.topology.TopologyStateEnum;
import sk.stuba.fiit.kvasnicka.topologyvisual.topology.Topology;
import sk.stuba.fiit.kvasnicka.topologyvisual.utils.SimulationData;
import sk.stuba.fiit.kvasnicka.topologyvisual.utils.SimulationData.Data;

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
    private JMenuItem menuItemEditVertex;
    private JMenuItem menuItemEditEdge;

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
                correctVertexPopup();
                vertexPopup.show(vv, e.getX(), e.getY());
                //right clicking on a vertex makes it selected/picked
                topology.deselectEdges();
//                topology.deselectVertices();

                topology.manuallySelectVertex(v, true);
            } else {
                final TopologyEdge edge = pickSupport.getEdge(vv.getGraphLayout(), p.getX(), p.getY());
                if (edge != null) {
                    logg.debug("Edge " + edge + " was right clicked");
                    selectedEdge = edge;
                    correctEdgePopup();
                    edgePopup.show(vv, e.getX(), e.getY());
                    //right clicking on a edge makes it selected/picked
//                    topology.deselectEdges();
                    topology.deselectVertices();

                    topology.manuallySelectEdge(edge, true);
                }
            }
        }
    }

    /**
     * when simulation running, user cannot delete vertex and instead of "Edit"
     * label, "View" label is shown (the same menu item)
     */
    private void correctVertexPopup() {
        if (topology.getTopolElementTopComponent().isSimulationRunning()) {
            menuItemDeleteVertex.setEnabled(false);
            menuItemEditVertex.setText(NbBundle.getMessage(PopupVertexEdgeMenuMousePlugin.class, "view"));
        } else {
            menuItemDeleteVertex.setEnabled(true);
            menuItemEditVertex.setText(NbBundle.getMessage(PopupVertexEdgeMenuMousePlugin.class, "edit"));
        }
    }

    private void correctEdgePopup() {
        if (topology.getTopolElementTopComponent().isSimulationRunning()) {
            menuItemDeleteEdge.setEnabled(false);
            menuItemEditEdge.setText(NbBundle.getMessage(PopupVertexEdgeMenuMousePlugin.class, "view"));
        } else {
            menuItemDeleteEdge.setEnabled(true);
            menuItemEditEdge.setText(NbBundle.getMessage(PopupVertexEdgeMenuMousePlugin.class, "edit"));
        }

    }

    private void createVertexPopup() {
        vertexPopup = new JPopupMenu();

        menuItemEditVertex = new JMenuItem(NbBundle.getMessage(PopupVertexEdgeMenuMousePlugin.class, "edit"));
        menuItemEditVertex.setEnabled(true);

        menuItemDeleteVertex = new JMenuItem(NbBundle.getMessage(PopupVertexEdgeMenuMousePlugin.class, "delete"));
        menuItemDeleteVertex.setEnabled(true);

        vertexPopup.add(menuItemDeleteVertex);
        vertexPopup.add(menuItemEditVertex);

        JMenuItem menuSimulLog = new JMenuItem(NbBundle.getMessage(PopupVertexEdgeMenuMousePlugin.class, "simul_log"));
        menuSimulLog.addActionListener(new ShowSimulationLogsMenuItem());
        vertexPopup.add(menuSimulLog);

        JMenuItem menuCopy = new JMenuItem(NbBundle.getMessage(PopupVertexEdgeMenuMousePlugin.class, "copy"));
        menuCopy.addActionListener(new CopyVertexListener());
        vertexPopup.add(menuCopy);

        menuItemDeleteVertex.addActionListener(new VertexDeleteMenuItem());
        menuItemEditVertex.addActionListener(new VertexEditMenuItem());
    }

    private void createEdgePopup() {
        edgePopup = new JPopupMenu();

        menuItemEditEdge = new JMenuItem(NbBundle.getMessage(PopupVertexEdgeMenuMousePlugin.class, "edit"));
        menuItemEditEdge.addActionListener(new EdgeEditMenuItem());
        edgePopup.add(menuItemEditEdge);


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

    private class EdgeEditMenuItem implements ActionListener {

        public EdgeEditMenuItem() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (NetbeansWindowHelper.getInstance().getActiveTopologyVisualisation() == null) {
                return;
            }

            topology.getTopolElementTopComponent().showEdgeEditDialog(selectedEdge);
        }
    }

    private class VertexEditMenuItem implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (NetbeansWindowHelper.getInstance().getActiveTopology() == null) {
                return;
            }

            if (topology.getSelectedVertices().size() > 1) {
                JOptionPane.showMessageDialog(NetbeansWindowHelper.getInstance().getActiveTopologyVisualisation(),
                        "You can edit only one vertex at a time.",
                        "Editing error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            NetbeansWindowHelper.getInstance().getActiveTopologyVisualisation().editSelectedVertex();
        }
    }

    private class VertexDeleteMenuItem implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (NetbeansWindowHelper.getInstance().getActiveTopology() == null) {
                return;
            }

            Collection<TopologyVertex> toDelete;
            if (topology.getSelectedVertices().isEmpty()) {//there may be some selected vertices or user just right-clicks on the vertex
                toDelete = Arrays.asList(selectedVertex);
            } else {
                toDelete = topology.getSelectedVertices();
            }

            topology.getTopolElementTopComponent().deleteVerticesWithDialog(toDelete);

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

        @Override
        public void actionPerformed(ActionEvent e) {
            if (topology.getSelectedVertices().isEmpty()) {
                topology.getTopolElementTopComponent().openSimulationLogTopcomponent(Arrays.asList(selectedVertex));
            } else {
                topology.getTopolElementTopComponent().openSimulationLogTopcomponent(topology.getSelectedVertices());
            }
        }
    }

    private class CopyVertexListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            topology.getTopolElementTopComponent().performVertexCopyFromTopology();
        }
    }
}
