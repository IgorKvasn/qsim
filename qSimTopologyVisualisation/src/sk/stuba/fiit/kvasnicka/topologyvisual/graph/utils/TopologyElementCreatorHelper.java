package sk.stuba.fiit.kvasnicka.topologyvisual.graph.utils;

import org.apache.log4j.Logger;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.topologyvisual.filetype.gui.TopologyVisualisation;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.commons.TopologyElementFactory;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.edges.TopologyEdge;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.NetbeansWindowHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.palette.PaletteActionEnum;
import sk.stuba.fiit.kvasnicka.topologyvisual.topology.Topology;

/**
 * handles all requests to create new graph element (vertex or edge)
 *
 * @author Igor Kvasnicka
 */
public class TopologyElementCreatorHelper {

    private static Logger logg = Logger.getLogger(TopologyElementCreatorHelper.class);
    private Topology topology;
    private TopologyVisualisation topolElementTopComponent;
    private PaletteActionEnum action;
    private TopologyVertex edgeStart;

    /**
     * creates new instance
     *
     * @param topology reference to Topology object
     */
    public TopologyElementCreatorHelper(Topology topology, TopologyVisualisation topolElementTopComponent) {
        this.topology = topology;
        this.topolElementTopComponent = topolElementTopComponent;
    }

    /**
     * cancels action to create new element
     */
    public void cancelAction() {
        action = null;
        topology.setPickingMode();
        edgeStart = null;
        topology.deselectVertices();
        topolElementTopComponent.deselectAction();
        topolElementTopComponent.paletteClearSelection();
        StatusDisplayer.getDefault().setStatusText("");
    }

    /**
     * gets current action - creating vertex or edge
     *
     * @return null if user does not select anything
     */
    public PaletteActionEnum getAction() {
        return action;
    }

    public void setAction(PaletteActionEnum action) {
        this.action = action;
    }

    /**
     * this method is called when user is creating new edge and he selects some
     * vertex
     *
     * @param vertex selected vertex
     * @return true if new edge was created
     */
    public boolean vertexSelected(TopologyVertex vertex) {
        if (edgeStart == null) {//this is starting vertex
            edgeStart = vertex;
            return false;
        } else { //ending vertex - time to create edge itself

            try {
                if (!checkEdgeAllowed(vertex, edgeStart)) {
                    return false;
                }

                if (topology.edgeExists(edgeStart, vertex)) {
                    NotifyDescriptor d = new NotifyDescriptor.Message(NbBundle.getMessage(TopologyElementCreatorHelper.class, "edge_between")
                            + " " + edgeStart.getName() + " "
                            + NbBundle.getMessage(TopologyElementCreatorHelper.class, "and") + " " + vertex.getName() + " "
                            + NbBundle.getMessage(TopologyElementCreatorHelper.class, "already_exists"),
                            NotifyDescriptor.ERROR_MESSAGE);

                    DialogDisplayer.getDefault().notify(d);
                    cancelAction();
                    topology.deselectVertices();
                    return false;
                }


                Edge edge = TopologyElementFactory.createEdge(action, topolElementTopComponent.getDialogHandler(), edgeStart.getDataModel(), vertex.getDataModel());
                logg.debug("creating edge from " + edgeStart.getName() + " to " + vertex.getName());
                topology.addEdge(edgeStart, vertex, new TopologyEdge(edge, edgeStart, vertex));
            } catch (IllegalStateException e) {
                //user hit cancel button when defining new edge parameters
                //simply do not create new edge
            } finally {        //if edge is created or user cancelles edge creation
                cancelAction();
                topology.deselectVertices();
            }
            return true;
        }
    }

    /**
     * check whether user can create this type of edge
     *
     * @param begin
     * @param end
     * @return
     */
    private boolean checkEdgeAllowed(TopologyVertex begin, TopologyVertex end) {

        if ((topology.isEdgeAllowed(begin, end)) != null) {
            NotifyDescriptor descriptor = new NotifyDescriptor.Message(NbBundle.getMessage(TopologyElementCreatorHelper.class, "edge_creation_forbidden"), NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(descriptor);
            return false;
        }
        return true;
    }
}
