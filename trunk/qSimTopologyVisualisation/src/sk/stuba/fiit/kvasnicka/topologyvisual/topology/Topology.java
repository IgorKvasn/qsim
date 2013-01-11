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
package sk.stuba.fiit.kvasnicka.topologyvisual.topology;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.AbstractGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.control.*;
import edu.uci.ics.jung.visualization.decorators.DefaultVertexIconTransformer;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.picking.ShapePickSupport;
import edu.uci.ics.jung.visualization.renderers.DefaultVertexLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import java.awt.*;
import java.util.List;
import java.awt.event.InputEvent;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.EventListenerList;
import lombok.Getter;
import org.apache.commons.collections15.Transformer;
import org.apache.log4j.Logger;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;
import sk.stuba.fiit.kvasnicka.topologyvisual.PreferenciesHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.exceptions.RoutingException;
import sk.stuba.fiit.kvasnicka.topologyvisual.filetype.gui.TopologyVisualisation;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.MyVisualizationViewer;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.edges.TopologyEdge;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.events.vertexcreated.VertexCreatedEvent;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.events.vertexcreated.VertexCreatedListener;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.events.vertexdeleted.VertexDeletedEvent;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.events.vertexdeleted.VertexDeletedListener;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.utils.*;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.utils.MyVertexIconShapeTransformer;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.utils.VertexToIconTransformer;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.NetbeansWindowHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.palette.TopologyPaletteTopComponent;
import sk.stuba.fiit.kvasnicka.topologyvisual.route.RoutingHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.serialisation.DeserialisationResult;

/**
 * This class is graphical representation of network topology it uses JUNG
 * library
 *
 * @author Igor Kvasnicka
 */
@NbBundle.Messages({"cycle_exception=No cycles are allowed in the route"})
public class Topology implements VertexCreatedListener {

    private static Logger logg = Logger.getLogger(Topology.class);
    private final TopologyModeEnum DEFAULT_MODE = TopologyModeEnum.CREATION;
    @Getter
    private AbstractGraph<TopologyVertex, TopologyEdge> g;
    @Getter
    private AbstractLayout<TopologyVertex, TopologyEdge> layout;
    private PopupVertexEdgeMenuMousePlugin popupVertexMenuMousePlugin;
    private MyVisualizationViewer vv;
    private MyGraphMouse graphMouse;
    @Getter
    private TopologyVertexFactory vertexFactory;
    private transient RoutingHelper routingHelper = new RoutingHelper();
    @Getter
    private final TopologyVisualisation topolElementTopComponent;
    @Getter
    private TopologyModeEnum topologyMode = null;
    private GraphMouseListener<TopologyVertex> vertexPickedListener;
    private DefaultModalGraphMouse defaultGm;
    private EventListenerList listenerList = new EventListenerList();
    @Getter
    /**
     * sometimes I need to know single selected vertex (so i do not care about
     * multiselect)
     */
    private TopologyVertex selectedSingleVertex = null;

    /**
     * creates new instance
     *
     * @param mainFrame instance of MainFrame
     */
    public Topology(TopologyVisualisation topolElementTopComponent) {
        this.topolElementTopComponent = topolElementTopComponent;
    }

    public void setSelectedSingleVertex(TopologyVertex selectedSingleVertex) {
        this.selectedSingleVertex = selectedSingleVertex;
        //update copy action button in toolbar
        if (selectedSingleVertex == null) {
            topolElementTopComponent.updateCopyButton(false);
        } else {
            topolElementTopComponent.updateCopyButton(true);
        }
    }

    /**
     * sets topology to its default mode
     */
    public void setDefaultMode() {
        setMode(DEFAULT_MODE);
    }

    /**
     * retrieves all selected vertices
     *
     * @return
     */
    public Set<TopologyVertex> getSelectedVertices() {
        return vv.getPickedVertexState().getPicked();
    }

    /**
     * retrieves all selected edges
     *
     * @return
     */
    public Set<TopologyEdge> getSelectedEdges() {
        return vv.getPickedEdgeState().getPicked();
    }

