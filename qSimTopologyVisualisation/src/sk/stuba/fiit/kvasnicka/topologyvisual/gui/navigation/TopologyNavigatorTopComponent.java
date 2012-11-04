/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.navigation;

import java.util.Enumeration;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import sk.stuba.fiit.kvasnicka.topologyvisual.filetype.gui.TopologyVisualisation;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.events.vertexcreated.VertexCreatedEvent;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.events.vertexcreated.VertexCreatedListener;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.events.vertexdeleted.VertexDeletedEvent;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.events.vertexdeleted.VertexDeletedListener;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.utils.TopologyVertexFactory;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.ComputerVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.RouterVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.SwitchVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;

/**
 * Top component which displays something.
 */
//@ConvertAsProperties(
//    dtd = "-//sk.stuba.fiit.kvasnicka.topologyvisual.gui.navigation//TopologyNavigator//EN",
//autostore = false)
@TopComponent.Description(
    preferredID = "TopologyNavigatorTopComponent",
iconBase = "sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/compass.png",
persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "navigator", openAtStartup = false)
@ActionID(category = "Window", id = "sk.stuba.fiit.kvasnicka.topologyvisual.gui.navigation.TopologyNavigatorTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
//@TopComponent.OpenActionRegistration(
//    displayName = "#CTL_TopologyNavigatorAction",
//preferredID = "TopologyNavigatorTopComponent")
@Messages({
    "CTL_TopologyNavigatorAction=TopologyNavigator",
    "CTL_TopologyNavigatorTopComponent=TopologyNavigator Window",
    "HINT_TopologyNavigatorTopComponent=This is a TopologyNavigator window"
})
public final class TopologyNavigatorTopComponent extends TopComponent implements VertexCreatedListener, VertexDeletedListener {

    private DefaultMutableTreeNode computerNode, routerNode, switchNode;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;
    private TopologyVisualisation topologyVisualisation;

    public TopologyNavigatorTopComponent(TopologyVisualisation topologyVisualisation) {
        initComponents();
        this.topologyVisualisation = topologyVisualisation;
        setName(Bundle.CTL_TopologyNavigatorTopComponent());
        setToolTipText(Bundle.HINT_TopologyNavigatorTopComponent());

        initVerticesList(topologyVisualisation.getTopology().getVertexFactory());

        topologyVisualisation.getTopology().addVertexCreatedListener(this);
        topologyVisualisation.getTopology().addVertexDeletedListener(this);

    }

    public void cleanUp() {
        topologyVisualisation.getTopology().removeVertexCreatedListener(this);
        topologyVisualisation.getTopology().removeVertexDeletedListener(this);
    }

    /**
     * initially fills bean tree view component with topology vertices
     *
     * @param vertices
     */
    private void initVerticesList(TopologyVertexFactory factory) {
        rootNode = new DefaultMutableTreeNode();
        treeModel = new DefaultTreeModel(rootNode);
        jXTree1.setModel(treeModel);


        routerNode = new DefaultMutableTreeNode(NbBundle.getMessage(TopologyNavigatorTopComponent.class, "router"));
        treeModel.insertNodeInto(routerNode, rootNode, 0);

        for (RouterVertex rv : factory.getVertexRouterList()) {
            treeModel.insertNodeInto(new DefaultMutableTreeNode(rv), routerNode, 0);
        }

        switchNode = new DefaultMutableTreeNode(NbBundle.getMessage(TopologyNavigatorTopComponent.class, "switch"));
        treeModel.insertNodeInto(switchNode, rootNode, 0);

        for (SwitchVertex sv : factory.getVertexSwitchList()) {
            treeModel.insertNodeInto(new DefaultMutableTreeNode(sv), switchNode, 0);
        }

        computerNode = new DefaultMutableTreeNode(NbBundle.getMessage(TopologyNavigatorTopComponent.class, "computer"));
        treeModel.insertNodeInto(computerNode, rootNode, 0);

        for (ComputerVertex cv : factory.getVertexComputerList()) {
            treeModel.insertNodeInto(new DefaultMutableTreeNode(cv), computerNode, 0);
        }

        expandTree(jXTree1);

    }

    private void expandTree(JTree tree) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        Enumeration e = root.breadthFirstEnumeration();
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
            if (node.isLeaf()) {
                continue;
            }
            int row = tree.getRowForPath(new TreePath(node.getPath()));
            tree.expandRow(row);
        }
    }

    @Override
    public void vertexCreatedOccurred(VertexCreatedEvent evt) {
        TopologyVertex newVertex = evt.getNewVertex();
        if (newVertex instanceof RouterVertex) {
            treeModel.insertNodeInto(new DefaultMutableTreeNode(newVertex), routerNode, 0);
        }
        if (newVertex instanceof ComputerVertex) {
            treeModel.insertNodeInto(new DefaultMutableTreeNode(newVertex), computerNode, 0);
        }
        if (newVertex instanceof SwitchVertex) {
            treeModel.insertNodeInto(new DefaultMutableTreeNode(newVertex), switchNode, 0);
        }
    }

    @Override
    public void vertexDeletedOccurred(VertexDeletedEvent evt) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        jXTree1 = new org.jdesktop.swingx.JXTree();

        setLayout(new java.awt.BorderLayout());

        jXTree1.setRootVisible(false);
        jScrollPane2.setViewportView(jXTree1);

        add(jScrollPane2, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane2;
    private org.jdesktop.swingx.JXTree jXTree1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
}
