/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.topology.qos;

import java.awt.Dialog;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.queuemanagement.impl.WeightedRED.WredDefinition;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.utils.ClassDefinition;
import sk.stuba.fiit.kvasnicka.topologyvisual.exceptions.QosCreationException;

/**
 *
 * @author Igor Kvasnicka
 */
public class WredQueueManagementDialog extends javax.swing.JDialog {

    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;
    private Map<String, Integer> classMapMap;//key=class name; value=tree node index
    private Map<String, WredDefinition> configuration = new HashMap<String, WredDefinition>(); //key =class name; value=WredDefinition
    private ClassDefinition[] classes;

    /**
     * Creates new form WredQueueManagementDialog
     */
    public WredQueueManagementDialog(JDialog owner, ClassDefinition[] classes) {
        super(owner, true);
        init();
        this.classes = classes;
        createClasses(classes);
    }

    /**
     * constructor that loads previous configuration - e.g. when editing vertex
     *
     * @param owner
     * @param params
     */
    public WredQueueManagementDialog(JDialog owner, WredDefinition[] params) {
        super(owner, true);
        init();

        //make  ClassDefinition[] from WredDefinition[]
        // this.classes = classes;
        //call  createClasses(classes);        
    }

    private void init() {
        initComponents();
        treeModel = (DefaultTreeModel) jTree1.getModel();
        classMapMap = new HashMap<String, Integer>();
        jTree1.addTreeSelectionListener(new SelectionListener());
        selectTreeNode(0);
    }

    /**
     * initially create queues
     *
     * @param queueCount
     */
    private void createClasses(ClassDefinition[] classes) {
        try {
            for (int i = 0; i < classes.length; i++) {
                addClass(classes[i].getName());
            }
        } catch (QosCreationException ex) {
            Exceptions.printStackTrace(ex);//this should not happen
        }
    }

    private void loadConfiguration() {
        String selClass = getSelectedClass();
        if (selClass == null) {//nothing is selected
            return;
        }
        WredDefinition wredDefinition = configuration.get(selClass);
        if (wredDefinition == null) {//configuration was not saved before
            jSpinner1.setValue(0.002d);
            jSpinner2.setValue(0.2d);
            jSpinner3.setValue(0.8d);
            jSpinner4.setValue(0.2d);
            return;
        }
        jSpinner1.setValue(wredDefinition.getExponentialWeightFactor());
        jSpinner2.setValue(wredDefinition.getMinThreshold());
        jSpinner3.setValue(wredDefinition.getMaxThreshold());
        jSpinner4.setValue(wredDefinition.getMaxProbability());
    }

    private void selectTreeNode(int index) {
        if (rootNode.getChildCount() == 0) {//nothing to select
            return;
        }
        jTree1.setSelectionRow(index);
    }

    /**
     * saves configuration for currently displayed queue
     */
    private void saveConfiguration() {
        if (rootNode.getChildCount() == 0) {//nothing to save
            return;
        }
        String selClass = getSelectedClass();
        if (selClass == null) {//nothing is selected
            return;
        }
        WredDefinition definition = new WredDefinition(getClassDefinitionByName(selClass), getExponenetial(), getMaxThresh(), getMinThresh(), getMaxProb());
        configuration.put(selClass, definition);
    }

    private ClassDefinition getClassDefinitionByName(String className) {
        for (ClassDefinition def : classes) {
            if (def.getName().equals(className)) {
                return def;
            }
        }

        throw new IllegalArgumentException("unable to find class with name: " + className);

    }

    private double getExponenetial() {
        return (Double) jSpinner1.getValue();
    }

    private double getMinThresh() {
        return (Double) jSpinner2.getValue();
    }

    private double getMaxThresh() {
        return (Double) jSpinner3.getValue();
    }

    private double getMaxProb() {
        return (Double) jSpinner4.getValue();
    }

