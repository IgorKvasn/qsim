package sk.stuba.fiit.kvasnicka.topologyvisual.graph.utils;

/**
 * @author Igor Kvasnicka
 */
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.control.*;
import java.awt.event.InputEvent;
import org.apache.commons.collections15.Factory;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.edges.TopologyEdge;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.topology.Topology;

public class MyEditingModalGraphMouse extends EditingModalGraphMouse<TopologyVertex, TopologyEdge> {

    /**
     * creates new instance
     *
     * @param rc render context
     * @param vertexFactory vertex factory to create new vertices
     */
    public MyEditingModalGraphMouse(RenderContext<TopologyVertex, TopologyEdge> rc, Factory<TopologyVertex> vertexFactory, Topology topology) {
        super(rc, vertexFactory, null);
    }

    @Override
    public void setMode(Mode mode) {
        super.setMode(mode);

        remove(annotatingPlugin);
        remove(popupEditingPlugin);
    }

    /**
     * create the plugins, and load the plugins for TRANSFORMING mode
     */
    @Override
    protected void loadPlugins() {
        pickingPlugin = new PickingGraphMousePlugin<TopologyVertex, TopologyEdge>();
        animatedPickingPlugin = new AnimatedPickingGraphMousePlugin<TopologyVertex, TopologyEdge>();
        translatingPlugin = new TranslatingGraphMousePlugin(InputEvent.BUTTON1_MASK);
        scalingPlugin = new ScalingGraphMousePlugin(new CrossoverScalingControl(), 0, in, out);
        rotatingPlugin = new RotatingGraphMousePlugin();
        shearingPlugin = new ShearingGraphMousePlugin();
        editingPlugin = new MyEditingGraphMousePlugin(vertexFactory, edgeFactory);

        labelEditingPlugin = new LabelEditingGraphMousePlugin<TopologyVertex, TopologyEdge>();
        add(scalingPlugin);
        setMode(Mode.PICKING);
    }

    public MyEditingGraphMousePlugin getMyEditingGraphMousePlugin() {
        return (MyEditingGraphMousePlugin) editingPlugin;
    }
}
