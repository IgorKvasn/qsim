/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.topology.qos;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.openide.util.NbBundle;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.impl.DscpClassification;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.utils.ClassDefinition;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.utils.ParameterException;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.utils.QosUtils;

/**
 *
 * @author Igor Kvasnicka
 */
public class ClassDefinitionDialog extends javax.swing.JDialog {

    private DefaultTreeModel treeModel;
    private MutableTreeNode availableQueuesNode;
    private MutableTreeNode rootNode;
    @Getter
    private ClassDefinition[] classes;
    private boolean isDscp;
    private Logger logg = Logger.getLogger(ClassDefinitionDialog.class);

    /**
     * Creates new form ClassDefinitionDialog
     */
    public ClassDefinitionDialog(JDialog parent, Set<Integer> queues, boolean isDscp) {
        super(parent, true);
        this.isDscp = isDscp;
        init();
        fillTree(queues);
        expandTree(jTree1);
    }

    public ClassDefinitionDialog(JDialog parent, ClassDefinition[] classes) {
        super(parent, true);
        init();

        for (ClassDefinition def : classes) {
            addClass(def.getName(), def.getQueueNumbers());
        }
    }

    private void init() {
        initComponents();
        jTree1.setDragEnabled(true);
        jTree1.setDropMode(DropMode.ON_OR_INSERT);
        jTree1.setTransferHandler(new TreeTransferHandler(NbBundle.getMessage(ClassDefinitionDialog.class, "undefined_queues_tree_node")));
        jTree1.getSelectionModel().setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
        jTree1.setRootVisible(false);
    }

    public void showDialog() {
        setVisible(true);
    }

    private ClassDefinition[] makeClasses() throws ParameterException {
        if (availableQueuesNode.getChildCount() != 0) {
            throw new ParameterException("There are some queues that does not belong to any QoS class.");
        }

        List<ClassDefinition> classesList = new LinkedList<ClassDefinition>();

        for (int i = 0; i < rootNode.getChildCount(); i++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) rootNode.getChildAt(i);
            if (!node.isLeaf()) {
                List<Integer> list = new LinkedList<Integer>();
                TreeNode child = (TreeNode) treeModel.getChild(rootNode, i);
                for (int j = 0; j < child.getChildCount(); j++) {
                    String q = (String) ((DefaultMutableTreeNode) child.getChildAt(j)).getUserObject();
                    if (StringUtils.isNumeric(q)) {//it is a plain number
                        logg.debug("queue is integer");
                        list.add(Integer.parseInt(q));
                    } else {//it is a DSCP value
                        if (!isDscp) {
                            throw new IllegalStateException("queue is a string, but it is not a DSCP value");
                        }
                        logg.debug("queue is DSCP string value");
                        list.add(DscpClassification.DscpValuesEnum.valueOf(q).getQosQueue());
                    }

                }
                if (list.isEmpty()) {//emtpy classes will be ignored
                    continue;
                }
                ClassDefinition def = new ClassDefinition(list, (String) node.getUserObject());
                classesList.add(def);
            }
        }

        ClassDefinition[] result = classesList.toArray(new ClassDefinition[classesList.size()]);
        QosUtils.checkClassDefinition(result);
        return result;
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