    /**
     * sets topology into specified mode. each mode is used for different
     * purpose. in fact mode means set of JUNG plugins
     *
     * @param mode
     */
    public void setMode(TopologyModeEnum mode) {
        if (mode == topologyMode) {//mode did not change
            return;
        }

        clearRoutingMode();
        clearTopologyCreateMode();

        switch (mode) {
            case CREATION:
                initTopologyCreateMode();
                break;
            case SIMULATION:
                break;
            case SIMULATION_RULES:
                initTopologySimulationRulesCreationMode();
                break;
            case ROUTING:
                initTopologyRoutingMode();
                break;
            default:
                throw new IllegalStateException("unkown TopologyModeEnum");
        }
        topologyMode = mode;
    }

    /**
     * reverts all changes made by Routing mode
     */
    private void clearRoutingMode() {
        vv.getRenderContext().setEdgeDrawPaintTransformer(new PickableEdgePaintTransformer<TopologyEdge>(vv.getPickedEdgeState(), Color.black, Color.cyan));
    }

    private void clearTopologyCreateMode() {
        if (defaultGm == null) {
            defaultGm = new DefaultModalGraphMouse();
            defaultGm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        }
        vv.setGraphMouse(defaultGm);

        TopologyPaletteTopComponent component = (TopologyPaletteTopComponent) WindowManager.getDefault().findTopComponent("TopologyPaletteTopComponent");
        if (component == null) {
            logg.error("Could not find component TopologyPaletteTopComponent");
            return;
        }
        component.setEnabledPalette(false);
    }

    /**
     * programatically (manually) select specified vertex
     *
     * @param v
     * @param selected
     */
    public void manuallySelectVertex(TopologyVertex v, boolean selected) {
        vv.getPickedVertexState().pick(v, selected);
    }

    /**
     * programatically (manually) select specified edge
     *
     * @param e
     * @param selected
     */
    public void manuallySelectEdge(TopologyEdge e, boolean selected) {
        vv.getPickedEdgeState().pick(e, selected);
    }

    @Override
    public void vertexCreatedOccurred(VertexCreatedEvent evt) {
        logg.debug("vertex created");

        topolElementTopComponent.getTopologyElementCreator().cancelAction();
        topolElementTopComponent.paletteClearSelection();
    }

    /**
     * initialises JUNG stuff. here are all default plugins initialised
     *
     */
    public void initTopology() {
        logg.debug("init jung - creation");

        //vertex as icon
        Transformer<TopologyVertex, Paint> vpf = new PickableVertexPaintTransformer<TopologyVertex>(vv.getPickedVertexState(), Color.white, Color.yellow);
        vv.getRenderContext().setVertexFillPaintTransformer(vpf);
        vv.getRenderContext().setEdgeDrawPaintTransformer(new PickableEdgePaintTransformer<TopologyEdge>(vv.getPickedEdgeState(), Color.black, Color.cyan));

        final MyVertexIconShapeTransformer<TopologyVertex> vertexImageShapeFunction = new MyVertexIconShapeTransformer<TopologyVertex>();
        final DefaultVertexIconTransformer<TopologyVertex> vertexIconFunction = new VertexToIconTransformer<TopologyVertex>();

        vv.getRenderContext().setVertexShapeTransformer(vertexImageShapeFunction);
        vv.getRenderContext().setVertexIconTransformer(vertexIconFunction);


        //tooltips over the vertex
        vv.setVertexToolTipTransformer(new Transformer<TopologyVertex, String>() {
            @Override
            public String transform(TopologyVertex topologyVertex) {
                if (!PreferenciesHelper.isNodeTooltipName() && !PreferenciesHelper.isNodeTooltipDescription()) {
                    return null;
                }
                StringBuilder sb = new StringBuilder("<html>");
                if (PreferenciesHelper.isNodeTooltipName()) {
                    sb.append("<b>Name: </b>").append(topologyVertex.getName());
                }
                if (PreferenciesHelper.isNodeTooltipDescription()) {
                    if (PreferenciesHelper.isNodeTooltipName()) {
                        sb.append("<br>");
                    }
                    sb.append("<b>Description: </b>").append(topologyVertex.getDescription());
                }
                sb.append("</html>");
                return sb.toString();
            }
        });
        vv.getRenderContext().setVertexLabelRenderer(new MyVertexLabelRenderer(Color.BLACK));
        vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.S);
        vv.getRenderContext().setVertexLabelTransformer(new Transformer<TopologyVertex, String>() {
            @Override
            public String transform(TopologyVertex v) {
                if (PreferenciesHelper.isShowNodeNamesInTopology()) {
                    return v.getName();
                }
                return null;
            }
        });


