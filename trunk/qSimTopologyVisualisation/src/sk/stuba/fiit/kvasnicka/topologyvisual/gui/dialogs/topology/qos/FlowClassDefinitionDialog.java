/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.topology.qos;

import java.util.LinkedList;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import sk.stuba.fiit.kvasnicka.qsimsimulation.exceptions.ClassDefinitionException;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.utils.FlowClassDefinition;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.topology.qos.supporting.DscpQueryDialog;

/**
 *
 * @author Igor Kvasnicka
 */
public class FlowClassDefinitionDialog extends javax.swing.JDialog {

    @Getter
    private FlowClassDefinition[] classes;
    private DefaultTableModel tableModel;

    /**
     * Creates new form FlowClassDefinitionDialog
     */
    public FlowClassDefinitionDialog(JDialog parent, FlowClassDefinition[] classes) {
        super(parent, true);
        initComponents();
        tableModel = (DefaultTableModel) jTable1.getModel();

        for (FlowClassDefinition def : classes) {
            addRow(def.getName(), def.getAcl());
        }
        setLocationRelativeTo(parent);
    }

    /**
     * Creates new form FlowClassDefinitionDialog
     */
    public FlowClassDefinitionDialog(JDialog parent) {
        super(parent, true);
        initComponents();
        tableModel = (DefaultTableModel) jTable1.getModel();

        setLocationRelativeTo(parent);
    }

    private void addRow(String name, String acl) {
        tableModel.addRow(new String[]{name, acl});
    }

    public void showDialog() {
        setVisible(true);
    }

    private void showQueryDialog(int row) {
        DscpQueryDialog queryDialog = new DscpQueryDialog(this, (String) jTable1.getValueAt(row, 1));
        queryDialog.setVisible(true);

        String result = queryDialog.getDscpQuery();
        if (result == null) {//user hit cancel
            return;
        }

        try {
            FlowClassDefinition.checkForbiddenFields(result);
        } catch (ClassDefinitionException e) {
            JOptionPane.showMessageDialog(this,
                    "These ACLs are allowed to manupilate with source/destination ports or vertices.",
                    "ACL error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (StringUtils.isEmpty(result)) {
            jTable1.setValueAt("", row, 1);
        } else {
            jTable1.setValueAt(result, row, 1);
        }
    }

    private FlowClassDefinition[] createClasses() throws ClassDefinitionException {
        List<FlowClassDefinition> classesList = new LinkedList<FlowClassDefinition>();

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String className = (String) tableModel.getValueAt(i, 1);
            String acl = (String) tableModel.getValueAt(i, 1);

            FlowClassDefinition def = new FlowClassDefinition(className, acl);
            FlowClassDefinition.checkForbiddenFields(acl);
            classesList.add(def);
        }
        //check for problems
        FlowClassDefinition[] result = classesList.toArray(new FlowClassDefinition[classesList.size()]);

        return result;
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
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(FlowClassDefinitionDialog.class, "FlowClassDefinitionDialog.title")); // NOI18N

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "ACL"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                true, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.setColumnSelectionAllowed(true);
        jTable1.getTableHeader().setReorderingAllowed(false);
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);
        jTable1.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jTable1.getColumnModel().getColumn(0).setMinWidth(50);
        jTable1.getColumnModel().getColumn(0).setPreferredWidth(50);
        jTable1.getColumnModel().getColumn(0).setMaxWidth(50);
        jTable1.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(FlowClassDefinitionDialog.class, "FlowClassDefinitionDialog.jTable1.columnModel.title0")); // NOI18N
        jTable1.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(FlowClassDefinitionDialog.class, "FlowClassDefinitionDialog.jTable1.columnModel.title1")); // NOI18N

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/add.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(FlowClassDefinitionDialog.class, "FlowClassDefinitionDialog.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/remove.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(FlowClassDefinitionDialog.class, "FlowClassDefinitionDialog.jButton2.text")); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButton3, org.openide.util.NbBundle.getMessage(FlowClassDefinitionDialog.class, "FlowClassDefinitionDialog.jButton3.text")); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButton4, org.openide.util.NbBundle.getMessage(FlowClassDefinitionDialog.class, "FlowClassDefinitionDialog.jButton4.text")); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 375, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton2, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(21, 21, 21))
            .addGroup(layout.createSequentialGroup()
                .addGap(105, 105, 105)
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(79, 79, 79)
                        .addComponent(jButton1)
                        .addGap(18, 18, 18)
                        .addComponent(jButton2))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(32, 32, 32)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton3)
                    .addComponent(jButton4))
                .addContainerGap(25, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
        if (jTable1.getSelectedColumn() != 1) {
            return;
        }
        showQueryDialog(jTable1.getSelectedRow());
    }//GEN-LAST:event_jTable1MouseClicked

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        addRow("", "");
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        try {
            classes = createClasses();
            this.setVisible(false);
        } catch (ClassDefinitionException e) {
            JOptionPane.showMessageDialog(this,
                    "These ACLs are allowed to manupilate with source/destination ports or vertices.",
                    "ACL error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        if (jTable1.getSelectedRowCount() != 1) {
            return;
        }
        tableModel.removeRow(jTable1.getSelectedRow());
    }//GEN-LAST:event_jButton2ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
