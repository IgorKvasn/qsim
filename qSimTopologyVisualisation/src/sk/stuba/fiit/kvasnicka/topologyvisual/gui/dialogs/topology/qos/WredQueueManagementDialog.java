/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.topology.qos;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.openide.util.NbBundle;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.queuemanagement.impl.WeightedRED.WredDefinition;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.utils.ClassDefinition;

/**
 *
 * @author Igor Kvasnicka
 */
public class WredQueueManagementDialog extends javax.swing.JDialog {

    private DefaultListModel listModel;
    private Map<String, WredDefinition> configuration = new HashMap<String, WredDefinition>(); //key =class name; value=WredDefinition
    private ClassDefinition[] classes;
    private Map<String, WredDefinition> backupConfiguration = new HashMap<String, WredDefinition>(); //key =class name; value=WredDefinition

    /**
     * Creates new form WredQueueManagementDialog
     */
    public WredQueueManagementDialog(JDialog owner, ClassDefinition[] classes) {
        super(owner, true);
        init();
        this.classes = classes;
        addClassesToList(classes);
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
        saveBackupConfiguration();

        classes = new ClassDefinition[params.length];


        for (int i = 0; i < params.length; i++) {
            classes[i] = params[i].getQosClass();
        }

        addClassesToList(classes);
    }

    /**
     * makes deep copy of existing configuration
     */
    private void saveBackupConfiguration() {
        backupConfiguration.clear();
        for (Map.Entry<String, WredDefinition> e : configuration.entrySet()) {
            backupConfiguration.put(e.getKey(), new WredDefinition(e.getValue().getQosClass(), e.getValue().getExponentialWeightFactor(), e.getValue().getMaxThreshold(), e.getValue().getMinThreshold(), e.getValue().getMaxProbability()));
        }
    }

    private void loadBackupConfiguration() {
        configuration.clear();
        for (Map.Entry<String, WredDefinition> e : backupConfiguration.entrySet()) {
            configuration.put(e.getKey(), new WredDefinition(e.getValue().getQosClass(), e.getValue().getExponentialWeightFactor(), e.getValue().getMaxThreshold(), e.getValue().getMinThreshold(), e.getValue().getMaxProbability()));
        }
        backupConfiguration.clear();//to make garbage collector happy :)
    }

    private void init() {
        initComponents();
        listModel = new DefaultListModel();
        jList1.setModel(listModel);
        jList1.addListSelectionListener(new SelectionListener());
        selectClass(0);
    }

    /**
     * initially create QoS classes in List
     *
     * @param queueCount
     */
    private void addClassesToList(ClassDefinition[] classes) {
        for (int i = 0; i < classes.length; i++) {
            addClass(classes[i]);
        }
    }

    private void loadConfiguration() {
        ListItem selClass = getSelectedClass();
        if (selClass == null) {//nothing is selected
            return;
        }
        WredDefinition wredDefinition = configuration.get(selClass.def.getName());
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

    private void selectClass(int index) {
        jList1.setSelectedIndex(index);
    }

    /**
     * saves configuration for currently displayed queue
     */
    private void saveConfiguration() {
        ListItem selClass = getSelectedClass();
        if (selClass == null) {//nothing is selected
            return;
        }
        WredDefinition definition = new WredDefinition(selClass.def, getExponenetial(), getMaxThresh(), getMinThresh(), getMaxProb());
        configuration.put(selClass.def.getName(), definition);
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
    private ListItem getSelectedClass() {
        if (jList1.getSelectedIndex() == -1) {
            return null;
        }
        return (ListItem) jList1.getSelectedValue();
    }

    private void addClass(ClassDefinition classDefinition) {
        listModel.addElement(new ListItem(classDefinition));
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
        jLabel7 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jButton1 = new javax.swing.JButton();

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

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(WredQueueManagementDialog.class, "WredQueueManagementDialog.jLabel7.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButton3, org.openide.util.NbBundle.getMessage(WredQueueManagementDialog.class, "WredQueueManagementDialog.jButton3.text")); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jList1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane2.setViewportView(jList1);

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(WredQueueManagementDialog.class, "WredQueueManagementDialog.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
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
                        .addComponent(jLabel7)
                        .addGap(0, 196, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane2)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton3)
                    .addComponent(jButton1))
                .addContainerGap(18, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        saveConfiguration();
        this.setVisible(false);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        configuration.clear();
        loadBackupConfiguration();

        setVisible(false);

    }//GEN-LAST:event_jButton1ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JList jList1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JSpinner jSpinner2;
    private javax.swing.JSpinner jSpinner3;
    private javax.swing.JSpinner jSpinner4;
    // End of variables declaration//GEN-END:variables

    class SelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (getSelectedClass() == null) {
                return;
            }

            //save previous configuration
            saveConfiguration();//todo test if saving and loading works as intended, because it looks like it saves and loads the same config
            //load new configuration
            loadConfiguration();

        }
    }

    private class ListItem {

        private ClassDefinition def;

        public ListItem(ClassDefinition def) {
            this.def = def;
        }

        @Override
        public String toString() {
            return def.getName();
        }
    }
}