    private void addClass() {
        //ask user for class's name
        String name = JOptionPane.showInputDialog(this, "Qos class name:");

        if (StringUtils.isEmpty(name)) {
            return;
        }

        if (!isClassNameUnique(name)) {
            JOptionPane.showMessageDialog(this,
                    "QoS class with this name already exists.",
                    "Class name duplicity",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        treeModel.insertNodeInto(new DefaultMutableTreeNode(name), rootNode, rootNode.getChildCount());
    }

    private void addClass(String name, List<Integer> queueNumbers) {
        DefaultMutableTreeNode classNode = new DefaultMutableTreeNode(name);
        treeModel.insertNodeInto(classNode, rootNode, rootNode.getChildCount());

        for (int queue : queueNumbers) {
            String label;
            if (isDscp) {
                label = DscpClassification.findDscpValueByQueueNumber(queue).getTextName();
            } else {
                label = String.valueOf(queue);
            }
            treeModel.insertNodeInto(new DefaultMutableTreeNode(label), classNode, classNode.getChildCount());
        }

    }

    /**
     * detects if class name is unique
     *
     * @param name
     * @return
     */
    private boolean isClassNameUnique(String name) {
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) rootNode.getChildAt(i);
            String nodeName = ((String) node.getUserObject());
            //I do not care about "undefined queues" tree node
            if (nodeName.equals(NbBundle.getMessage(ClassDefinitionDialog.class, "undefined_queues_tree_node"))) {
                continue;
            }
            if (nodeName.equals(name)) {
                return false;
            }
        }
        return true;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setModalityType(java.awt.Dialog.ModalityType.APPLICATION_MODAL);

        jScrollPane1.setViewportView(jTree1);

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(ClassDefinitionDialog.class, "ClassDefinitionDialog.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ClassDefinitionDialog.class, "ClassDefinitionDialog.jLabel1.text")); // NOI18N

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/add.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(ClassDefinitionDialog.class, "ClassDefinitionDialog.jButton2.text")); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(33, 33, 33)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(36, 36, 36)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(63, 63, 63)
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(44, 44, 44)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton1)
                .addContainerGap(20, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        try {
            classes = makeClasses();
            setVisible(false);
        } catch (ParameterException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        addClass();
    }//GEN-LAST:event_jButton2ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTree jTree1;
    // End of variables declaration//GEN-END:variables

    private void fillTree(Collection<Integer> queues) {
        rootNode = new DefaultMutableTreeNode();
        treeModel = new DefaultTreeModel(rootNode);
        availableQueuesNode = new DefaultMutableTreeNode(NbBundle.getMessage(ClassDefinitionDialog.class, "undefined_queues_tree_node"));
        treeModel.insertNodeInto(availableQueuesNode, rootNode, 0);
        int count = 0;
        for (int i : queues) {
            String label;
            if (isDscp) {
                label = DscpClassification.findDscpValueByQueueNumber(i).getTextName();
            } else {
                label = String.valueOf(i);
            }
            treeModel.insertNodeInto(new DefaultMutableTreeNode(label), availableQueuesNode, count);
            count++;
        }
        jTree1.setModel(treeModel);
    }

    class TreeTransferHandler extends TransferHandler {

        DataFlavor nodesFlavor;
        DataFlavor[] flavors = new DataFlavor[1];
        DefaultMutableTreeNode[] nodesToRemove;
        private final String notDraggable;

        public TreeTransferHandler(String notDraggable) {
            try {
                String mimeType = DataFlavor.javaJVMLocalObjectMimeType
                        + ";class=\""
                        + javax.swing.tree.DefaultMutableTreeNode[].class.getName()
                        + "\"";
                nodesFlavor = new DataFlavor(mimeType);
                flavors[0] = nodesFlavor;
            } catch (ClassNotFoundException e) {
                System.out.println("ClassNotFound: " + e.getMessage());
            }
            this.notDraggable = notDraggable;
        }

        @Override
        public boolean canImport(TransferHandler.TransferSupport support) {
            if (!support.isDrop()) {
                return false;
            }
            support.setShowDropLocation(true);
            if (!support.isDataFlavorSupported(nodesFlavor)) {
                return false;
            }
            // Do not allow a drop on the drag source selections.
            JTree.DropLocation dl =
                    (JTree.DropLocation) support.getDropLocation();
            JTree tree = (JTree) support.getComponent();
            int dropRow = tree.getRowForPath(dl.getPath());
            int[] selRows = tree.getSelectionRows();
            for (int i = 0; i < selRows.length; i++) {
                if (selRows[i] == dropRow) {
                    return false;
                }
            }

            //not draggable nodes
            for (TreePath path : tree.getSelectionPaths()) {
                if ((((DefaultMutableTreeNode) path.getLastPathComponent())).getUserObject() instanceof DefaultMutableTreeNode) {
                    System.out.println("");
                }
                String userObject = (String) ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
                if (userObject.equals(notDraggable)) {
                    return false;
                }
            }

            TreePath dest = dl.getPath();
            DefaultMutableTreeNode target =
                    (DefaultMutableTreeNode) dest.getLastPathComponent();
            if (target.getParent() == null) {
                return false;
            }

            if (target.isLeaf() && (target.getParent().getParent() != null)) {
                return false;
            }



            // Do not allow MOVE-action drops if a non-leaf node is
            // selected unless all of its children are also selected.
            int action = support.getDropAction();
            if (action == MOVE) {
                return haveCompleteNode(tree);
            }
            // Do not allow a non-leaf node to be copied to a level
            // which is less than its source level.

            TreePath path = tree.getPathForRow(selRows[0]);
            DefaultMutableTreeNode firstNode =
                    (DefaultMutableTreeNode) path.getLastPathComponent();
            if (firstNode.getChildCount() > 0
                    && target.getLevel() < firstNode.getLevel()) {
                return false;
            }

            return true;
        }

        private boolean haveCompleteNode(JTree tree) {
            int[] selRows = tree.getSelectionRows();
            TreePath path = tree.getPathForRow(selRows[0]);
            DefaultMutableTreeNode first =
                    (DefaultMutableTreeNode) path.getLastPathComponent();
            int childCount = first.getChildCount();
            // first has children and no children are selected.
            if (childCount > 0 && selRows.length == 1) {
                return false;
            }
            // first may have children.
            for (int i = 1; i < selRows.length; i++) {
                path = tree.getPathForRow(selRows[i]);
                DefaultMutableTreeNode next =
                        (DefaultMutableTreeNode) path.getLastPathComponent();
                if (first.isNodeChild(next)) {
                    // Found a child of first.
                    if (childCount > selRows.length - 1) {
                        // Not all children of first are selected.
                        return false;
                    }
                }
            }
            return true;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            JTree tree = (JTree) c;
            TreePath[] paths = tree.getSelectionPaths();
            if (paths != null) {
                // Make up a node array of copies for transfer and
                // another for/of the nodes that will be removed in
                // exportDone after a successful drop.
                List<DefaultMutableTreeNode> copies =
                        new ArrayList<DefaultMutableTreeNode>();
                List<DefaultMutableTreeNode> toRemove =
                        new ArrayList<DefaultMutableTreeNode>();
                DefaultMutableTreeNode node =
                        (DefaultMutableTreeNode) paths[0].getLastPathComponent();
                DefaultMutableTreeNode copy = copy(node);
                copies.add(copy);
                toRemove.add(node);
                for (int i = 1; i < paths.length; i++) {
                    DefaultMutableTreeNode next =
                            (DefaultMutableTreeNode) paths[i].getLastPathComponent();
                    // Do not allow higher level nodes to be added to list.
                    if (next.getLevel() < node.getLevel()) {
                        break;
                    } else if (next.getLevel() > node.getLevel()) {  // child node
                        copy.add(copy(next));
                        // node already contains child
                    } else {                                        // sibling
                        copies.add(copy(next));
                        toRemove.add(next);
                    }
                }
                DefaultMutableTreeNode[] nodes =
                        copies.toArray(new DefaultMutableTreeNode[copies.size()]);
                nodesToRemove =
                        toRemove.toArray(new DefaultMutableTreeNode[toRemove.size()]);
                return new NodesTransferable(nodes);
            }
            return null;
        }

        /**
         * Defensive copy used in createTransferable.
         */
        private DefaultMutableTreeNode copy(TreeNode node) {
            return new DefaultMutableTreeNode(node);
        }

        @Override
        protected void exportDone(JComponent source, Transferable data, int action) {
            if ((action & MOVE) == MOVE) {
                JTree tree = (JTree) source;
                DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
                // Remove nodes saved in nodesToRemove in createTransferable.
                for (int i = 0; i < nodesToRemove.length; i++) {
                    model.removeNodeFromParent(nodesToRemove[i]);
                }
            }
        }

        @Override
        public int getSourceActions(JComponent c) {
            return COPY_OR_MOVE;
        }

        @Override
        public boolean importData(TransferHandler.TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }
            // Extract transfer data.
            DefaultMutableTreeNode[] nodes = null;
            try {
                Transferable t = support.getTransferable();
                DefaultMutableTreeNode[] temp_nodes = (DefaultMutableTreeNode[]) t.getTransferData(nodesFlavor);
                nodes = new DefaultMutableTreeNode[temp_nodes.length];
                for (int i = 0; i < temp_nodes.length; i++) {
                    nodes[i] = new DefaultMutableTreeNode(((DefaultMutableTreeNode) temp_nodes[i].getUserObject()).getUserObject());
                }

            } catch (UnsupportedFlavorException ufe) {
                System.out.println("UnsupportedFlavor: " + ufe.getMessage());
            } catch (java.io.IOException ioe) {
                System.out.println("I/O error: " + ioe.getMessage());
            }
            // Get drop location info.
            JTree.DropLocation dl =
                    (JTree.DropLocation) support.getDropLocation();
            int childIndex = dl.getChildIndex();
            TreePath dest = dl.getPath();


            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) dest.getLastPathComponent();
            if (parent.isLeaf() && (parent.getParent().getParent() != null)) {
                return false;
            }

            JTree tree = (JTree) support.getComponent();
            DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
            // Configure for drop mode.
            int index = childIndex;    // DropMode.INSERT
            if (childIndex == -1) {     // DropMode.ON
                index = parent.getChildCount();
            }
            // Add data to model.
            for (int i = 0; i < nodes.length; i++) {
                model.insertNodeInto(nodes[i], parent, index++);
            }

            //expand parent - useful when creating new direcory by move
            int row = tree.getRowForPath(new TreePath(parent.getPath()));
            tree.expandRow(row);
            return true;
        }

        @Override
        public String toString() {
            return getClass().getName();
        }

        public class NodesTransferable implements Transferable {

            DefaultMutableTreeNode[] nodes;

            public NodesTransferable(DefaultMutableTreeNode[] nodes) {
                this.nodes = nodes;
            }

            @Override
            public Object getTransferData(DataFlavor flavor)
                    throws UnsupportedFlavorException {
                if (!isDataFlavorSupported(flavor)) {
                    throw new UnsupportedFlavorException(flavor);
                }
                return nodes;
            }

            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return flavors;
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return nodesFlavor.equals(flavor);
            }
        }
    }
}