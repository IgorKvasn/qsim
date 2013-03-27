/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.topology.qos;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.impl.DscpClassification;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.utils.dscp.DscpDefinition;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.utils.dscp.DscpManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.utils.dscp.DscpValuesEnum;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.topology.qos.supporting.DscpQueryDialog;

/**
 *
 * @author Igor Kvasnicka
 */
public class DscpClassificationDialog extends javax.swing.JDialog {

    private DefaultTableModel tableModel;

    public DscpClassificationDialog(JDialog parent) {
        super(parent, true);

        initComponents();
        errLabel.setVisible(false);
        tableModel = (DefaultTableModel) jTable1.getModel();



        TableColumn col = jTable1.getColumnModel().getColumn(0);
        ComboItem[] items = new ComboItem[DscpValuesEnum.values().length];
        for (int i = 0; i < items.length; i++) {
            DscpValuesEnum dscpEn = DscpValuesEnum.values()[i];
            items[i] = new ComboItem(dscpEn.getQosQueue(), dscpEn.getTextName());
        }

        col.setCellEditor(new MyComboBoxEditor(items));

        //init default class
        jComboBox1.removeAllItems();
        for (int i = 0; i < DscpValuesEnum.values().length; i++) {
            DscpValuesEnum dscpEn = DscpValuesEnum.values()[i];
            jComboBox1.addItem(new ComboItem(dscpEn.getQosQueue(), dscpEn.getTextName()));
        }
        setLocationRelativeTo(parent);
    }

    /**
     * used to load settings from already existing network node
     *
     * @param settings
     */
    public DscpClassificationDialog(JDialog parent, DscpManager dscpManager) {
        this(parent);

        //default queue
        String defaultQueueName = DscpClassification.findDscpValueByQueueNumber(dscpManager.getNotDefinedQueue()).getTextName();
        for (int i = 0; i < jComboBox1.getItemCount(); i++) {
            if (((ComboItem) jComboBox1.getItemAt(i)).getLabel().equals(defaultQueueName)) {
                jComboBox1.setSelectedIndex(i);
                break;
            }
        }
        //dscp queries
        for (DscpDefinition def : dscpManager.getDefinitions()) {
            tableModel.addRow(new Object[]{new ComboItem(def.getQueueNumber(),DscpClassification.findDscpValueByQueueNumber(def.getQueueNumber()).getTextName()), def.getQuery()});
        }
    }

    private void showQueryDialog(int row) {
        DscpQueryDialog queryDialog = new DscpQueryDialog(this, (String) jTable1.getValueAt(row, 1));
        queryDialog.setVisible(true);

        String result = queryDialog.getDscpQuery();
        if (result == null) {//user hit cancel
            return;
        }

        if (StringUtils.isEmpty(result)) {
            setQueryString(row, "");
        } else {
            setQueryString(row, result);
        }
    }

    private void setQueryString(int row, String query) {
        jTable1.setValueAt(query, row, 1);
    }

    /**
     * returns validated result of DSCP configuration
     *
     * @return
     */
    public List<DscpDefinition> getDscpDefinitions() {
        List<DscpDefinition> result = new LinkedList<DscpDefinition>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            DscpDefinition res = new DscpDefinition((String) jTable1.getValueAt(i, 1), ((ComboItem) tableModel.getValueAt(i, 0)).getValue());
            result.add(res);
        }
        return result;
    }

    public DscpValuesEnum getDefaultQueueNumber() {
        return DscpClassification.findDscpValueByQueueNumber(((ComboItem) jComboBox1.getSelectedItem()).value);
    }

    /**
     * validates user input in case of some error it also shows error label with
     * error description
     *
     * @return true if everything is OK, false otherwise
     */
    private boolean validateInput() {
        if (jTable1.getRowCount() == 0) {
            showErrorLabel(NbBundle.getMessage(DscpClassificationDialog.class, "no_dscp"));
            return false;
        }

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            try {
                ComboItem a = (ComboItem) tableModel.getValueAt(i, 0);
                if (a == null) {
                    throw new ClassCastException(); //not the best solution to throw unrelated exception, but catch block is already written...
                }
                String s = (String) jTable1.getValueAt(i, 1);
                if (StringUtils.isEmpty(s)) {
                    throw new ClassCastException();
                }
            } catch (ClassCastException e) {
                showErrorLabel(NbBundle.getMessage(DscpClassificationDialog.class, "invalid_dscp_setting"));
                return false;
            }
        }
        return true;
    }

    private void showErrorLabel(String s) {
        errLabel.setText(s);
        errLabel.setVisible(true);
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
        jLabel1 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        errLabel = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();

        setMinimumSize(new java.awt.Dimension(583, 511));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Queue #", "ACL query"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.String.class
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
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);
        jTable1.getColumnModel().getColumn(0).setMinWidth(60);
        jTable1.getColumnModel().getColumn(0).setPreferredWidth(70);
        jTable1.getColumnModel().getColumn(0).setMaxWidth(90);
        jTable1.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(DscpClassificationDialog.class, "DscpClassificationDialog.jTable1.columnModel.title0")); // NOI18N
        jTable1.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(DscpClassificationDialog.class, "DscpClassificationDialog.jTable1.columnModel.title1")); // NOI18N

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/add.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(DscpClassificationDialog.class, "DscpClassificationDialog.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/remove.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(DscpClassificationDialog.class, "DscpClassificationDialog.jButton2.text")); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(DscpClassificationDialog.class, "DscpClassificationDialog.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButton3, org.openide.util.NbBundle.getMessage(DscpClassificationDialog.class, "DscpClassificationDialog.jButton3.text")); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        errLabel.setForeground(new java.awt.Color(255, 0, 0));
        org.openide.awt.Mnemonics.setLocalizedText(errLabel, org.openide.util.NbBundle.getMessage(DscpClassificationDialog.class, "DscpClassificationDialog.errLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(DscpClassificationDialog.class, "DscpClassificationDialog.jLabel2.text")); // NOI18N

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(153, 153, 153)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 375, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jButton1)
                                    .addComponent(jButton2))
                                .addGap(53, 53, 53))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addComponent(errLabel)))))
                .addContainerGap(23, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7)
                .addComponent(errLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton2))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addComponent(jButton3)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
        if (jTable1.getSelectedColumn() != 1) {
            return;
        }
        showQueryDialog(jTable1.getSelectedRow());
    }//GEN-LAST:event_jTable1MouseClicked

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        tableModel.addRow(new Object[2]);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        if (jTable1.getSelectedRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "No DSCP query selected.",
                    "Unable to delete",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        tableModel.removeRow(jTable1.getSelectedRow());
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        if (validateInput()) {
            this.setVisible(false);
        }
    }//GEN-LAST:event_jButton3ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel errLabel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables

    private static class ComboItem {

        @Getter
        private int value;
        @Getter
        private String label;

        public ComboItem(int value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private class MyComboBoxEditor extends DefaultCellEditor {

        private MyComboBoxEditor(ComboItem[] items) {
            super(new JComboBox(items));
        }
    }
}