        //picking support, so that vertices can be selected
        vv.setPickSupport(new ShapePickSupport<TopologyVertex, TopologyEdge>(vv));

        PickedState<TopologyVertex> pickedState = vv.getPickedVertexState();
        pickedState.addItemListener(new VertexPickedTopolCreationListener(vertexIconFunction, topolElementTopComponent, pickedState));


    }

    /**
     * inits stuff related to Creation Mode
     */
    private void initTopologyCreateMode() {

        //init mouse
        initMouseControlTopologyCreation(topolElementTopComponent);

        //palette
        TopologyPaletteTopComponent component = (TopologyPaletteTopComponent) WindowManager.getDefault().findTopComponent("TopologyPaletteTopComponent");
        if (component == null) {
            logg.error("Could not find component TopologyPaletteTopComponent");
            return;
        }
        component.setEnabledPalette(true);

    }

    /**
     * initializes mouse controls for topology creation
     *
     * @param mainFrame reference to MainFrame object
     */
    private void initMouseControlTopologyCreation(TopologyVisualisation topolElementTopComponent) {
        if (graphMouse == null) {
            graphMouse = new MyGraphMouse(vv.getRenderContext(), vertexFactory, this);
            graphMouse.addVertexCreatedListener(this);
            graphMouse.addVertexCreatedListener(topolElementTopComponent);
            graphMouse.add(new TranslatingGraphMousePlugin(InputEvent.BUTTON3_MASK));

            popupVertexMenuMousePlugin = new PopupVertexEdgeMenuMousePlugin(this);
            graphMouse.add(popupVertexMenuMousePlugin);
            graphMouse.setZoomAtMouse(true);
        }

        graphMouse.setMode(ModalGraphMouse.Mode.PICKING);
        vv.setGraphMouse(graphMouse);
    }

    /**
     * initialises JUNG stuff when user is creating simulation rules
     *
     * @param mainFrame reference to MainFrame object
     */
    private void initTopologySimulationRulesCreationMode() {
        PickedState<TopologyVertex> ps = vv.getPickedVertexState();
        vv.removeGraphMouseListener(vertexPickedListener);
        DefaultVertexIconTransformer<TopologyVertex> vertexIconTransformer = (DefaultVertexIconTransformer<TopologyVertex>) vv.getRenderContext().getVertexIconTransformer();
        vertexPickedListener = new VertexPickedSimulRulesListener(vertexIconTransformer, topolElementTopComponent, topolElementTopComponent.getAddSimulRuleTopComponent(), ps);
        vv.addGraphMouseListener(vertexPickedListener);
    }

    /**
     * inits plugins for routing mode
     */
    private void initTopologyRoutingMode() {
    }

    /**
     * deletes vertex from JUNG topology
     *
     * @param vertex vertex to delete
     */
    public void deleteVertex(TopologyVertex vertex) {
        vertexFactory.deleteVertex(vertex);
        g.removeVertex(vertex);
        getVv().repaint();
        topolElementTopComponent.topologyModified();
        fireVertexDeletedEvent(new VertexDeletedEvent(this, vertex));
    }

    public void addVertex(TopologyVertex newVertex, Point location) {
        Graph<TopologyVertex, TopologyEdge> graph = vv.getModel().getGraphLayout().getGraph();

        graph.addVertex(newVertex);

        layout.setLocation(newVertex, vv.getRenderContext().getMultiLayerTransformer().inverseTransform(location));
        vv.repaint();
        fireVertexCreatedEvent(new VertexCreatedEvent(this, newVertex));
    }

    /**
     * deletes multiple vertices from JUNG topology
     *
     * @param vertex vertex to delete
     */
    public void deleteVertex(Collection<TopologyVertex> vertices) {
        for (TopologyVertex vertex : vertices) {
            vertexFactory.deleteVertex(vertex);
            g.removeVertex(vertex);
        }
        getVv().repaint();
        topolElementTopComponent.topologyModified();
        fireVertexDeletedEvent(new VertexDeletedEvent(this, vertices));
    }

    /**
     * adds new edge into the topology all edges are not-oriented
     *
     * @param begin starting vertex
     * @param end ending vertex
     * @param edge instance of TopologyEdge that defines edge
     */
    public void addEdge(TopologyVertex begin, TopologyVertex end, TopologyEdge edge) {
        g.addEdge(edge, begin, end);
        topolElementTopComponent.paletteClearSelection();
    }

    /**
     * check that this edge is not forbidden
     *
     * @param v1 first end of the edge
     * @param v2 second end of the edge
     * @return null of edge is allowed; v1 or v2 depending on which of them
     * blocks the edge (if both, then v1 is returned)
     */
    public TopologyVertex isEdgeAllowed(TopologyVertex v1, TopologyVertex v2) {
        TopologyEdge dummyEdge = new TopologyEdge(null, v1, v2);//create a dummy edge for JUNG, so I will be able to get neighbours
        g.addEdge(dummyEdge, v1, v2);

        //tests vertex v1
        Map<Class, Integer> routingRule1 = v1.getDataModel().getRoutingRules();
        for (Class c : routingRule1.keySet()) {
            int count = RoutingHelper.getNumberOfNeighboursByType(this, v1, c);
            if (count > routingRule1.get(c)) {
                //do not forget do delete the dummy edge
                g.removeEdge(dummyEdge);
                return v1;
            }
        }
        //tests vertex v2
        routingRule1 = v2.getDataModel().getRoutingRules();
        for (Class c : routingRule1.keySet()) {
            int count = RoutingHelper.getNumberOfNeighboursByType(this, v2, c);
            if (count > routingRule1.get(c)) {
                //do not forget do delete the dummy edge
                g.removeEdge(dummyEdge);
                return v2;
            }
        }

        //do not forget do delete the dummy edge
        g.removeEdge(dummyEdge);

        return null;
    }

    /**
     * deletes edge from JUNG topology
     *
     * @param edge edge to delete
     */
    public void deleteEdge(TopologyEdge edge) {
        g.removeEdge(edge);
        getVv().repaint();
        topolElementTopComponent.paletteClearSelection();
        topolElementTopComponent.topologyModified();
    }

    /**
     * deletes edge from JUNG topology
     *
     * @param edge edge to delete
     */
    public void deleteEdge(Collection<TopologyEdge> edges) {
        for (TopologyEdge edge : edges) {
            g.removeEdge(edge);
        }
        getVv().repaint();
        topolElementTopComponent.paletteClearSelection();
        topolElementTopComponent.topologyModified();

    }

    /**
     * getter for BasicVisualizationServer
     *
     * @return BasicVisualizationServer instance
     */
    public BasicVisualizationServer<TopologyVertex, TopologyEdge> getVv() {
        return vv;
    }

    /**
     * sets JUNG's mode to PICIKNG, so that new vertices can be selected
     *
     * @see #setEditingMode()
     * @see #setTransformingMode()
     */
    public void setPickingMode() {
        graphMouse.setMode(ModalGraphMouse.Mode.PICKING);
    }

    /**
     * sets JUNG's mode to PICIKNG, so that user can move around the topology
     *
     * @see #setEditingMode()
     * @see #setPickingMode()
     */
    public void setTransformingMode() {
        graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);
    }

    public void repaintGraph() {
        vv.repaint();
    }

    public boolean edgeExists(TopologyVertex v1, TopologyVertex v2) {
        if (g.findEdge(v1, v2) == null) {
            return false;
        }
        return true;
    }

    /**
     * if no settings were loaded from file
     */
    public void createDefaultSettings() {
        vertexFactory = new TopologyVertexFactory(topolElementTopComponent);
        g = new UndirectedSparseGraph<TopologyVertex, TopologyEdge>();

        layout = new StaticLayout<TopologyVertex, TopologyEdge>(g);

        vv = new MyVisualizationViewer(layout);

        vv.setBackground(Color.WHITE);

        topolElementTopComponent.addJungIntoFrame(vv);
    }

    /**
     * this topology was created from file
     *
     * @param loadSettings
     */
    public void loadFromSettings(DeserialisationResult loadSettings) {
        if (loadSettings == null) {
            return;
        }

        vertexFactory = loadSettings.getVertexFactory();

        g = loadSettings.getG();
        layout = loadSettings.getLayout();
        layout.setGraph(g);
        vv = new MyVisualizationViewer(layout);
        vv.setBackground(Color.WHITE);
        topolElementTopComponent.addJungIntoFrame(vv);

    }

    /**
     * higlights edges from one vertex to other. to computer edges between these
     * two vertices will be used algorithm specified in file settings. This
     * method may be used only when Topology is in ROUTING mode
     *
     * @param sourceVertex
     * @param destinationVertex
     * @param vertices between them
     * @param distanceVector true if distance vector routing protocol
     */
    public void highlightEdgesFromTo(TopologyVertex source, TopologyVertex destination, List<TopologyVertex> fixedVertices, boolean distanceVector) throws RoutingException {
        if (TopologyModeEnum.ROUTING != topologyMode) {
            return;
        }
        //first retirieve edges between these two vertices        
        final Collection<TopologyEdge> edges = RoutingHelper.retrieveEdges(getG(), source, destination, distanceVector, fixedVertices);
        if (!routingHelper.checkRouteForCycle(edges)) {
            throw new RoutingException(NbBundle.getMessage(Topology.class, "cycle_exception"));
        }
        //now highlight each edge
        vv.getRenderContext().setEdgeDrawPaintTransformer(new Transformer<TopologyEdge, Paint>() {
            @Override
            public Paint transform(TopologyEdge i) {
                for (TopologyEdge e : edges) {
                    if (i == e) { //they are literally the same - the same object
                        return Color.BLUE;
                    }
                }
                return Color.BLACK;
            }
        });
        vv.repaint();
        //and do not forget to highlight source and destination vertices, too
    }

    /**
     * deselects all selected vertices
     */
    public void deselectVertices() {
        if (NetbeansWindowHelper.getInstance().getActiveTopology() == null) {
            return;
        }

        vv.getPickedVertexState().clear();

        for (TopologyVertex vertex : NetbeansWindowHelper.getInstance().getActiveTopology().getVertexFactory().getAllVertices()) {
            vertex.deSelectVertex();
            vertex.deCheckVertex();
        }
    }

    /**
     * deselects all selected edges
     */
    public void deselectEdges() {
        vv.getPickedEdgeState().clear();
    }

    public void addVertexCreatedListener(VertexCreatedListener listener) {
        graphMouse.addVertexCreatedListener(listener);
    }

    public void removeVertexCreatedListener(VertexCreatedListener listener) {
        graphMouse.removeVertexCreatedListener(listener);
    }

    private void fireVertexCreatedEvent(VertexCreatedEvent event) {
        graphMouse.fireVertexCreatedEvent(event);
    }

    public void addVertexDeletedListener(VertexDeletedListener listener) {
        listenerList.add(VertexDeletedListener.class, listener);
    }

    public void removeVertexDeletedListener(VertexDeletedListener listener) {
        listenerList.remove(VertexDeletedListener.class, listener);
    }

    private void fireVertexDeletedEvent(VertexDeletedEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        // Each listener occupies two elements - the first is the listener class
        // and the second is the listener instance
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == VertexDeletedListener.class) {
                ((VertexDeletedListener) listeners[i + 1]).vertexDeletedOccurred(evt);
            }
        }
    }

    /**
     * this is renderer for vertex labels. the problem with a default one is
     * that when vertex is selected, label is blue - I do not want this
     * behaviour, because I do not use Pick mouse plugin
     */
    public static class MyVertexLabelRenderer extends DefaultVertexLabelRenderer {

        private Color background_color;
        private Color foreground_color;

        public MyVertexLabelRenderer(Color pickedVertexLabelColor) {
            super(pickedVertexLabelColor);
            foreground_color = Color.BLACK;
            background_color = Color.WHITE;
        }

        @Override
        public <V> Component getVertexLabelRendererComponent(JComponent vv,
                Object value, Font font, boolean isSelected, V vertex) {
            Component res = super.getVertexLabelRendererComponent(vv, value, font,
                    isSelected, vertex);

            res.setForeground(foreground_color);
            res.setBackground(background_color);

            return res;
        }
    }

    public enum TopologyModeEnum {

        CREATION,//creating new topology
        SIMULATION_RULES,//defining simulation rules
        ROUTING,//used when defining routing path
        SIMULATION//simulation running
    }
}
