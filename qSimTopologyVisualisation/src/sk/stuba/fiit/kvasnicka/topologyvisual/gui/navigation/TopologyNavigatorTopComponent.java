/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.navigation;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.apache.log4j.Logger;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import sk.stuba.fiit.kvasnicka.topologyvisual.filetype.gui.TopologyVisualisation;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.events.vertexcreated.VertexCreatedEvent;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.events.vertexcreated.VertexCreatedListener;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.events.vertexdeleted.VertexDeletedEvent;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.events.vertexdeleted.VertexDeletedListener;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.utils.PopupVertexEdgeMenuMousePlugin;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.utils.TopologyVertexFactory;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.ComputerVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.RouterVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.SwitchVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.resources.ImageResourceHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.resources.ImageType;
import static sk.stuba.fiit.kvasnicka.topologyvisual.resources.ImageType.TOPOLOGY_VERTEX_COMPUTER;

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
    "CTL_TopologyNavigatorTopComponent=Topology Navigator",
    "HINT_TopologyNavigatorTopComponent=Topology Navigator"
})
public final class TopologyNavigatorTopComponent extends TopComponent implements VertexCreatedListener, VertexDeletedListener {

    private static final Logger logg = Logger.getLogger(TopologyNavigatorTopComponent.class);
    private DefaultMutableTreeNode computerNode, routerNode, switchNode;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;
    private TopologyVisualisation topologyVisualisation;
    private JPopupMenu popupMenu;
    private JMenuItem itemDeleteItem, itemEditItem;