    /**
     * returns selected class in the tree
     *
     * @return null if nothing is selected
     */
    private String getSelectedClass() {
        if (jTree1.getLastSelectedPathComponent() == null) {
            return null;
        }
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) jTree1.getLastSelectedPathComponent();
        String selectedNodeName = selectedNode.toString();
        return selectedNodeName;
    }

    /**
     * shows text that informs user about recommended queue number
     *
     * @param queueCount
     */
    public final void setQueueCountLabel(int queueCount) {
        if (queueCount == -1) {
            jLabel7.setText(NbBundle.getMessage(WredQueueManagementDialog.class, "unable_to_predict_queue_count"));
        } else {
            jLabel7.setText(NbBundle.getMessage(WredQueueManagementDialog.class, "predicted_queue_count") + ": " + queueCount);
        }
    }

    private void addClass(String className) throws QosCreationException {
        if (classMapMap.containsKey(className)) {
            throw new QosCreationException("duplicite queue number " + className);
        }
        classMapMap.put(className, rootNode.getChildCount());
        treeModel.insertNodeInto(new DefaultMutableTreeNode(className), rootNode, rootNode.getChildCount());
    }

    private void removeClass(String className) throws QosCreationException {
        if (!classMapMap.containsKey(className)) {
            throw new QosCreationException("no queue with number " + className);
        }
        treeModel.removeNodeFromParent((MutableTreeNode) rootNode.getChildAt(classMapMap.get(className)));
        classMapMap.remove(className);

    }

    public Collection<WredDefinition> getConfiguration() {
        return configuration.values();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jSpinner1 = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        jSpinner2 = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();
        jSpinner3 = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        jSpinner4 = new javax.swing.JSpinner();
        jLabel6 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        jLabel7 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(WredQueueManagementDialog.class, "WredQueueManagementDialog.jLabel5.text")); // NOI18N

        jSpinner1.setModel(new javax.swing.SpinnerNumberModel(0.0020d, 0.0d, 1.0d, 0.1d));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(WredQueueManagementDialog.class, "WredQueueManagementDialog.jLabel4.text")); // NOI18N

        jSpinner2.setModel(new javax.swing.SpinnerNumberModel(0.2d, 0.0d, 1.0d, 0.1d));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(WredQueueManagementDialog.class, "WredQueueManagementDialog.jLabel2.text")); // NOI18N

        jSpinner3.setModel(new javax.swing.SpinnerNumberModel(0.8d, 0.0d, 1.0d, 0.1d));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(WredQueueManagementDialog.class, "WredQueueManagementDialog.jLabel3.text")); // NOI18N

        jSpinner4.setModel(new javax.swing.SpinnerNumberModel(0.2d, 0.0d, 1.0d, 0.1d));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(WredQueueManagementDialog.class, "WredQueueManagementDialog.jLabel6.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(WredQueueManagementDialog.class, "WredQueueManagementDialog.jLabel1.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 386, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel4)
                                .addComponent(jLabel3))
                            .addGap(48, 48, 48)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jSpinner3, javax.swing.GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE)
                                .addComponent(jSpinner4, javax.swing.GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE)))
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                    .addComponent(jLabel1)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(jLabel2)
                                    .addGap(58, 58, 58)))
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jSpinner2, javax.swing.GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE)
                                .addComponent(jSpinner1, javax.swing.GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE))))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel5)
                        .addComponent(jLabel6))
                    .addContainerGap()))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 247, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGap(77, 77, 77)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                        .addComponent(jLabel1)
                        .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel5))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                        .addComponent(jLabel2)
                        .addComponent(jSpinner2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                        .addComponent(jLabel3)
                        .addComponent(jSpinner3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                        .addComponent(jLabel4)
                        .addComponent(jSpinner4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel6))
                    .addContainerGap(54, Short.MAX_VALUE)))
        );

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        jTree1.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jTree1.setRootVisible(false);
        jScrollPane1.setViewportView(jTree1);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(WredQueueManagementDialog.class, "WredQueueManagementDialog.jLabel7.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButton3, org.openide.util.NbBundle.getMessage(WredQueueManagementDialog.class, "WredQueueManagementDialog.jButton3.text")); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addGap(0, 196, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(196, 196, 196)
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton3)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        saveConfiguration();
        this.setVisible(false);
    }//GEN-LAST:event_jButton3ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JSpinner jSpinner2;
    private javax.swing.JSpinner jSpinner3;
    private javax.swing.JSpinner jSpinner4;
    private javax.swing.JTree jTree1;
    // End of variables declaration//GEN-END:variables

    class SelectionListener implements TreeSelectionListener {

        @Override
        public void valueChanged(TreeSelectionEvent se) {
            JTree tree = (JTree) se.getSource();
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            String selectedNodeName = selectedNode.toString();

            //save previous configuration
            saveConfiguration();//todo test if saving and loading works as intended, because it looks like it saves and loads the same config
            //load new configuration
            loadConfiguration();

        }
    }
}
