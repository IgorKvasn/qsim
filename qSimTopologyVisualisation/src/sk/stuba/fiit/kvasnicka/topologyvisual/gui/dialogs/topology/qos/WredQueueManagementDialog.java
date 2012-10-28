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
import sk.stuba.fiit.kvasnicka.topologyvisual.exceptions.QosCreationException;

/**
 *
 * @author Igor Kvasnicka
 */
public class WredQueueManagementDialog extends javax.swing.JDialog {

    
    
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;
    private Map<Integer, Integer> queueMap;//key=queue number; value=tree node index
    private Map<Integer, WredDefinition> configuration = new HashMap<Integer, WredDefinition>(); //key =queue number; value=WredDefinition

    /**
     * Creates new form WredQueueManagementDialog
     */
    public WredQueueManagementDialog(JDialog owner, int queueCount) {
        super(owner, true);
        init();
        setQueueCountLabel(queueCount);
        createQueues(queueCount);
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
        
        
    }

    private void init() {
        initComponents();
        treeModel = (DefaultTreeModel) jTree1.getModel();
        queueMap = new HashMap<Integer, Integer>();
        jTree1.addTreeSelectionListener(new SelectionListener());
        selectTreeNode(0);
    }

    /**
     * initially create queues
     *
     * @param queueCount
     */
    private void createQueues(int queueCount) {
        try {
            if (queueCount == -1) {
                //create default queue
                addQueue(-1);
            } else {
                for (int i = 0; i < queueCount; i++) {
                    addQueue(i);
                }
            }
        } catch (QosCreationException ex) {
            Exceptions.printStackTrace(ex);//this should not happen
        }
    }

    private void loadConfiguration() {
        int queue = getSelectedQueue();
        if (queue == -2) {//nothing is selected
            return;
        }
        WredDefinition wredDefinition = configuration.get(queue);
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
        int queue = getSelectedQueue();
        if (queue == -2) {//nothing is selected
            return;
        }
        WredDefinition definition = new WredDefinition(queue, getExponenetial(), getMaxThresh(), getMinThresh(), getMaxProb());

        configuration.put(queue, definition);
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
     * returns selected queue in the tree
     *
     * @return -2 if nothing is selected
     */
    private int getSelectedQueue() {
        if (jTree1.getLastSelectedPathComponent() == null) {
            return -2;
        }
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) jTree1.getLastSelectedPathComponent();
        String selectedNodeName = selectedNode.toString();
        return Integer.parseInt(selectedNodeName);
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

    private void addQueue(int queueNumber) throws QosCreationException {
        if (queueMap.containsKey(queueNumber)) {
            throw new QosCreationException("duplicite queue number " + queueNumber);
        }
        queueMap.put(queueNumber, rootNode.getChildCount());
        treeModel.insertNodeInto(new DefaultMutableTreeNode(queueNumber), rootNode, rootNode.getChildCount());
    }

    private void removeQueue(int queueNumber) throws QosCreationException {
        if (!queueMap.containsKey(queueNumber)) {
            throw new QosCreationException("no queue with number " + queueNumber);
        }
        treeModel.removeNodeFromParent((MutableTreeNode) rootNode.getChildAt(queueMap.get(queueNumber)));
        queueMap.remove(queueNumber);

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
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
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

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(WredQueueManagementDialog.class, "WredQueueManagementDialog.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(WredQueueManagementDialog.class, "WredQueueManagementDialog.jButton2.text")); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel8, org.openide.util.NbBundle.getMessage(WredQueueManagementDialog.class, "WredQueueManagementDialog.jLabel8.text")); // NOI18N

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
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jButton1)
                                .addGap(18, 18, 18)
                                .addComponent(jButton2)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(jLabel8))
                        .addGap(0, 0, Short.MAX_VALUE)))
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
                .addGap(9, 9, 9)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton1)
                            .addComponent(jButton2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton3)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        String s = (String) JOptionPane.showInputDialog(
                this,
                "Insert queue number you want to add",
                "Add new queue",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                rootNode.getChildCount());

        try {
            if (Integer.parseInt(s) < -1) {
                throw new NumberFormatException();
            }
            addQueue(Integer.parseInt(s));
            selectTreeNode(rootNode.getChildCount() - 1);//select new node
        } catch (QosCreationException e) {
            JOptionPane.showMessageDialog(this,
                    "Configuration for this queue already exists.",
                    "Queue number error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Please enter number (positive integer value) or -1 or default settings",
                    "Numeric error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        String s = (String) JOptionPane.showInputDialog(
                this,
                "Insert queue number you want to add",
                "Remove queue",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                rootNode.getChildCount());

        try {
            removeQueue(Integer.parseInt(s));
            selectTreeNode(0);//select first node
        } catch (QosCreationException e) {
            JOptionPane.showMessageDialog(this,
                    "Configuration for this queue does not exist.",
                    "Queue number error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Please enter number (positive integer value)",
                    "Numeric error",
                    JOptionPane.ERROR_MESSAGE);
        }    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        saveConfiguration();
        this.setVisible(false);
    }//GEN-LAST:event_jButton3ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
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
            saveConfiguration();
            //load new configuration
            loadConfiguration();

        }
    }
}
