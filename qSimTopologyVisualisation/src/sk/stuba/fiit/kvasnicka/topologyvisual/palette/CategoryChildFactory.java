/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.palette;

import java.util.List;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import sk.stuba.fiit.kvasnicka.topologyvisual.palette.gui.TopolElementTopComponent;

/**
 *
 * @author Igor Kvasnicka
 */
@Deprecated
public class CategoryChildFactory extends ChildFactory<String> {

    @Override
    protected boolean createKeys(List<String> toPopulate) {
        toPopulate.add(NbBundle.getMessage(TopolElementTopComponent.class, "nodesCategory"));
        toPopulate.add(NbBundle.getMessage(TopolElementTopComponent.class, "linksCategory"));
        return true;
    }

    @Override
    protected Node createNodeForKey(String category) {
        Node node = new AbstractNode(Children.create(new NodeChildFactory(category), true));
        node.setDisplayName(category);
        return node;
    }

    private static class NodeChildFactory extends ChildFactory<PaletteTopologyElement> {

        private String category;

        public NodeChildFactory(String category) {
            this.category = category;

        }

        @Override
        protected boolean createKeys(List<PaletteTopologyElement> toPopulate) {
            //je to router kategoria
            if (NbBundle.getMessage(TopolElementTopComponent.class, "nodesCategory").equals(category)) {
                toPopulate.add(new PaletteTopologyElement(NbBundle.getMessage(TopolElementTopComponent.class, "routerVertex"), PaletteActionEnum.NEW_VERTEX_ROUTER));
                toPopulate.add(new PaletteTopologyElement(NbBundle.getMessage(TopolElementTopComponent.class, "switchVertex"), PaletteActionEnum.NEW_VERTEX_SWITCH));
                toPopulate.add(new PaletteTopologyElement(NbBundle.getMessage(TopolElementTopComponent.class, "computerVertex"), PaletteActionEnum.NEW_VERTEX_PC));
            }

            //je to link kategoria
            if (NbBundle.getMessage(TopolElementTopComponent.class, "linksCategory").equals(category)) {
                toPopulate.add(new PaletteTopologyElement(NbBundle.getMessage(TopolElementTopComponent.class, "gigaEthernetLink"), PaletteActionEnum.NEW_EDGE_GIGA_ETHERNET));
                toPopulate.add(new PaletteTopologyElement(NbBundle.getMessage(TopolElementTopComponent.class, "fastEthernetLink"), PaletteActionEnum.NEW_EDGE_FAST_ETHERNET));
                toPopulate.add(new PaletteTopologyElement(NbBundle.getMessage(TopolElementTopComponent.class, "ethernetLink"), PaletteActionEnum.NEW_EDGE_ETHERNET));
                toPopulate.add(new PaletteTopologyElement(NbBundle.getMessage(TopolElementTopComponent.class, "customLink"), PaletteActionEnum.NEW_EDGE_CUSTOM));
            }

            return true;
        }

        @Override
        protected Node createNodeForKey(final PaletteTopologyElement key) {
            Node node = new MyNode(key);
            return node;
        }
    }

    public static class MyNode extends AbstractNode {

        private PaletteTopologyElement paletteTopologyElement;

        public MyNode(PaletteTopologyElement paletteTopologyElement) {
            super(Children.LEAF);
            this.paletteTopologyElement = paletteTopologyElement;
            setDisplayName(paletteTopologyElement.getName());
        }

        public PaletteTopologyElement getPaletteTopologyElement() {
            return paletteTopologyElement;
        }
    }
}
