/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.simulationdata;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis;
import info.monitorenter.gui.chart.IAxisScalePolicy;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyForcedPoint;
import java.awt.BorderLayout;
import java.awt.LayoutManager;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import lombok.Getter;
import org.jdesktop.swingx.JXTaskPane;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.export.SimulRuleExportDialog;
import sk.stuba.fiit.kvasnicka.topologyvisual.simulation.SimulationRulesExportBean;
import sk.stuba.fiit.kvasnicka.topologyvisual.simulation.rules.SimulRuleStatisticalData;
import sk.stuba.fiit.kvasnicka.topologyvisual.simulation.rules.SimulRuleStatisticalDataManager;

/**
 * Top component which displays something.
 */
//@ConvertAsProperties(dtd = "-//sk.stuba.fiit.kvasnicka.topologyvisual.simulationdata//SimulationData//EN",
//autostore = false)
@TopComponent.Description(preferredID = "SimulationDataTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "myoutput", openAtStartup = false)
@ActionID(category = "Window", id = "sk.stuba.fiit.kvasnicka.topologyvisual.simulationdata.SimulationDataTopComponent")
@ActionReference(path = "Menu/Window" /*
 * , position = 333
 */)
//@TopComponent.OpenActionRegistration(displayName = "#CTL_SimulationDataAction",
//preferredID = "SimulationDataTopComponent")
@Messages({
    "CTL_SimulationDataAction=SimulationData",
    "CTL_SimulationDataTopComponent=Simulation data",
    "HINT_SimulationDataTopComponent=Simulation data"
})
public final class SimulationDataTopComponent extends TopComponent {

    private Map<SimulationRuleBean, TaskPanel> rules;
    private SimulRuleStatisticalDataManager statDataManager;
    private SimulRuleExportDialog simulRuleExportDialog = new SimulRuleExportDialog();

    public SimulationDataTopComponent(Collection<SimulRuleStatisticalData> statDatas) {
        initComponents();
        setName(Bundle.CTL_SimulationDataTopComponent());
        setToolTipText(Bundle.HINT_SimulationDataTopComponent());
        rules = new HashMap<SimulationRuleBean, TaskPanel>();

        chart.setToolTipType(Chart2D.ToolTipType.VALUE_SNAP_TO_TRACEPOINTS);

        IAxis<IAxisScalePolicy> yAxis = (IAxis<IAxisScalePolicy>) chart.getAxisY();
        yAxis.setRangePolicy(new RangePolicyForcedPoint());

        initTraces(statDatas);
    }

    private void initTraces(Collection<SimulRuleStatisticalData> statDatas) {
        for (SimulRuleStatisticalData data : statDatas) {
            chart.addTrace(data.getChartTrace());
        }
        chart.removeAllTraces();
    }

    /**
     * adds simulation rule that should be showed
     *
     * @param rules
     */
    public void addSimulationRule(SimulRuleStatisticalDataManager statData, SimulationRuleBean rule) {
        if (rules.containsKey(rule)) {//this rule is already showing
            return;
        }
        this.statDataManager = statData;
        SimulRuleStatisticalData statisticalData = statData.getStatisticalData(rule);
        SimulationDataPanel panel = new SimulationDataPanel(statisticalData, this, statisticalData.getChartTrace().getColor());
        statisticalData.addStatisticalDataChangedListener(panel);
        JXTaskPane pane = new JXTaskPane();
        pane.setLayout(new BorderLayout());
        pane.add(panel, BorderLayout.CENTER);
        pane.setAnimated(true);
        pane.setTitle(rule.getName());
        jXTaskPaneContainer1.add(pane);
        jXTaskPaneContainer1.revalidate();

        rules.put(rule, new TaskPanel(pane, panel));
        setName(createTitle(rules.keySet()));

        showInChart(statisticalData, true);

    }

    public void removeSimulationRule(SimulationRuleBean rule) {
        SimulRuleStatisticalData statisticalData = statDataManager.getStatisticalData(rule);
        TaskPanel hashPanel = rules.get(rule);
        if (hashPanel == null) {
            throw new IllegalStateException("could not find panel for simulation rule");
        }

        statisticalData.removeStatisticalDataChangedListener(hashPanel.getPanel());

        jXTaskPaneContainer1.remove(hashPanel.getTaskPane());

        ITrace2D trace = statisticalData.getChartTrace();
        chart.removeTrace(trace);

        rules.remove(rule);
        setName(createTitle(rules.keySet()));

        jXTaskPaneContainer1.revalidate();

    }

    private String createTitle(Collection<SimulationRuleBean> rules) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (SimulationRuleBean rule : rules) {
            if (i == rules.size() - 1) {
                sb.append(rule.getName());
                break;
            }
            i++;
            sb.append(rule.getName()).append(", ");
        }
        return sb.toString();
    }

    void showInChart(SimulRuleStatisticalData statData, boolean showTrace) {
        ITrace2D trace = statData.getChartTrace();

        if (showTrace) {
            chart.addTrace(trace);
        } else {
            chart.removeTrace(trace);
        }
    }

    /**
     * prepare data to export and show export dialog
     */
    private void export() {
        if (statDataManager.getStatisticalData().isEmpty()) {//nothing to export
            return;
        }
        try {
            BufferedImage bi = chart.snapShot();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bi, "PNG", baos);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            simulRuleExportDialog.showDialog(bais, createSimulationRuleExportBeans(statDataManager));
        } catch (IOException ex) {
            NotifyDescriptor nd = new NotifyDescriptor.Message("Unable to export chart with error: " + ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }
    }

    private List<SimulationRulesExportBean> createSimulationRuleExportBeans(SimulRuleStatisticalDataManager statDataManager) {
        List<SimulationRulesExportBean> list = new LinkedList<SimulationRulesExportBean>();
        for (SimulRuleStatisticalData sData : statDataManager.getStatisticalData()) {
            list.add(new SimulationRulesExportBean(sData));
        }
        return list;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        chart = new info.monitorenter.gui.chart.Chart2D();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jXTaskPaneContainer1 = new org.jdesktop.swingx.JXTaskPaneContainer();

        setLayout(new java.awt.BorderLayout());

        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.LINE_AXIS));

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/export.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(SimulationDataTopComponent.class, "SimulationDataTopComponent.jButton1.text")); // NOI18N
        jButton1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton1);

        add(jPanel2, java.awt.BorderLayout.NORTH);

        jSplitPane1.setDividerLocation(370);

        chart.setRequestedRepaint(false);
        chart.setUseAntialiasing(true);

        javax.swing.GroupLayout chartLayout = new javax.swing.GroupLayout(chart);
        chart.setLayout(chartLayout);
        chartLayout.setHorizontalGroup(
            chartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        chartLayout.setVerticalGroup(
            chartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jSplitPane1.setRightComponent(chart);

        jPanel1.setLayout(new java.awt.BorderLayout());

        LayoutManager originLayoutManager = jXTaskPaneContainer1.getLayout();
        jXTaskPaneContainer1.setOpaque(false);

        javax.swing.GroupLayout jXTaskPaneContainer1Layout = new javax.swing.GroupLayout(jXTaskPaneContainer1);
        jXTaskPaneContainer1.setLayout(jXTaskPaneContainer1Layout);
        jXTaskPaneContainer1Layout.setHorizontalGroup(
            jXTaskPaneContainer1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 880, Short.MAX_VALUE)
        );
        jXTaskPaneContainer1Layout.setVerticalGroup(
            jXTaskPaneContainer1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 216, Short.MAX_VALUE)
        );

        jXTaskPaneContainer1.setLayout(originLayoutManager);

        jScrollPane1.setViewportView(jXTaskPaneContainer1);

        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jSplitPane1.setLeftComponent(jPanel1);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        export();
    }//GEN-LAST:event_jButton1ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private info.monitorenter.gui.chart.Chart2D chart;
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private org.jdesktop.swingx.JXTaskPaneContainer jXTaskPaneContainer1;
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

    @Getter
    private static class TaskPanel {

        private JXTaskPane taskPane;
        private SimulationDataPanel panel;

        public TaskPanel(JXTaskPane taskPane, SimulationDataPanel panel) {
            this.taskPane = taskPane;
            this.panel = panel;
        }
    }
}
