/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation;

import java.awt.Component;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.Mode;
import org.openide.windows.WindowManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.facade.SimulationFacade;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;
import sk.stuba.fiit.kvasnicka.topologyvisual.filetype.gui.TopologyVisualisation;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.NetbeansWindowHelper;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//sk.stuba.fiit.kvasnicka.topologyvisual.gui//Simulation//EN",
autostore = false)
@TopComponent.Description(preferredID = "SimulationTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "output", openAtStartup = false)
@ActionID(category = "Window", id = "sk.stuba.fiit.kvasnicka.topologyvisual.gui.SimulationTopComponent")
@ActionReference(path = "Menu/Window" /*
 * , position = 333
 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_SimulationAction",
preferredID = "SimulationTopComponent")
@Messages({
    "CTL_SimulationAction=Simulation",
    "CTL_SimulationTopComponent=Simulation Window",
    "HINT_SimulationTopComponent=This is a Simulation window",
    "not_selected_row_error=No row has been selected."
})
public final class SimulationTopComponent extends TopComponent {

    private static Logger logg = Logger.getLogger(SimulationTopComponent.class);
    private RowFilter<TableModel, Object> sourceFilter = null;
    private RowFilter<TableModel, Object> destinationFilter = null;
    private List<RowFilter<TableModel, Object>> filters = new LinkedList<RowFilter<TableModel, Object>>();
    private RowFilter<TableModel, Object> compoundRowFilter = null;
    private RowFilter<TableModel, Object> pingFilter;
    private TableRowSorter<TableModel> sorterSimRules, sorterPingRules;
    private DefaultTableModel simulRulesTableModel, pingRulesTableModel;

    public SimulationTopComponent() {
        initComponents();
        setName(Bundle.CTL_SimulationTopComponent());
        setToolTipText(Bundle.HINT_SimulationTopComponent());
        simulRulesTableModel = ((DefaultTableModel) jXTable1.getModel());
        pingRulesTableModel = ((DefaultTableModel) jXTable2.getModel());
        sorterSimRules = new TableRowSorter<TableModel>(jXTable1.getModel());
        sorterPingRules = new TableRowSorter<TableModel>(jXTable2.getModel());
    }

    /**
     * opens AddSimulationTopcompnent window it also includes workaround for bug
     * #208059: http://netbeans.org/bugzilla/show_bug.cgi?id=208059
     */
    private void addSimulation() {
        Mode outputMode = WindowManager.getDefault().findMode("output");
        TopComponent myTC = WindowManager.getDefault().findTopComponent("AddSimulationTopComponent");
        if (myTC == null) {
            logg.error("Could not ind window: AddSimulationTopComponent");
            return;
        }
        outputMode.dockInto(myTC);
        myTC.open();
        myTC.requestActive();
    }

    private void updateFilter() {
        filters.clear();

        if (!StringUtils.isEmpty(txtSource.getText())) {
            try {
                sourceFilter = RowFilter.regexFilter(txtSource.getText(), 0);
                filters.add(sourceFilter);
            } catch (java.util.regex.PatternSyntaxException e) {
                return;
            }
        }

        if (!StringUtils.isEmpty(txtDest.getText())) {
            try {
                destinationFilter = RowFilter.regexFilter(txtDest.getText(), 1);
                filters.add(destinationFilter);
            } catch (java.util.regex.PatternSyntaxException e) {
                return;
            }
        }

        compoundRowFilter = RowFilter.andFilter(filters); //it is also possible to use OR filter: RowFilter.orFilter
        sorterPingRules.setRowFilter(compoundRowFilter);
        sorterSimRules.setRowFilter(compoundRowFilter);
    }

    /**
     * loads and shows simulation and ping rules
     */
    public void loadSimulationandPingRules() {
        SimulationFacade simulationFacade = NetbeansWindowHelper.getInstance().getActiveTopologyVisualisation().getSimulationFacade();

        loadRules(simulRulesTableModel, simulationFacade.getSimulationRules(), simulationFacade);
        loadRules(pingRulesTableModel, simulationFacade.getPingSimulationRules(), simulationFacade);

    }

    private void loadRules(DefaultTableModel model, List<SimulationRuleBean> rules, SimulationFacade simulationFacade) {
        //delete all old simulation rules
        while (model.getRowCount() != 0) {
            model.removeRow(0);
        }

        //add new simulation rules
        for (SimulationRuleBean rule : rules) {
            model.addRow(new Object[]{rule.getSource().getName(), rule.getDestination().getName()});
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
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        jXTable1 = new org.jdesktop.swingx.JXTable();
        jScrollPane1 = new javax.swing.JScrollPane();
        jXTable2 = new org.jdesktop.swingx.JXTable();

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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(44, 44, 44)
                .addComponent(txtSource, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(46, 46, 46)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtDest, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(176, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtSource, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2)
                        .addComponent(txtDest, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jXTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Source", "Destination"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jXTable1.setColumnSelectionAllowed(true);
        jXTable1.setSortable(false);
        jXTable1.getTableHeader().setReorderingAllowed(false);
        jScrollPane2.setViewportView(jXTable1);
        jXTable1.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jXTable1.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(SimulationTopComponent.class, "SimulationTopComponent.jXTable1.columnModel.title0")); // NOI18N
        jXTable1.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(SimulationTopComponent.class, "SimulationTopComponent.jXTable1.columnModel.title1")); // NOI18N

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(SimulationTopComponent.class, "SimulationTopComponent.jScrollPane2.TabConstraints.tabTitle"), jScrollPane2); // NOI18N

        jXTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Source", "Destination"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jXTable2.setColumnSelectionAllowed(true);
        jXTable2.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(jXTable2);
        jXTable2.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jXTable2.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(SimulationTopComponent.class, "SimulationTopComponent.jXTable2.columnModel.title0")); // NOI18N
        jXTable2.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(SimulationTopComponent.class, "SimulationTopComponent.jXTable2.columnModel.title1")); // NOI18N

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(SimulationTopComponent.class, "SimulationTopComponent.jScrollPane1.TabConstraints.tabTitle"), jScrollPane1); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 648, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnAdd)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addContainerGap(162, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(btnAdd)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        addSimulation();
    }//GEN-LAST:event_btnAddActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        if (jXTable1.getSelectedRowCount() == 0) {
            JOptionPane.showMessageDialog(null,
                    NbBundle.getMessage(SimulationTopComponent.class, "not_selected_row_error"),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        int rowToRemove = jXTable1.convertRowIndexToModel(jXTable1.getSelectedRow());
        throw new UnsupportedOperationException("not yet implemented");
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        if (jXTable1.getSelectedRowCount() == 0) {
            JOptionPane.showMessageDialog(null,
                    NbBundle.getMessage(SimulationTopComponent.class, "not_selected_row_error"),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void txtSourceKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSourceKeyReleased
        updateFilter();
    }//GEN-LAST:event_txtSourceKeyReleased

    private void txtDestKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtDestKeyReleased
        updateFilter();
    }//GEN-LAST:event_txtDestKeyReleased
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private org.jdesktop.swingx.JXTable jXTable1;
    private org.jdesktop.swingx.JXTable jXTable2;
    private javax.swing.JTextField txtDest;
    private javax.swing.JTextField txtSource;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        TopologyVisualisation activeTopologyVisualisation = NetbeansWindowHelper.getInstance().getActiveTopologyVisualisation();
        if (activeTopologyVisualisation == null) {
            throw new IllegalStateException("activeTopologyVisualisation is NULL");
        }
        List<SimulationRuleBean> simulationRules = activeTopologyVisualisation.getSimulationFacade().getSimulationRules();
        if (simulationRules == null) {
            throw new IllegalStateException("Simulation rules are NULL"); //this really should not happen
        }
        loadSimulationandPingRules();
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
