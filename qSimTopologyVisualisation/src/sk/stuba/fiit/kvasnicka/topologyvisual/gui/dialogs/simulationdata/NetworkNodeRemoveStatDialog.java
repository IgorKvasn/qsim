/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.simulationdata;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import lombok.Getter;
import org.jdesktop.swingx.JXSearchField;
import org.openide.windows.WindowManager;
import sk.stuba.fiit.kvasnicka.topologyvisual.filetype.gui.TopologyVisualisation;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.simulationdata.NetworkNodeStatisticsTopComponent;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.simulationdata.NetworkNodeStatisticsTopComponent.MonitoringNode;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.simulationdata.NetworkNodeStatisticsTopComponent.NetworkNodePropertyEnum;

/**
 *
 * @author Igor Kvasnicka
 */
public class NetworkNodeRemoveStatDialog extends javax.swing.JDialog {

    private NetworkNodeStatisticsTopComponent statisticsTopComponent;
    private TopologyVisualisation topologyVisualisation;
    private DefaultListModel listModel = new DefaultListModel();
    private ListSelectionModel listSelectionModel;
    private Set<AddRemove> nodesToRemoveSet = new HashSet<AddRemove>();
    private RowFilter<ListModel, Object> listFilter;

    /**
     * Creates new form NetworkNodeRemoveStatDialog
     */
    public NetworkNodeRemoveStatDialog(NetworkNodeStatisticsTopComponent statisticsTopComponent, TopologyVisualisation topologyVisualisation) {
        super(WindowManager.getDefault().getMainWindow(), true);
        initComponents();
        this.statisticsTopComponent = statisticsTopComponent;
        this.topologyVisualisation = topologyVisualisation;
        jXList1.setModel(listModel);
        listSelectionModel = jXList1.getSelectionModel();
        listSelectionModel.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                listItemSelected();
            }
        });

        jXSearchField1.setSearchMode(JXSearchField.SearchMode.INSTANT);
        jXSearchField1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterList(e.getActionCommand());
            }
        });
    }

    private void filterList(String text) {
        listFilter = RowFilter.regexFilter(text);
        jXList1.setRowFilter(listFilter);
    }

    public void showDialog(Set<MonitoringNode> traceSet) {
        resetData(traceSet);
        setVisible(true);

        if (!traceSet.isEmpty()) {//there is at least one vertex
            jXList1.setSelectedIndex(0);//select the first vertex
            listItemSelected();
        }
    }

    private void resetData(Set<MonitoringNode> traceMap) {
        jXSearchField1.setText("");
        jXList1.setRowFilter(null);


        listModel.clear();
        for (MonitoringNode m : traceMap) {
            listModel.addElement(new ListItem(m.getVertex().getName(), m));
        }
    }

    private MonitoringNode getSelectedMonitoringNode() {
        if (jXList1.getSelectedValue() == null) {//nothing is selected
            return null;
        }
        return (MonitoringNode) jXList1.getSelectedValue();

    }

    private void listItemSelected() {
        MonitoringNode monitoringNode = getSelectedMonitoringNode();
        if (monitoringNode == null) {
            return;
        }
        updateProperties(monitoringNode);
    }

    private void updateProperties(MonitoringNode monitoringNode) {
        Set<NetworkNodePropertyEnum> propertyEnumSet = monitoringNode.getPropertyEnumSet();

        chckInput.setSelected(propertyEnumSet.contains(NetworkNodePropertyEnum.INPUT_BUFFER));
        chckOutput.setSelected(propertyEnumSet.contains(NetworkNodePropertyEnum.OUTPUT_BUFFER));

        chckProcessing.setSelected(propertyEnumSet.contains(NetworkNodePropertyEnum.PROCESSING));

        chckRX.setSelected(propertyEnumSet.contains(NetworkNodePropertyEnum.RX));
        chckTX.setSelected(propertyEnumSet.contains(NetworkNodePropertyEnum.TX));
    }

    private void nodePropertyModified(NetworkNodePropertyEnum propertyEnum, boolean selected) {
        MonitoringNode monitoringNode = getSelectedMonitoringNode();

        if (getOriginalValue(monitoringNode, propertyEnum) == selected) {//this is the original value
            nodesToRemoveSet.remove(new AddRemove(monitoringNode.getVertex(), propertyEnum));
            return;
        }

        AddRemove addRemove = new AddRemove(monitoringNode.getVertex(), propertyEnum);
        nodesToRemoveSet.add(addRemove);
    }

    private boolean getOriginalValue(MonitoringNode monitoringNode, NetworkNodePropertyEnum propertyEnum) {
        Set<NetworkNodePropertyEnum> propertyEnumSet = monitoringNode.getPropertyEnumSet();
        return propertyEnumSet.contains(propertyEnum);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jXSearchField1 = new org.jdesktop.swingx.JXSearchField();
        jScrollPane2 = new javax.swing.JScrollPane();
        jXList1 = new org.jdesktop.swingx.JXList();
        jPanel1 = new javax.swing.JPanel();
        chckRX = new javax.swing.JCheckBox();
        chckTX = new javax.swing.JCheckBox();
        chckInput = new javax.swing.JCheckBox();
        chckOutput = new javax.swing.JCheckBox();
        chckProcessing = new javax.swing.JCheckBox();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(NetworkNodeRemoveStatDialog.class, "NetworkNodeRemoveStatDialog.jPanel2.border.title"))); // NOI18N

        jXSearchField1.setText(org.openide.util.NbBundle.getMessage(NetworkNodeRemoveStatDialog.class, "NetworkNodeRemoveStatDialog.jXSearchField1.text")); // NOI18N
        jXSearchField1.setToolTipText(org.openide.util.NbBundle.getMessage(NetworkNodeRemoveStatDialog.class, "NetworkNodeRemoveStatDialog.jXSearchField1.toolTipText")); // NOI18N
        jXSearchField1.setLayoutStyle(org.jdesktop.swingx.JXSearchField.LayoutStyle.VISTA);

        jXList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane2.setViewportView(jXList1);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jXSearchField1, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addComponent(jXSearchField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(NetworkNodeRemoveStatDialog.class, "NetworkNodeRemoveStatDialog.jPanel1.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(chckRX, org.openide.util.NbBundle.getMessage(NetworkNodeRemoveStatDialog.class, "NetworkNodeRemoveStatDialog.chckRX.text")); // NOI18N
        chckRX.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chckRXActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(chckTX, org.openide.util.NbBundle.getMessage(NetworkNodeRemoveStatDialog.class, "NetworkNodeRemoveStatDialog.chckTX.text")); // NOI18N
        chckTX.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chckTXActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(chckInput, org.openide.util.NbBundle.getMessage(NetworkNodeRemoveStatDialog.class, "NetworkNodeRemoveStatDialog.chckInput.text")); // NOI18N
        chckInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chckInputActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(chckOutput, org.openide.util.NbBundle.getMessage(NetworkNodeRemoveStatDialog.class, "NetworkNodeRemoveStatDialog.chckOutput.text")); // NOI18N
        chckOutput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chckOutputActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(chckProcessing, org.openide.util.NbBundle.getMessage(NetworkNodeRemoveStatDialog.class, "NetworkNodeRemoveStatDialog.chckProcessing.text")); // NOI18N
        chckProcessing.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chckProcessingActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chckProcessing)
                    .addComponent(chckOutput)
                    .addComponent(chckInput)
                    .addComponent(chckTX)
                    .addComponent(chckRX))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chckRX)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chckTX)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chckInput)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chckOutput)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chckProcessing)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(NetworkNodeRemoveStatDialog.class, "NetworkNodeRemoveStatDialog.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(NetworkNodeRemoveStatDialog.class, "NetworkNodeRemoveStatDialog.jButton2.text")); // NOI18N
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
                        .addGap(138, 138, 138)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        statisticsTopComponent.removeNetworkNodes(nodesToRemoveSet);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void chckRXActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chckRXActionPerformed
        nodePropertyModified(NetworkNodePropertyEnum.RX, chckRX.isSelected());
    }//GEN-LAST:event_chckRXActionPerformed

    private void chckTXActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chckTXActionPerformed
        nodePropertyModified(NetworkNodePropertyEnum.TX, chckTX.isSelected());

    }//GEN-LAST:event_chckTXActionPerformed

    private void chckInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chckInputActionPerformed
        nodePropertyModified(NetworkNodePropertyEnum.INPUT_BUFFER, chckInput.isSelected());

    }//GEN-LAST:event_chckInputActionPerformed

    private void chckOutputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chckOutputActionPerformed
        nodePropertyModified(NetworkNodePropertyEnum.OUTPUT_BUFFER, chckOutput.isSelected());

    }//GEN-LAST:event_chckOutputActionPerformed

    private void chckProcessingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chckProcessingActionPerformed
        nodePropertyModified(NetworkNodePropertyEnum.PROCESSING, chckProcessing.isSelected());
    }//GEN-LAST:event_chckProcessingActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox chckInput;
    private javax.swing.JCheckBox chckOutput;
    private javax.swing.JCheckBox chckProcessing;
    private javax.swing.JCheckBox chckRX;
    private javax.swing.JCheckBox chckTX;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private org.jdesktop.swingx.JXList jXList1;
    private org.jdesktop.swingx.JXSearchField jXSearchField1;
    // End of variables declaration//GEN-END:variables

    @Getter
    public static class AddRemove {

        private TopologyVertex v;
        private NetworkNodePropertyEnum propertyEnum;

        public AddRemove(TopologyVertex v, NetworkNodePropertyEnum propertyEnum) {
            this.v = v;
            this.propertyEnum = propertyEnum;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 53 * hash + (this.v != null ? this.v.hashCode() : 0);
            hash = 53 * hash + (this.propertyEnum != null ? this.propertyEnum.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final AddRemove other = (AddRemove) obj;
            if (this.v != other.v && (this.v == null || !this.v.equals(other.v))) {
                return false;
            }
            if (this.propertyEnum != other.propertyEnum) {
                return false;
            }
            return true;
        }
    }

    @Getter
    private class ListItem {

        private String label;
        private Object value;

        public ListItem(String label, Object value) {
            this.label = label;
            this.value = value;
        }
    }
}
