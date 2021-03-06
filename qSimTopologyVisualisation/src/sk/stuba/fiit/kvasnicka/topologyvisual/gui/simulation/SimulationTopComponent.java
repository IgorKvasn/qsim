/*
 * This file is part of qSim.
 *
 * qSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * qSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with qSim.  If not, see <http://www.gnu.org/licenses/>.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation;

import java.awt.Component;
import java.util.LinkedList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.facade.SimulationFacade;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.NetbeansWindowHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.wizard.SimulationRuleIterator;
import sk.stuba.fiit.kvasnicka.topologyvisual.utils.SimulationData;
import sk.stuba.fiit.kvasnicka.topologyvisual.utils.SimulationData.Data;

/**
 * Top component which displays something.
 */
//@ConvertAsProperties(dtd = "-//sk.stuba.fiit.kvasnicka.topologyvisual.gui//Simulation//EN",
//autostore = false)
@TopComponent.Description(preferredID = "SimulationTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "myoutput", openAtStartup = false)
@ActionID(category = "Window", id = "sk.stuba.fiit.kvasnicka.topologyvisual.gui.SimulationTopComponent")
@ActionReference(path = "Menu/Window" /*
 * , position = 333
 */)
//@TopComponent.OpenActionRegistration(displayName = "#CTL_SimulationAction",
//preferredID = "SimulationTopComponent")
@Messages({
    "CTL_SimulationAction=Simulation",
    "CTL_SimulationTopComponent=Simulation rules overview",
    "HINT_SimulationTopComponent=Simulation rules overview",
    "not_selected_row_error=No row has been selected."
})
public final class SimulationTopComponent extends TopComponent {

    private static Logger logg = Logger.getLogger(SimulationTopComponent.class);
    private RowFilter<TableModel, Object> sourceFilter = null;
    private RowFilter<TableModel, Object> destinationFilter = null;
    private RowFilter<TableModel, Object> nameFilter = null;
    private List<RowFilter<TableModel, Object>> filters = new LinkedList<RowFilter<TableModel, Object>>();
    private RowFilter<TableModel, Object> compoundRowFilter = null;
    private TableRowSorter<TableModel> sorterSimRules;
    private DefaultTableModel tableModel;
    private final SimulationFacade simulationFacade;
    @Getter
    private AddSimulationTopComponent addSimulationTopComponent;
    private final SimulationData simulationData;

    public SimulationTopComponent(SimulationFacade simulationFacade, SimulationData simulationData) {
        initComponents();

        setName(Bundle.CTL_SimulationTopComponent());
        setToolTipText(Bundle.HINT_SimulationTopComponent());
        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);

        tableModel = ((DefaultTableModel) jXTable1.getModel());
        sorterSimRules = new TableRowSorter<TableModel>(jXTable1.getModel());
        /**
         * the first column (index=0) is a hidden column containing ID of a Data
         * object
         */
        jXTable1.removeColumn(jXTable1.getColumnModel().getColumn(0));
        this.simulationFacade = simulationFacade;
        this.simulationData = simulationData;
        
        loadSimulationRules();
    }

    /**
     * opens AddSimulationTopcompnent window it also includes workaround for bug
     * #208059: http://netbeans.org/bugzilla/show_bug.cgi?id=208059
     */
    private void addSimulation() {
        if (addSimulationTopComponent == null) {
            throw new IllegalStateException("addSimulationTopComponent is NULL");
        }
        Mode outputMode = WindowManager.getDefault().findMode("myoutput");
        outputMode.dockInto(addSimulationTopComponent);
        addSimulationTopComponent.open();
        addSimulationTopComponent.requestActive();
    }

    public void closeAddSimulationTopComponent() {
        if (addSimulationTopComponent != null) {
            addSimulationTopComponent.close();
            addSimulationTopComponent = null;
        }
    }

    private void updateFilter() {
        filters.clear();
        
          if (!StringUtils.isEmpty(txtName.getText())) {
            try {
                nameFilter = RowFilter.regexFilter(txtName.getText(), 1);
                filters.add(nameFilter);
            } catch (java.util.regex.PatternSyntaxException e) {
                return;
            }
        }

        if (!StringUtils.isEmpty(txtSource.getText())) {
            try {
                sourceFilter = RowFilter.regexFilter(txtSource.getText(), 2);
                filters.add(sourceFilter);
            } catch (java.util.regex.PatternSyntaxException e) {
                return;
            }
        }

        if (!StringUtils.isEmpty(txtDest.getText())) {
            try {
                destinationFilter = RowFilter.regexFilter(txtDest.getText(), 3);
                filters.add(destinationFilter);
            } catch (java.util.regex.PatternSyntaxException e) {
                return;
            }
        }

        compoundRowFilter = RowFilter.andFilter(filters); //it is also possible to use OR filter: RowFilter.orFilter

        sorterSimRules.setRowFilter(compoundRowFilter);
        jXTable1.setRowSorter(sorterSimRules);
    }

    /**
     * loads and shows simulation and ping rules
     */
    public void loadSimulationRules() {
        List<Data> simulationData = NetbeansWindowHelper.getInstance().getActiveTopologyVisualisation().getSimulationData().getSimulationData();
        loadRules(tableModel, simulationData);
    }

    private void loadRules(DefaultTableModel model, List<Data> dataList) {
        //delete all old simulation rules
        while (model.getRowCount() != 0) {
            model.removeRow(0);
        }

        //add new simulation rules
        for (Data rule : dataList) {
            model.addRow(new Object[]{rule.getId(), rule.getName(), rule.getSourceVertex().getName(), rule.getDestinationVertex().getName(), rule.isPing()});
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnAdd = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtSource = new javax.swing.JTextField();
        txtDest = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        jXTable1 = new org.jdesktop.swingx.JXTable();

        btnAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/add.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnAdd, org.openide.util.NbBundle.getMessage(SimulationTopComponent.class, "SimulationTopComponent.btnAdd.text")); // NOI18N
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/remove.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(SimulationTopComponent.class, "SimulationTopComponent.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/edit.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(SimulationTopComponent.class, "SimulationTopComponent.jButton2.text")); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(SimulationTopComponent.class, "SimulationTopComponent.jPanel1.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SimulationTopComponent.class, "SimulationTopComponent.jLabel1.text")); // NOI18N

        txtSource.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtSourceKeyReleased(evt);
            }
        });

        txtDest.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtDestKeyReleased(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(SimulationTopComponent.class, "SimulationTopComponent.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(SimulationTopComponent.class, "SimulationTopComponent.jLabel3.text")); // NOI18N

        txtName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtNameKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(13, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addGap(44, 44, 44)
                .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(46, 46, 46)
                .addComponent(jLabel1)
                .addGap(44, 44, 44)
                .addComponent(txtSource, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(46, 46, 46)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtDest, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(123, 123, 123))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2)
                        .addComponent(txtDest, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(txtSource, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel3)
                        .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 12, Short.MAX_VALUE))
        );

        jXTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Name", "Source", "Destination", "Ping"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jXTable1.setColumnSelectionAllowed(true);
        jXTable1.setSortable(false);
        jXTable1.getTableHeader().setReorderingAllowed(false);
        jScrollPane2.setViewportView(jXTable1);
        jXTable1.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jXTable1.getColumnModel().getColumn(0).setResizable(false);
        jXTable1.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(SimulationTopComponent.class, "SimulationTopComponent.jXTable1.columnModel.title2")); // NOI18N
        jXTable1.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(SimulationTopComponent.class, "SimulationTopComponent.jXTable1.columnModel.title4")); // NOI18N
        jXTable1.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(SimulationTopComponent.class, "SimulationTopComponent.jXTable1.columnModel.title0")); // NOI18N
        jXTable1.getColumnModel().getColumn(3).setHeaderValue(org.openide.util.NbBundle.getMessage(SimulationTopComponent.class, "SimulationTopComponent.jXTable1.columnModel.title1")); // NOI18N
        jXTable1.getColumnModel().getColumn(4).setMinWidth(50);
        jXTable1.getColumnModel().getColumn(4).setPreferredWidth(70);
        jXTable1.getColumnModel().getColumn(4).setMaxWidth(100);
        jXTable1.getColumnModel().getColumn(4).setHeaderValue(org.openide.util.NbBundle.getMessage(SimulationTopComponent.class, "SimulationTopComponent.jXTable1.columnModel.title3")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 643, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnAdd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(21, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(34, 34, 34)
                        .addComponent(btnAdd)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2)
                        .addGap(0, 96, Short.MAX_VALUE)))
                .addGap(38, 38, 38)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        addSimulationTopComponent = new AddSimulationTopComponent(this);
        addSimulation();
    }//GEN-LAST:event_btnAddActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        if (jXTable1.getSelectedRowCount() == 0) {
            return;
        }
        int rowToRemove = jXTable1.convertRowIndexToModel(jXTable1.getSelectedRow());
        String dataID = (String) tableModel.getValueAt(rowToRemove, 0);
        NetbeansWindowHelper.getInstance().getActiveTopologyVisualisation().getSimulationData().removeSimulationData(dataID);
        loadSimulationRules();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        if (jXTable1.getSelectedRowCount() == 0) {
            return;
        }

        int rowToModify = jXTable1.convertRowIndexToModel(jXTable1.getSelectedRow());
        String dataID = (String) tableModel.getValueAt(rowToModify, 0);
        Data data = simulationData.findSimulationData(dataID);

        if (addSimulationTopComponent == null) {
            addSimulationTopComponent = new AddSimulationTopComponent(this);
        }
        addSimulationTopComponent.modifySimulationRule(data, SimulationRuleIterator.ROUTING_PANEL);
        addSimulation();

    }//GEN-LAST:event_jButton2ActionPerformed

    private void txtSourceKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSourceKeyReleased
        updateFilter();
    }//GEN-LAST:event_txtSourceKeyReleased

    private void txtDestKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtDestKeyReleased
        updateFilter();
    }//GEN-LAST:event_txtDestKeyReleased

    private void txtNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtNameKeyReleased
          updateFilter();
    }//GEN-LAST:event_txtNameKeyReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private org.jdesktop.swingx.JXTable jXTable1;
    private javax.swing.JTextField txtDest;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtSource;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        if (simulationFacade == null) {//simulation was not started, yet - nothing to load
            return;
        }
        List<SimulationRuleBean> simulationRules = simulationFacade.getSimulationRules();
        if (simulationRules == null) {
            throw new IllegalStateException("Simulation rules are NULL"); //this really should not happen
        }
        loadSimulationRules();
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
     * cell is rendered as button
     */
    class ButtonRenderer implements TableCellRenderer {

        JButton button = new JButton();

        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row, int column) {
            if (value == null) {
                return null;
            }
            button.setText(value.toString());
            return button;
        }
    }
}
