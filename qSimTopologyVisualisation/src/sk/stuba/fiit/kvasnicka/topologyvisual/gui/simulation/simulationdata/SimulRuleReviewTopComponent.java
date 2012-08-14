/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.simulationdata;

import java.util.List;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
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
import sk.stuba.fiit.kvasnicka.topologyvisual.simulation.rules.SimulRuleStatisticalData;
import sk.stuba.fiit.kvasnicka.topologyvisual.simulation.rules.SimulRuleStatisticalDataManager;

/**
 * Top component which displays review (summary) about all simulation rules.
 */
//@ConvertAsProperties(dtd = "-//sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.simulationdata//SimulRuleReview//EN",
//autostore = false)
@TopComponent.Description(preferredID = "SimulRuleReviewTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "commonpalette", openAtStartup = false)
@ActionID(category = "Window", id = "sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.simulationdata.SimulRuleReviewTopComponent")
@ActionReference(path = "Menu/Window" /*
 * , position = 333
 */)
//@TopComponent.OpenActionRegistration(displayName = "#CTL_SimulRuleReviewAction",
//preferredID = "SimulRuleReviewTopComponent")
@Messages({
    "CTL_SimulRuleReviewAction=SimlRuleReview",
    "CTL_SimulRuleReviewTopComponent=SimlRuleReview Window",
    "HINT_SimulRuleReviewTopComponent=This is a SimlRuleReview window",
    "active=active",
    "finished=finished"
})
public final class SimulRuleReviewTopComponent extends TopComponent implements SimulationRuleActivationListener, SimulationRuleListener, PingRuleListener {

    private DefaultTableModel pingModel, simulRuleModel;
    private SimulRuleStatisticalDataManager statManager;
    private SimulationDataTopComponent simulDataTopComponent;
    private final SimulationFacade simulationFacade;

    public SimulRuleReviewTopComponent(SimulationFacade simulationFacade) {
        initComponents();
        this.simulationFacade = simulationFacade;

        setName(Bundle.CTL_SimulRuleReviewTopComponent());
        setToolTipText(Bundle.HINT_SimulRuleReviewTopComponent());

        putClientProperty(TopComponent.PROP_CLOSING_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);

        pingModel = (DefaultTableModel) pingTable.getModel();
        simulRuleModel = (DefaultTableModel) simulTable.getModel();

        pingTable.removeColumn(pingTable.getColumnModel().getColumn(0));
        simulTable.removeColumn(simulTable.getColumnModel().getColumn(0));

        SelectionListener listenerPing = new SelectionListener(pingTable, true);
        pingTable.getSelectionModel().addListSelectionListener(listenerPing);
        pingTable.getColumnModel().getSelectionModel().addListSelectionListener(listenerPing);

        SelectionListener listenerSimul = new SelectionListener(simulTable, false);
        simulTable.getSelectionModel().addListSelectionListener(listenerSimul);
        simulTable.getColumnModel().getSelectionModel().addListSelectionListener(listenerSimul);

        ChangeListener changeListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
                int index = sourceTabbedPane.getSelectedIndex();
                if (index == 1) {//tab with index=1 is simulation rules tab
                    btnStat.setEnabled(false);
                } else {
                    btnStat.setEnabled(true);
                }

            }
        };
        jTabbedPane1.addChangeListener(changeListener);
    }

    public void closeSimulationDataTopComponent() {
        if (simulDataTopComponent != null) {
            simulDataTopComponent.close();
            simulDataTopComponent = null;
        }
    }

    public void setSimulationRules(SimulRuleStatisticalDataManager statManager, List<SimulationRuleBean> simulRules) {
        this.statManager = statManager;
        simulDataTopComponent = new SimulationDataTopComponent(statManager.getStatisticalData());


        //clear both tables from old data
        while (pingModel.getRowCount() != 0) {
            pingModel.removeRow(0);
        }

        while (simulRuleModel.getRowCount() != 0) {
            simulRuleModel.removeRow(0);
        }

        //add ping rules
        for (SimulRuleStatisticalData data : statManager.getStatisticalData()) {
            if (data.getRule().isPing()) {
                addRow(pingModel, data.getRule());
            }
        }

        //add simulation rules
        for (SimulationRuleBean rule : simulRules) {
            if (!rule.isPing()) {
                addRow(simulRuleModel, rule);
            }
        }
    }

    private void addRow(DefaultTableModel model, SimulationRuleBean rule) {
        model.addRow(new Object[]{rule.getUniqueID(), rule.getName(), rule.getSource().getName(), rule.getDestination().getName(), isActive(rule)});
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

    private SimulationRuleBean findSimulationRuleByRow(int row, DefaultTableModel model) {
        if (model.getRowCount() - 1 < row) {
            throw new IllegalStateException("illegal row number: " + row + " actual row count: " + model.getRowCount());
        }

        String ruleId = (String) model.getValueAt(row, 0);

        return simulationFacade.findSimulationRuleById(ruleId);

    }

    private String isActive(SimulationRuleBean rule) {
        if (rule.isFinished()) {
            return NbBundle.getMessage(SimulRuleReviewTopComponent.class, "finished");
        }
        if (rule.isActive()) {
            return NbBundle.getMessage(SimulRuleReviewTopComponent.class, "active");
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

    private void showDetails(int row, boolean ping) {
        SimulationRuleBean rule;
        if (ping) {
            rule = findSimulationRuleByRow(row, pingModel);
        } else {
            rule = findSimulationRuleByRow(row, simulRuleModel);
        }

        lblName.setText(rule.getName());
        lblSource.setText(rule.getSource().getName());
        lblDestination.setText(rule.getDestination().getName());
        lblActivation.setText(isActive(rule));
        lblLayer4.setText(rule.getLayer4Type().toString());
        lblPacketSize.setText(String.valueOf(rule.getPacketSize()) + " B");

        try {
            Double.parseDouble(isActive(rule));
            //it is a number - rule is not activated
            btnActivate.setEnabled(true);
        } catch (NumberFormatException e) {
            //it is not a number - rule is activated or finished
            btnActivate.setEnabled(false);
        }
    }

    private void showStatisticalData() {
        //first find simulation rule that I am speaking about
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

        SimulationRuleBean simulRule = simulationFacade.findSimulationRuleById(ruleId);

        //now open statistical data top component
        if (simulDataTopComponent == null) {
            throw new IllegalStateException("simulDataTopComponent is NULL");
        }
        simulDataTopComponent.addSimulationRule(statManager, simulRule);
        simulDataTopComponent.requestAttention(true);
        simulDataTopComponent.open();
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
        btnActivate = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        lblName = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        lblActivation = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        lblSource = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        lblDestination = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        lblPacketSize = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        lblLayer4 = new javax.swing.JLabel();
        btnRoute = new javax.swing.JButton();
        btnStat = new javax.swing.JButton();

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
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
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
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 292, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(16, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(36, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(SimulRuleReviewTopComponent.class, "SimulRuleReviewTopComponent.jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnActivate, org.openide.util.NbBundle.getMessage(SimulRuleReviewTopComponent.class, "SimulRuleReviewTopComponent.btnActivate.text")); // NOI18N
        btnActivate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnActivateActionPerformed(evt);
            }
        });

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(SimulRuleReviewTopComponent.class, "SimulRuleReviewTopComponent.jPanel3.border.title"))); // NOI18N
        jPanel3.setLayout(new java.awt.GridLayout(0, 2));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SimulRuleReviewTopComponent.class, "SimulRuleReviewTopComponent.jLabel1.text")); // NOI18N
        jPanel3.add(jLabel1);

        org.openide.awt.Mnemonics.setLocalizedText(lblName, org.openide.util.NbBundle.getMessage(SimulRuleReviewTopComponent.class, "SimulRuleReviewTopComponent.lblName.text")); // NOI18N
        jPanel3.add(lblName);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(SimulRuleReviewTopComponent.class, "SimulRuleReviewTopComponent.jLabel7.text")); // NOI18N
        jPanel3.add(jLabel7);

        org.openide.awt.Mnemonics.setLocalizedText(lblActivation, org.openide.util.NbBundle.getMessage(SimulRuleReviewTopComponent.class, "SimulRuleReviewTopComponent.lblActivation.text")); // NOI18N
        jPanel3.add(lblActivation);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(SimulRuleReviewTopComponent.class, "SimulRuleReviewTopComponent.jLabel2.text")); // NOI18N
        jPanel3.add(jLabel2);

        org.openide.awt.Mnemonics.setLocalizedText(lblSource, org.openide.util.NbBundle.getMessage(SimulRuleReviewTopComponent.class, "SimulRuleReviewTopComponent.lblSource.text")); // NOI18N
        jPanel3.add(lblSource);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(SimulRuleReviewTopComponent.class, "SimulRuleReviewTopComponent.jLabel3.text")); // NOI18N
        jPanel3.add(jLabel3);

        org.openide.awt.Mnemonics.setLocalizedText(lblDestination, org.openide.util.NbBundle.getMessage(SimulRuleReviewTopComponent.class, "SimulRuleReviewTopComponent.lblDestination.text")); // NOI18N
        jPanel3.add(lblDestination);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(SimulRuleReviewTopComponent.class, "SimulRuleReviewTopComponent.jLabel5.text")); // NOI18N
        jPanel3.add(jLabel5);

        org.openide.awt.Mnemonics.setLocalizedText(lblPacketSize, org.openide.util.NbBundle.getMessage(SimulRuleReviewTopComponent.class, "SimulRuleReviewTopComponent.lblPacketSize.text")); // NOI18N
        jPanel3.add(lblPacketSize);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(SimulRuleReviewTopComponent.class, "SimulRuleReviewTopComponent.jLabel6.text")); // NOI18N
        jPanel3.add(jLabel6);

        org.openide.awt.Mnemonics.setLocalizedText(lblLayer4, org.openide.util.NbBundle.getMessage(SimulRuleReviewTopComponent.class, "SimulRuleReviewTopComponent.lblLayer4.text")); // NOI18N
        jPanel3.add(lblLayer4);

        org.openide.awt.Mnemonics.setLocalizedText(btnRoute, org.openide.util.NbBundle.getMessage(SimulRuleReviewTopComponent.class, "SimulRuleReviewTopComponent.btnRoute.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnStat, org.openide.util.NbBundle.getMessage(SimulRuleReviewTopComponent.class, "SimulRuleReviewTopComponent.btnStat.text")); // NOI18N
        btnStat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStatActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(btnActivate, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 325, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(btnStat)
                        .addGap(18, 18, 18)
                        .addComponent(btnRoute)))
                .addContainerGap(78, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 296, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnActivate)
                .addGap(22, 22, 22)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRoute)
                    .addComponent(btnStat))
                .addContainerGap(157, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnActivateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActivateActionPerformed
        activate();
    }//GEN-LAST:event_btnActivateActionPerformed

    private void btnStatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStatActionPerformed
        showStatisticalData();
    }//GEN-LAST:event_btnStatActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActivate;
    private javax.swing.JButton btnRoute;
    private javax.swing.JButton btnStat;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel lblActivation;
    private javax.swing.JLabel lblDestination;
    private javax.swing.JLabel lblLayer4;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblPacketSize;
    private javax.swing.JLabel lblSource;
    private javax.swing.JTable pingTable;
    private javax.swing.JTable simulTable;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
    }

    @Override
    public void componentClosed() {
        simulationFacade.removeSimulationRuleActivatedListener(this);
        simulationFacade.removePingRuleListener(this);
        simulationFacade.removeSimulationRuleListener(this);
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
        addRow(simulRuleModel, event.getRule());
    }

    @Override
    public void simulationRuleRemoved(SimulationRuleEvent event) {
        removeRow(simulRuleModel, event.getRule());
    }

    @Override
    public void pingRuleAdded(PingRuleEvent event) {
        addRow(pingModel, event.getRule());
    }

    @Override
    public void pingRuleRemoved(PingRuleEvent event) {
        removeRow(pingModel, event.getRule());
    }

    private class SelectionListener implements ListSelectionListener {

        JTable table;
        private boolean ping;

        // It is necessary to keep the table since it is not possible
        // to determine the table from the event's source
        SelectionListener(JTable table, boolean ping) {
            this.table = table;
            this.ping = ping;
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {

            if (table.getSelectedRowCount() == 0) {
                return;
            }
            showDetails(table.getSelectedRow(), ping);
        }
    }
}