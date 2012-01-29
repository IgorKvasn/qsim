/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.topology;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.AbstractGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.decorators.DefaultVertexIconTransformer;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.picking.ShapePickSupport;
import edu.uci.ics.jung.visualization.renderers.DefaultVertexLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JComponent;
import lombok.Getter;
import org.apache.commons.collections15.Transformer;
import org.apache.log4j.Logger;
import sk.stuba.fiit.kvasnicka.topologyvisual.PreferenciesHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.filetype.gui.TopologyVisualisation;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.MyVisualizationViewer;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.edges.TopologyEdge;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.events.VertexCreatedEvent;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.events.VertexCreatedListener;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.utils.*;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.utils.MyVertexIconShapeTransformer;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.utils.VertexToIconTransformer;
import sk.stuba.fiit.kvasnicka.topologyvisual.routing.RoutingHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.serialisation.DeserialisationResult;

/**
 * This class is graphical representation of network topology it uses JUNG
 * library
 *
 * @author Igor Kvasnicka
 */
public class Topology implements VertexCreatedListener {

    private static Logger logg = Logger.getLogger(Topology.class);
    @Getter
    private AbstractGraph<TopologyVertex, TopologyEdge> g;
    @Getter
    private AbstractLayout<TopologyVertex, TopologyEdge> layout;
    private PopupVertexEdgeMenuMousePlugin popupVertexMenuMousePlugin;
    private MyVisualizationViewer vv;
    private MyEditingModalGraphMouse graphMouse;
    private TopologyVertexFactory vertexFactory;
    private RoutingHelper routingHelper = new RoutingHelper();
    private final TopologyVisualisation topolElementTopComponent;
    @Getter
    private TopologyModeEnum topologyMode = null;
    private GraphMouseListener<TopologyVertex> vertexPickedListener;

    /**
     * creates new instance
     *
     * @param mainFrame instance of MainFrame
     */
    public Topology(TopologyVisualisation topolElementTopComponent) {
        this.topolElementTopComponent = topolElementTopComponent;
    }

    public TopologyVertexFactory getVertexFactory() {
        return vertexFactory;
    }

    public void setMode(TopologyModeEnum mode) {
        if (mode == topologyMode) {//mode did not change
            return;
        }
        switch (mode) {
            case CREATION:
                initTopologyCreateMode();
                break;
            case SIMULATION:
                break;
            case SIMULATION_RULES:
                initTopologySimulationRulesCreation();
                break;
            case ROUTING:
                break;
            default:
                throw new IllegalStateException("unkown TopologyModeEnum");
        }
        topologyMode = mode;
    }

    @Override
    public void vertexCreatedOccurred(VertexCreatedEvent evt) {
        logg.debug("vertex created");

        topolElementTopComponent.getTopologyElementCreator().cancelAction();
        topolElementTopComponent.paletteClearSelection();
    }

    /**
     * initialises JUNG stuff for topology creation
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
        //vv.setPickedVertexState(new MultiPickedState<TopologyVertex>());

    }

    /**
     * inits stuff related to Creation Mode
     */
    private void initTopologyCreateMode() {
        vv.removeGraphMouseListener(vertexPickedListener);
        PickedState<TopologyVertex> ps = vv.getPickedVertexState();
        DefaultVertexIconTransformer<TopologyVertex> vertexIconTransformer = (DefaultVertexIconTransformer<TopologyVertex>) vv.getRenderContext().getVertexIconTransformer();
        vertexPickedListener = new VertexPickedTopolCreationListener(vertexIconTransformer, topolElementTopComponent, ps);
        vv.addGraphMouseListener(vertexPickedListener);
        //init mouse
        initMouseControlTopologyCreation(topolElementTopComponent);

    }

    /**
     * initializes mouse controls for topology creation
     *
     * @param mainFrame reference to MainFrame object
     */
    private void initMouseControlTopologyCreation(TopologyVisualisation topolElementTopComponent) {
        graphMouse = new MyEditingModalGraphMouse(vv.getRenderContext(), vertexFactory, this);
        graphMouse.getMyEditingGraphMousePlugin().addVertexCreatedListener(this);
        graphMouse.getMyEditingGraphMousePlugin().addVertexCreatedListener(topolElementTopComponent);
        graphMouse.add(new TranslatingGraphMousePlugin(InputEvent.BUTTON3_MASK));
        graphMouse.setMode(ModalGraphMouse.Mode.PICKING);

        popupVertexMenuMousePlugin = new PopupVertexEdgeMenuMousePlugin(this);
        graphMouse.add(popupVertexMenuMousePlugin);
        graphMouse.setZoomAtMouse(true);

        vv.setGraphMouse(graphMouse);
    }

    /**
     * initialises JUNG stuff when user is creating simulation rules
     *
     * @param mainFrame reference to MainFrame object
     */
    private void initTopologySimulationRulesCreation() {
        PickedState<TopologyVertex> ps = vv.getPickedVertexState();
        vv.removeGraphMouseListener(vertexPickedListener);
        DefaultVertexIconTransformer<TopologyVertex> vertexIconTransformer = (DefaultVertexIconTransformer<TopologyVertex>) vv.getRenderContext().getVertexIconTransformer();
        vertexPickedListener = new VertexPickedSimulRulesListener(vertexIconTransformer, topolElementTopComponent, ps);
        vv.addGraphMouseListener(vertexPickedListener);
    }

    /**
     * adds new vertex to the topology
     *
     * @param x x-coordinate of center of the vertex
     * @param y y-coordinate of center of the vertex
     * @param element what kind of element is about to add (router, PC,...)
     */
    public void addVertex(TopologyVertex element, int x, int y) {
        addVertex(element, new Point(x, y));
    }

    /**
     * adds new vertex to the topology
     *
     * @param position vertex position of the center
     * @param element what kind of element is about to add (router, PC,...)
     */
    public void addVertex(TopologyVertex element, Point2D position) {
        g.addVertex(element);
        layout.setLocation(element, vv.getRenderContext().getMultiLayerTransformer().inverseTransform(position));
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
     * sets JUNG's mode to EDITINNG, so that new vertices can be created
     *
     * @see #setTransformingMode()
     * @see #setPickingMode()
     */
    public void setEditingMode() {
        graphMouse.setMode(ModalGraphMouse.Mode.EDITING);
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

        vertexFactory = loadSettings.getVFactory();

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
     */
    public void highlightEdgesFromTo(TopologyVertex source, TopologyVertex dest) {
        if (TopologyModeEnum.ROUTING != topologyMode) {
            return;
        }
        //first retirieve edges between these two vertices
        boolean distanceVector = topolElementTopComponent.getDataObject().getLoadSettings().isDistanceVectorRouting();
        final Collection<TopologyEdge> edges = routingHelper.retrieveEdges(getG(), source, distanceVector, dest);
        //now highlight each edge
        vv.getRenderContext().setEdgeFillPaintTransformer(new Transformer<TopologyEdge, Paint>() {

            @Override
            public Paint transform(TopologyEdge i) {
                for (TopologyEdge e : edges) {
                    if (i == e) { //they are literally the same - the same object
                        return Color.YELLOW;
                    }
                }
                return Color.BLACK;
            }
        });

        //and do not forget to highlight source and destination vertices, too
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