    public TopologyNavigatorTopComponent(TopologyVisualisation topologyVisualisation) {
        initComponents();
        this.topologyVisualisation = topologyVisualisation;
        setName(Bundle.CTL_TopologyNavigatorTopComponent());
        setToolTipText(Bundle.HINT_TopologyNavigatorTopComponent());

        initVerticesList(topologyVisualisation.getTopology().getVertexFactory());

        topologyVisualisation.getTopology().addVertexCreatedListener(this);
        topologyVisualisation.getTopology().addVertexDeletedListener(this);

        jTree1.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        initPopupMenu();

        //disable jtree collapsing
        jTree1.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent e) {
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent e)
                    throws ExpandVetoException {
                throw new ExpandVetoException(e, "you can't collapse this JTree");
            }
        });


        //attach popup on node
        jTree1.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    //right click selection does not work by default - here is a workaround
                    jTree1.getSelectionModel().setSelectionPath(jTree1.getPathForLocation(e.getX(), e.getY()));
                    //show popup
                    TreePath path = jTree1.getPathForLocation(e.getX(), e.getY());
                    //only leafs shows popups
                    if (isVertexCategory((DefaultMutableTreeNode) path.getLastPathComponent())) {
                        return; //it is not a leaf
                    }

                    Rectangle pathBounds = jTree1.getUI().getPathBounds(jTree1, path);
                    if (pathBounds != null && pathBounds.contains(e.getX(), e.getY())) {
                        correctPopup();
                        popupMenu.show(jTree1, pathBounds.x, pathBounds.y + pathBounds.height);
                    }
                }
            }
        });

        jTree1.setCellRenderer(new CustomIconRenderer());
    }

    /**
     * when simulation running, user cannot delete vertex and instead of "Edit"
     * label, "View" label is shown (the same menu item)
     */
    private void correctPopup() {
        if (topologyVisualisation.isSimulationRunning()) {
            itemDeleteItem.setEnabled(false);
            itemEditItem.setText(NbBundle.getMessage(PopupVertexEdgeMenuMousePlugin.class, "view"));
        } else {
            itemDeleteItem.setEnabled(true);
            itemEditItem.setText(NbBundle.getMessage(PopupVertexEdgeMenuMousePlugin.class, "edit"));
        }
    }

    /**
     * determines if specified node is a category node<br>category nodes are
     * computerNode, routerNode and switchNode - so they are "category" nodes
     *
     * @param node
     * @return
     */
    private boolean isVertexCategory(DefaultMutableTreeNode node) {
        if (node.getLevel() <= 1) {
            return true;
        }
        return false;
    }

    /**
     * inits popup menu that will be shown when user right clicks on a JTree
     * node
     */
    private void initPopupMenu() {
        popupMenu = new JPopupMenu();

        JMenuItem itemFind = new JMenuItem(NbBundle.getMessage(TopologyNavigatorTopComponent.class, "find"));
        itemFind.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TopologyVertex selected = getSelectedVertex();
                if (selected == null) {
                    logg.warn("user clicked on popup menu, but no selected node found - so how did he managed to show popup menu?");
                    return;
                }
                topologyVisualisation.centerOnVertex(selected);
            }
        });

        popupMenu.add(itemFind);

        JMenuItem itemCopy = new JMenuItem(NbBundle.getMessage(TopologyNavigatorTopComponent.class, "copy"));
        itemCopy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TopologyVertex selected = getSelectedVertex();
                if (selected == null) {
                    logg.warn("user clicked on popup menu, but no selected node found - so how did he managed to show popup menu?");
                    return;
                }
                topologyVisualisation.performVertexCopy(selected);
            }
        });

        popupMenu.add(itemCopy);

        itemDeleteItem = new JMenuItem(NbBundle.getMessage(TopologyNavigatorTopComponent.class, "delete"));
        itemDeleteItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<TopologyVertex> selected = getSelectedVertices();
                if (selected.isEmpty()) {
                    logg.warn("user clicked on popup menu, but no selected node found - so how did he managed to show popup menu?");
                    return;
                }
                topologyVisualisation.deleteVerticesWithDialog(selected);
            }
        });

        popupMenu.add(itemDeleteItem);

        itemEditItem = new JMenuItem(NbBundle.getMessage(TopologyNavigatorTopComponent.class, "edit"));
        itemEditItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TopologyVertex selected = getSelectedVertex();
                if (selected == null) {
                    logg.warn("user clicked on popup menu, but no selected node found - so how did he managed to show popup menu?");
                    return;
                }
                topologyVisualisation.editVertex(selected);
            }
        });

        popupMenu.add(itemEditItem);

    }

    /**
     * returns all selected vertices (JTree leafs only)
     *
     * @return
     */
    private List<TopologyVertex> getSelectedVertices() {
        TreePath[] paths = jTree1.getSelectionPaths();
        List<TopologyVertex> result = new LinkedList<TopologyVertex>();
        for (TreePath p : paths) {
            if (((TreeNode) p.getLastPathComponent()).isLeaf()) {
                result.add((TopologyVertex) ((DefaultMutableTreeNode) p.getLastPathComponent()).getUserObject());
            }
        }

        return result;
    }

    /**
     * finds selected vertex in tree
     *
     * @return null if nothing is selected
     */
    private TopologyVertex getSelectedVertex() {
        if (jTree1.getSelectionCount() == 0) {
            return null;
        }
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) jTree1.getLastSelectedPathComponent();
        return (TopologyVertex) node.getUserObject();
    }

    /**
     * this dialog is closing, so I need to clean up some listeners and stuff..
     */
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

        jTree1.setModel(treeModel);

        expandTree(jTree1);

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
        expandTree(jTree1);
    }

    @Override
    public void vertexDeletedOccurred(VertexDeletedEvent evt) {
        Collection<TopologyVertex> deletedCollection = evt.getDeletedVertex();
        for (TopologyVertex delV : deletedCollection) {
            if (delV instanceof RouterVertex) {
                treeModel.removeNodeFromParent(findNode(delV.getName(), routerNode));
            }
            if (delV instanceof ComputerVertex) {
                treeModel.removeNodeFromParent(findNode(delV.getName(), computerNode));
            }
            if (delV instanceof SwitchVertex) {
                treeModel.removeNodeFromParent(findNode(delV.getName(), switchNode));
            }
        }
    }

    /**
     * finds tree node by user object (name of TopologyVertex)
     *
     * @param name
     * @return
     */
    private DefaultMutableTreeNode findNode(String name, DefaultMutableTreeNode parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent.getChildAt(i);
            if (((TopologyVertex) (node.getUserObject())).getName().equals(name)) {
                return node;
            }
        }
        throw new IllegalStateException("could not find node/vertex " + name + " in parent " + parent.getUserObject().toString());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();

        setLayout(new java.awt.BorderLayout());

        jTree1.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
        jTree1.setRootVisible(false);
        jScrollPane1.setViewportView(jTree1);

        add(jScrollPane1, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTree jTree1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
    }

    @Override
    public void componentClosed() {
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
    }

    /**
     * some data have been changed - I need to refresh JTree
     */
    public void updateData() {
        jTree1.repaint();
    }

    class CustomIconRenderer extends DefaultTreeCellRenderer {

        ImageIcon routerIcon, computerIcon, switchIcon;
        ImageIcon defaultIcon;

        public CustomIconRenderer() {
            computerIcon = ImageResourceHelper.loadImage("/sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/vertex_computer_16.png");
            routerIcon = ImageResourceHelper.loadImage("/sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/vertex_router_16.png");
            switchIcon = ImageResourceHelper.loadImage("/sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/vertex_switch_16.png");
            defaultIcon = ImageResourceHelper.loadImage("/sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/dot.png");
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
                boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel,
                    expanded, leaf, row, hasFocus);
            ImageIcon icon = getIconForNode((DefaultMutableTreeNode) value);
            if (icon != null) {
                setIcon(icon);
            }

            return this;
        }

        private ImageIcon getIconForNode(DefaultMutableTreeNode node) {
            if (!isVertexCategory(node)) {
                return defaultIcon;
            }
            String name = (String) node.getUserObject();

            if (name.equals(NbBundle.getMessage(TopologyNavigatorTopComponent.class, "router"))) {
                return routerIcon;
            }
            if (name.equals(NbBundle.getMessage(TopologyNavigatorTopComponent.class, "switch"))) {
                return switchIcon;
            }
            if (name.equals(NbBundle.getMessage(TopologyNavigatorTopComponent.class, "computer"))) {
                return computerIcon;
            }

            throw new IllegalStateException("unable to find icon for: " + name);
        }
    }
}
