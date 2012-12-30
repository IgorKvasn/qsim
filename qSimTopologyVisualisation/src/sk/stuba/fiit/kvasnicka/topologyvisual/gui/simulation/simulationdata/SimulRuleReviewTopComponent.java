/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.simulationdata;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.JTable;
import javax.swing.ToolTipManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.pingrule.PingRuleEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.pingrule.PingRuleListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.ruleactivation.SimulationRuleActivationEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.ruleactivation.SimulationRuleActivationListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.simulationrule.SimulationRuleEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.simulationrule.SimulationRuleListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.facade.SimulationFacade;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;
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

    private DefaultTableModel simulRuleModel;
    private SimulRuleStatisticalDataManager statManager;
    private SimulationDataTopComponent simulDataTopComponent;
    private final SimulationFacade simulationFacade;
    private final int DEFAULT_TOOLTIP_TIMEOUT_INITIAL = ToolTipManager.sharedInstance().getInitialDelay();
    private final int DEFAULT_TOOLTIP_TIMEOUT_DISMISS = ToolTipManager.sharedInstance().getDismissDelay();

    public SimulRuleReviewTopComponent(SimulationFacade simulationFacade) {
        initComponents();
        this.simulationFacade = simulationFacade;

        setName(Bundle.CTL_SimulRuleReviewTopComponent());
        setToolTipText(Bundle.HINT_SimulRuleReviewTopComponent());

        putClientProperty(TopComponent.PROP_CLOSING_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);

        simulRuleModel = (DefaultTableModel) simulTable.getModel();

        simulTable.removeColumn(simulTable.getColumnModel().getColumn(0));


        SelectionListener listenerSimul = new SelectionListener(simulTable, false);
        simulTable.getSelectionModel().addListSelectionListener(listenerSimul);
        simulTable.getColumnModel().getSelectionModel().addListSelectionListener(listenerSimul);

        //workaround to set Tooltip dismiss delay just for this component and not for the whole Swing world
        simulTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                ToolTipManager.sharedInstance().setInitialDelay(250);
                ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ToolTipManager.sharedInstance().setInitialDelay(DEFAULT_TOOLTIP_TIMEOUT_INITIAL);
                ToolTipManager.sharedInstance().setDismissDelay(DEFAULT_TOOLTIP_TIMEOUT_DISMISS);

            }
        });

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

        while (simulRuleModel.getRowCount() != 0) {
            simulRuleModel.removeRow(0);
        }

        //add simulation rules
        for (SimulationRuleBean rule : simulRules) {
            addRow(simulRuleModel, rule);

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

    /**
     * creates tooltip for a row in table
     *
     * @param p
     * @return
     */
    private String getToolTip(Point p) {
        int rowIndex = simulTable.rowAtPoint(p);

        SimulationRuleBean rule = findSimulationRuleByRow(rowIndex, simulRuleModel);
        StringBuilder sb = new StringBuilder("<html><table>");

        sb.append("<tr><td>").append(NbBundle.getMessage(SimulRuleReviewTopComponent.class, "name")).append("</td><td>").append(rule.getName()).append("</td></tr>");
        sb.append("<tr><td>").append(NbBundle.getMessage(SimulRuleReviewTopComponent.class, "source")).append("</td><td>").append(rule.getSource().getName()).append("</td></tr>");
        sb.append("<tr><td>").append(NbBundle.getMessage(SimulRuleReviewTopComponent.class, "destination")).append("</td><td>").append(rule.getDestination().getName()).append("</td></tr>");
        sb.append("<tr><td>").append(NbBundle.getMessage(SimulRuleReviewTopComponent.class, "activation")).append("</td><td>").append(isActive(rule)).append("</td></tr>");
        sb.append("<tr><td>").append(NbBundle.getMessage(SimulRuleReviewTopComponent.class, "protocol")).append("</td><td>").append(rule.getLayer4Type().toString()).append("</td></tr>");
        sb.append("<tr><td>").append(NbBundle.getMessage(SimulRuleReviewTopComponent.class, "packetSize")).append("</td><td>").append(String.valueOf(rule.getPacketSize())).append(" B").append("</td></tr>");

        sb.append("</table></html>");

        return sb.toString();
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
        if (simulTable.getSelectedRowCount() == 0) {
            return;
        }

        int selRow = simulTable.convertRowIndexToModel(simulTable.getSelectedRow());
        String ruleId = (String) simulRuleModel.getValueAt(selRow, 0);


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
        int row = findRowBySimulationRule(rule, simulRuleModel);
        changeActiveStateTable(simulRuleModel, rule, row);
    }

    /**
     * changes active column text in table
     *
     * @param model
     * @param newActivationState
     * @param row
     */
    private void changeActiveStateTable(DefaultTableModel model, SimulationRuleBean rule, int row) {
        String text = isActive(rule);
        model.setValueAt(text, row, 4);
    }

    private void updateActivateButton(int row) {
        SimulationRuleBean rule = findSimulationRuleByRow(row, simulRuleModel);
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
        if (simulTable.getSelectedRowCount() == 0) {
            return;
        }
        int selRow = simulTable.convertRowIndexToModel(simulTable.getSelectedRow());
        String ruleId = (String) simulRuleModel.getValueAt(selRow, 0);


        SimulationRuleBean simulRule = simulationFacade.findSimulationRuleById(ruleId);

        //now open statistical data top component
        if (simulDataTopComponent == null) {
            throw new IllegalStateException("simulDataTopComponent is NULL");
        }
        simulDataTopComponent.addSimulationRule(statManager, simulRule);

        if (!simulDataTopComponent.isOpened()) {
            Mode outputMode = WindowManager.getDefault().findMode("myoutput");
            outputMode.dockInto(simulDataTopComponent);
            simulDataTopComponent.open();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane3 = new javax.swing.JScrollPane();
        simulTable = new javax.swing.JTable(){
            public String getToolTipText(MouseEvent e) {
                return getToolTip(e.getPoint());
            }
        };
        jPanel1 = new javax.swing.JPanel();
        btnActivate = new javax.swing.JButton();
        btnStat = new javax.swing.JButton();
        btnRoute = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

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

        add(jScrollPane3, java.awt.BorderLayout.CENTER);

        org.openide.awt.Mnemonics.setLocalizedText(btnActivate, org.openide.util.NbBundle.getMessage(SimulRuleReviewTopComponent.class, "SimulRuleReviewTopComponent.btnActivate.text")); // NOI18N
        btnActivate.setEnabled(false);
        btnActivate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnActivateActionPerformed(evt);
            }
        });
        jPanel1.add(btnActivate);

        org.openide.awt.Mnemonics.setLocalizedText(btnStat, org.openide.util.NbBundle.getMessage(SimulRuleReviewTopComponent.class, "SimulRuleReviewTopComponent.btnStat.text")); // NOI18N
        btnStat.setEnabled(false);
        btnStat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStatActionPerformed(evt);
            }
        });
        jPanel1.add(btnStat);

        org.openide.awt.Mnemonics.setLocalizedText(btnRoute, org.openide.util.NbBundle.getMessage(SimulRuleReviewTopComponent.class, "SimulRuleReviewTopComponent.btnRoute.text")); // NOI18N
        btnRoute.setEnabled(false);
        jPanel1.add(btnRoute);

        add(jPanel1, java.awt.BorderLayout.PAGE_END);
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
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable simulTable;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
    }

    @Override
    public void componentClosed() {
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
        addRow(simulRuleModel, event.getRule());
    }

    @Override
    public void pingRuleRemoved(PingRuleEvent event) {
        removeRow(simulRuleModel, event.getRule());
    }

    private class SelectionListener implements ListSelectionListener {

        JTable table;

        // It is necessary to keep the table since it is not possible
        // to determine the table from the event's source
        SelectionListener(JTable table, boolean ping) {
            this.table = table;
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {

            if (table.getSelectedRowCount() == 0) {
                return;
            }
            updateActivateButton(table.getSelectedRow());
            btnRoute.setEnabled(true);
            btnStat.setEnabled(true);
        }
    }
}
