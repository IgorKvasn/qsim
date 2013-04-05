/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.topology.qos;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.impl.DscpClassification;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.utils.dscp.DscpValuesEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.utils.ClassDefinition;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.utils.ParameterException;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.utils.QosUtils;

/**
 *
 * @author Igor Kvasnicka
 */
public class ClassDefinitionDialog extends javax.swing.JDialog {

    @Getter
    private ClassDefinition[] classes;
    private boolean isDscp;
    private DefaultTableModel tableModel;

    public ClassDefinitionDialog(JDialog parent, Set<Integer> queues, boolean isDscp) {
        super(parent, true);
        this.isDscp = isDscp;
        init();
        setLocationRelativeTo(parent);
    }

    public ClassDefinitionDialog(JDialog parent, ClassDefinition[] classes) {
        super(parent, true);
        init();

        for (ClassDefinition def : classes) {
            addClass(def.getName(), def.getQueueNumbers());
        }
        setLocationRelativeTo(parent);
    }

    private void init() {
        initComponents();
        tableModel = (DefaultTableModel) jTable1.getModel();
    }

    private void addClass(String className, List<Integer> queueNumbers) {
        for (int queue : queueNumbers) {
            String label;
            if (isDscp) {
                label = DscpClassification.findDscpValueByQueueNumber(queue).getTextName();
            } else {
                label = String.valueOf(queue);
            }
            addTableRow(label, className);
        }
    }

    private void addTableRow(String queue, String clazz) {
        tableModel.addRow(new String[]{queue, clazz});
    }

    private void addClassDialog(int row) {
        String name = JOptionPane.showInputDialog(this, "QoS class name:");

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
        tableModel.setValueAt(name, row, 1);
    }

    /**
     * detects if class name is unique
     *
     * @param name
     * @return
     */
    private boolean isClassNameUnique(String name) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String nodeName = (String) tableModel.getValueAt(i, 1);
            if (StringUtils.isEmpty(name)) {
                continue;
            }

            if (nodeName.equals(name)) {
                return false;
            }
        }
        return true;
    }

    private List<Integer> findQueuesForClass(String className) {
        List<Integer> list = new LinkedList<Integer>();
        int queueNumber;
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            if (className.equals((String) tableModel.getValueAt(row, 1))) {
                if (isDscp) {
                    queueNumber = DscpValuesEnum.valueOf((String) tableModel.getValueAt(row, 0)).getQosQueue();
                } else {
                    queueNumber = (Integer) tableModel.getValueAt(row, 0);
                }
                list.add(queueNumber);
            }
        }
        return list;
    }

    private ClassDefinition[] makeClasses() throws ParameterException {

        List<ClassDefinition> classesList = new LinkedList<ClassDefinition>();
        Set<String> definedClasses = new HashSet<String>();

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String className = (String) tableModel.getValueAt(i, 1);
            if (definedClasses.contains(className)) { //this class was alredy processed
                continue;
            }

            List<Integer> list = findQueuesForClass(className);
            if (list.isEmpty()) {//emtpy classes will be ignored
                continue;
            }
            ClassDefinition def = new ClassDefinition(list, className);
            classesList.add(def);

            definedClasses.add(className);

        }
        //check for problems
        ClassDefinition[] result = classesList.toArray(new ClassDefinition[classesList.size()]);
        QosUtils.checkClassDefinition(result);
        
        return result;
    }
    
    public void showDialog(){
        setVisible(true);
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
        jTable1 = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(ClassDefinitionDialog.class, "ClassDefinitionDialog.title")); // NOI18N

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Queue #", "Class"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);
        jTable1.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(ClassDefinitionDialog.class, "ClassDefinitionDialog.jTable1.columnModel.title0")); // NOI18N
        jTable1.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(ClassDefinitionDialog.class, "ClassDefinitionDialog.jTable1.columnModel.title1")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ClassDefinitionDialog.class, "ClassDefinitionDialog.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(ClassDefinitionDialog.class, "ClassDefinitionDialog.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

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
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 375, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(68, 68, 68)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(84, 84, 84)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(43, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 35, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addContainerGap())
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

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
        if (jTable1.getSelectedColumn() != 1) {
            return;
        }
        addClassDialog(jTable1.getSelectedRow());
    }//GEN-LAST:event_jTable1MouseClicked

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_jButton2ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
