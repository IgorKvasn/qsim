/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.topology;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.AbstractGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.decorators.DefaultVertexIconTransformer;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.picking.ShapePickSupport;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import java.awt.Color;
import java.awt.Paint;
import java.awt.event.InputEvent;
import lombok.Getter;
import org.apache.commons.collections15.Transformer;
import org.apache.log4j.Logger;
import sk.stuba.fiit.kvasnicka.topologyvisual.PreferenciesHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.edges.TopologyEdge;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.utils.MyEditingModalGraphMouse;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.utils.PopupVertexEdgeMenuMousePlugin;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.utils.TopologyVertexFactory;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.utils.VertexPickedListener;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.utils.MyVertexIconShapeTransformer;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.utils.VertexToIconTransformer;
import sk.stuba.fiit.kvasnicka.topologyvisual.palette.gui.TopolElementTopComponent;
import sk.stuba.fiit.kvasnicka.topologyvisual.palette.gui.TopologyMultiviewSimulation;
import sk.stuba.fiit.kvasnicka.topologyvisual.serialisation.SerialisationHelper;

/**
 *
 * @author Igor Kvasnicka
 */
public class TopologySimulation {

    private static Logger logg = Logger.getLogger(TopologySimulation.class);
    private TopologyMultiviewSimulation routingWindow;
    private AbstractGraph<TopologyVertex, TopologyEdge> g;
    private AbstractLayout<TopologyVertex, TopologyEdge> layout;
    private PopupVertexEdgeMenuMousePlugin popupVertexMenuMousePlugin;
    @Getter
    private VisualizationViewer<TopologyVertex, TopologyEdge> vv;
    private DefaultModalGraphMouse<TopologyVertex, TopologyEdge> graphMouse;

    public TopologySimulation(TopologyMultiviewSimulation routingWindow) {
        this.routingWindow = routingWindow;
    }

    /**
     * initialises JUNG stuff
     *
     * @param mainFrame reference to MainFrame object
     */
    public void initTopology() {
        logg.debug("init jung - routing");

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
        vv.getRenderContext().setVertexLabelRenderer(new TopologyCreation.MyVertexLabelRenderer(Color.BLACK));
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

        //init mouse
        initMouseControl();

        //picking support, so that vertices can be selected
        vv.setPickSupport(new ShapePickSupport<TopologyVertex, TopologyEdge>(vv));
        //vv.setPickedVertexState(new MultiPickedState<TopologyVertex>());
        PickedState<TopologyVertex> ps = vv.getPickedVertexState();
//        vv.addGraphMouseListener(new VertexPickedListener(vertexIconFunction, topolElementTopComponent, ps));
    }

    /**
     * initializes mouse controls
     *
     */
    private void initMouseControl() {
        graphMouse = new DefaultModalGraphMouse<TopologyVertex, TopologyEdge>();
        graphMouse.add(new TranslatingGraphMousePlugin(InputEvent.BUTTON3_MASK));
        graphMouse.setMode(ModalGraphMouse.Mode.PICKING);

//todo popup menu        popupVertexMenuMousePlugin = new PopupVertexEdgeMenuMousePlugin(this);
//        graphMouse.add(popupVertexMenuMousePlugin);


        DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
        vv.setGraphMouse(gm);

    }

    /**
     * if no settings were loaded from file
     */
    public void createDefaultSettings() {
        g = new UndirectedSparseGraph<TopologyVertex, TopologyEdge>();
        layout = new StaticLayout<TopologyVertex, TopologyEdge>(g);
        vv = new VisualizationViewer<TopologyVertex, TopologyEdge>(layout);
        vv.setBackground(Color.WHITE);
        routingWindow.addJungIntoFrame(vv);
    }

    /**
     * this topology was created from file
     *
     * @param loadSettings
     */
    public void loadFromSettings(SerialisationHelper.DeserialisationResult loadSettings) {
        if (loadSettings == null) {
            return;
        }

        g = loadSettings.getG();
        layout = loadSettings.getLayout();
        layout.setGraph(g);
        vv = new VisualizationViewer<TopologyVertex, TopologyEdge>(layout);
        vv.setBackground(Color.WHITE);
        routingWindow.addJungIntoFrame(vv);
    }
}
