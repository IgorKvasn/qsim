/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.simulationdata;

import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.pingrule.PingRuleEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.pingrule.PingRuleListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.ruleactivation.SimulationRuleActivationEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.ruleactivation.SimulationRuleActivationListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.simulationrule.SimulationRuleEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.simulationrule.SimulationRuleListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.facade.SimulationFacade;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.NetbeansWindowHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.simulation.StatisticalData;
import sk.stuba.fiit.kvasnicka.topologyvisual.simulation.StatisticalDataManager;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.simulationdata//SimulRuleReview//EN",
autostore = false)
@TopComponent.Description(preferredID = "SimulRuleReviewTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "commonpalette", openAtStartup = false)
@ActionID(category = "Window", id = "sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.simulationdata.SimulRuleReviewTopComponent")
@ActionReference(path = "Menu/Window" /*
 * , position = 333
 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_SimulRuleReviewAction",
preferredID = "SimulRuleReviewTopComponent")
@Messages({
    "CTL_SimulRuleReviewAction=SimlRuleReview",
    "CTL_SimulRuleReviewTopComponent=SimlRuleReview Window",
    "HINT_SimulRuleReviewTopComponent=This is a SimlRuleReview window",
    "active=active",
    "finished=finished"
})
public final class SimulRuleReviewTopComponent extends TopComponent implements SimulationRuleActivationListener, SimulationRuleListener, PingRuleListener {

    private DefaultTableModel pingModel, simulRuleModel;

    public SimulRuleReviewTopComponent() {
        initComponents();
        setName(Bundle.CTL_SimulRuleReviewTopComponent());
        setToolTipText(Bundle.HINT_SimulRuleReviewTopComponent());
        putClientProperty(TopComponent.PROP_CLOSING_DISABLED, Boolean.TRUE);

        pingModel = (DefaultTableModel) pingTable.getModel();
        simulRuleModel = (DefaultTableModel) simulTable.getModel();

        pingTable.removeColumn(pingTable.getColumnModel().getColumn(0));
        simulTable.removeColumn(simulTable.getColumnModel().getColumn(0));
    }

    public void setSimulationRules(StatisticalDataManager statManager, List<SimulationRuleBean> simulRules, double simulationTime) {
        //ping rules
        for (StatisticalData data : statManager.getStatisticalData()) {
            addRow(pingModel, data.getRule(), simulationTime);
        }

        //simulation rules
        for (SimulationRuleBean rule : simulRules) {
            addRow(simulRuleModel, rule, simulationTime);
        }
    }

    private void addRow(DefaultTableModel model, SimulationRuleBean rule, double simulationTime) {
        model.addRow(new Object[]{rule.getUniqueID(), rule.getName(), rule.getSource(), rule.getDestination(), isActive(rule, simulationTime)});
    }

    private void removeRow(DefaultTableModel model, SimulationRuleBean rule) {
        int row = findRowBySimulationRule(rule, model);
        model.removeRow(row);
    }

    private int findRowBySimulationRule(SimulationRuleBean rule, DefaultTableModel model) {
        for (int i = 0; i < model.getRowCount(); i++) {
            String id = (String) model.getValueAt(i, 0);
            if (id.equals(rule.getUniqueID())) {
                return i;
            }
        }
        throw new IllegalStateException("unable to find simulation rule in table");
    }

    private String isActive(SimulationRuleBean rule, double simulationTime) {
        if (rule.getActivationTime() <= simulationTime) {
            return NbBundle.getMessage(SimulRuleReviewTopComponent.class, "yes");
        }
        return String.valueOf(rule.getActivationTime());
    }

    private void activate() {
        String ruleId;
        if (jTabbedPane1.getSelectedIndex() == 0) {//ping rule
            if (pingTable.getSelectedRowCount() == 0) {
                return;
            }
            int selRow = pingTable.convertRowIndexToModel(pingTable.getSelectedRow());
            ruleId = (String) pingModel.getValueAt(selRow, 0);
        } else {//simulation rule
            if (simulTable.getSelectedRowCount() == 0) {
                return;
            }
            int selRow = simulTable.convertRowIndexToModel(simulTable.getSelectedRow());
            ruleId = (String) simulRuleModel.getValueAt(selRow, 0);
        }
        SimulationFacade simulationFacade = NetbeansWindowHelper.getInstance().getActiveTopologyVisualisation().getSimulationFacade();
        if (simulationFacade == null) {
            throw new IllegalStateException("simulation facade is NULL");
        }
        SimulationRuleBean simulrule = simulationFacade.findSimulationRuleById(ruleId);
        simulationFacade.setActivateSimulationRule(simulrule);
    }

    /**
     * simulation rule or ping rule change its state from activated to finished
     * or from not active to activated
     *
     * @param newActivationState false if finished; true if activated
     */
    private void simulationRuleActivationChanged(SimulationRuleBean rule, boolean newActivationState) {
        if (rule.isPing()) {
            int row = findRowBySimulationRule(rule, pingModel);
            changeActiveStateTable(pingModel, newActivationState, row);
        } else {
            int row = findRowBySimulationRule(rule, simulRuleModel);
            changeActiveStateTable(simulRuleModel, newActivationState, row);
        }
    }

    /**
     * changes active column text in table
     *
     * @param model
     * @param newActivationState
     * @param row
     */
    private void changeActiveStateTable(DefaultTableModel model, boolean newActivationState, int row) {
        String text = newActivationState ? NbBundle.getMessage(SimulRuleReviewTopComponent.class, "finished") : NbBundle.getMessage(SimulRuleReviewTopComponent.class, "active");
        model.setValueAt(text, row, 4);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        pingTable = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        simulTable = new javax.swing.JTable();
        jButton3 = new javax.swing.JButton();
        btnDetails = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();

        pingTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Id", "Name", "Source", "Destination", "Active"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
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
        pingTable.setColumnSelectionAllowed(true);
        pingTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane2.setViewportView(pingTable);
        pingTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        pingTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(SimulRuleReviewTopComponent.class, "SimulRuleReviewTopComponent.pingTable.columnModel.title4")); // NOI18N
        pingTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(SimulRuleReviewTopComponent.class, "SimulRuleReviewTopComponent.pingTable.columnModel.title0")); // NOI18N
        pingTable.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(SimulRuleReviewTopComponent.class, "SimulRuleReviewTopComponent.pingTable.columnModel.title1")); // NOI18N
        pingTable.getColumnModel().getColumn(3).setHeaderValue(org.openide.util.NbBundle.getMessage(SimulRuleReviewTopComponent.class, "SimulRuleReviewTopComponent.pingTable.columnModel.title2")); // NOI18N
        pingTable.getColumnModel().getColumn(4).setHeaderValue(org.openide.util.NbBundle.getMessage(SimulRuleReviewTopComponent.class, "SimulRuleReviewTopComponent.pingTable.columnModel.title3_1")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 412, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(193, 193, 193))
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(SimulRuleReviewTopComponent.class, "SimulRuleReviewTopComponent.jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        simulTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Id", "Name", "Source", "Destination", "Active"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
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
        simulTable.setColumnSelectionAllowed(true);
        simulTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane3.setViewportView(simulTable);
        simulTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        simulTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(SimulRuleReviewTopComponent.class, "SimulRuleReviewTopComponent.simulTable.columnModel.title4")); // NOI18N
        simulTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(SimulRuleReviewTopComponent.class, "SimulRuleReviewTopComponent.simulTable.columnModel.title0")); // NOI18N
        simulTable.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(SimulRuleReviewTopComponent.class, "SimulRuleReviewTopComponent.simulTable.columnModel.title1")); // NOI18N
        simulTable.getColumnModel().getColumn(3).setHeaderValue(org.openide.util.NbBundle.getMessage(SimulRuleReviewTopComponent.class, "SimulRuleReviewTopComponent.simulTable.columnModel.title2")); // NOI18N
        simulTable.getColumnModel().getColumn(4).setHeaderValue(org.openide.util.NbBundle.getMessage(SimulRuleReviewTopComponent.class, "SimulRuleReviewTopComponent.simulTable.columnModel.title3_1")); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 412, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(36, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(SimulRuleReviewTopComponent.class, "SimulRuleReviewTopComponent.jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButton3, org.openide.util.NbBundle.getMessage(SimulRuleReviewTopComponent.class, "SimulRuleReviewTopComponent.jButton3.text")); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnDetails, org.openide.util.NbBundle.getMessage(SimulRuleReviewTopComponent.class, "SimulRuleReviewTopComponent.btnDetails.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SimulRuleReviewTopComponent.class, "SimulRuleReviewTopComponent.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(SimulRuleReviewTopComponent.class, "SimulRuleReviewTopComponent.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(SimulRuleReviewTopComponent.class, "SimulRuleReviewTopComponent.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(SimulRuleReviewTopComponent.class, "SimulRuleReviewTopComponent.jLabel4.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(SimulRuleReviewTopComponent.class, "SimulRuleReviewTopComponent.jLabel5.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(SimulRuleReviewTopComponent.class, "SimulRuleReviewTopComponent.jLabel6.text")); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(jLabel1)
                .addGap(28, 28, 28)
                .addComponent(jLabel2)
                .addGap(28, 28, 28)
                .addComponent(jLabel3)
                .addGap(28, 28, 28)
                .addComponent(jLabel4)
                .addGap(28, 28, 28)
                .addComponent(jLabel5)
                .addGap(28, 28, 28)
                .addComponent(jLabel6)
                .addContainerGap(61, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jTabbedPane1)
                        .addGap(21, 21, 21))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnDetails)
                        .addGap(45, 45, 45))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 296, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton3)
                    .addComponent(btnDetails))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        activate();
    }//GEN-LAST:event_jButton3ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDetails;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable pingTable;
    private javax.swing.JTable simulTable;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
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

    @Override
    public void simulationRuleActivatedOccurred(SimulationRuleActivationEvent event) {
        simulationRuleActivationChanged(event.getRule(), true);
    }

    @Override
    public void simulationRuleFinishedOccurred(SimulationRuleActivationEvent event) {
        simulationRuleActivationChanged(event.getRule(), false);
    }

    @Override
    public void simulationRuleAdded(SimulationRuleEvent event) {
        SimulationFacade simulationFacade = NetbeansWindowHelper.getInstance().getActiveTopologyVisualisation().getSimulationFacade();
        if (simulationFacade == null) {
            throw new IllegalStateException("simulation facade is NULL");
        }
        addRow(simulRuleModel, event.getRule(), simulationFacade.getSimulationTime());
    }

    @Override
    public void simulationRuleRemoved(SimulationRuleEvent event) {
        removeRow(simulRuleModel, event.getRule());
    }

    @Override
    public void pingRuleAdded(PingRuleEvent event) {
        SimulationFacade simulationFacade = NetbeansWindowHelper.getInstance().getActiveTopologyVisualisation().getSimulationFacade();
        if (simulationFacade == null) {
            throw new IllegalStateException("simulation facade is NULL");
        }
        addRow(pingModel, event.getRule(), simulationFacade.getSimulationTime());
    }

    @Override
    public void pingRuleRemoved(PingRuleEvent event) {
        removeRow(pingModel, event.getRule());
    }
}
