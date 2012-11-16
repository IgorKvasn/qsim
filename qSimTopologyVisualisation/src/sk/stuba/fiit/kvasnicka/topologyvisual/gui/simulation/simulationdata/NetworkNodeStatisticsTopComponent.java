/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.simulationdata;

import info.monitorenter.gui.chart.ITrace2D;
import java.awt.BorderLayout;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import lombok.Getter;
import org.apache.log4j.Logger;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.UsageStatistics;
import sk.stuba.fiit.kvasnicka.topologyvisual.filetype.gui.TopologyVisualisation;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.panels.simulationdata.networknode.TextualStatisticsPanel;
import sk.stuba.fiit.kvasnicka.topologyvisual.simulation.nodes.NetworkNodeStatisticsBean;
import sk.stuba.fiit.kvasnicka.topologyvisual.simulation.nodes.NetworkNodeStatsManager;

/**
 * Top component which displays something.
 */
@TopComponent.Description(
    preferredID = "PokusTopComponentTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "myoutput", openAtStartup = false)
@ActionID(category = "Window", id = "sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.simulationdata.PokusTopComponentTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@Messages({
    "CTL_PokusTopComponentAction=PokusTopComponent",
    "CTL_PokusTopComponentTopComponent=PokusTopComponent Window",
    "HINT_PokusTopComponentTopComponent=This is a PokusTopComponent window"
})
public final class NetworkNodeStatisticsTopComponent extends TopComponent {

    private static Logger logg = Logger.getLogger(NetworkNodeStatisticsTopComponent.class);
    private TopologyVisualisation topologyVisualisation;
    private List<MonitoringNode> monitoringNodes;
    private TextualStatisticsPanel textualStatisticsPanel;
    private List<NetworkNodeStatisticsBean.TraceIdentifier> showingTraces = new java.util.LinkedList<NetworkNodeStatisticsBean.TraceIdentifier>();

    public NetworkNodeStatisticsTopComponent(TopologyVisualisation topologyVisualisation, NetworkNodeStatsManager networkNodeStatsManager) {
        initComponents();
        setName(Bundle.CTL_PokusTopComponentTopComponent());
        setToolTipText(Bundle.HINT_PokusTopComponentTopComponent());

        this.topologyVisualisation = topologyVisualisation;

        textualStatisticsPanel = new TextualStatisticsPanel(topologyVisualisation.getTopology().getVertexFactory().getAllVertices(), this);
        jPanel3.add(textualStatisticsPanel, BorderLayout.CENTER);
        topologyVisualisation.getSimulationFacade().addSimulationTimerListener(textualStatisticsPanel);

        monitoringNodes = new java.util.LinkedList<MonitoringNode>();
        for (TopologyVertex v : topologyVisualisation.getTopology().getVertexFactory().getAllVertices()) {
            monitoringNodes.add(new MonitoringNode(v));
        }

        initTraces(networkNodeStatsManager.getAllTraces());

        jTabbedPane1.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (jTabbedPane1.getSelectedIndex()==1){//user has changed tab to "Chart"
                    textualStatisticsPanel.saveChangesToChart();
                }
            }
        });
    }

    /**
     * all traces must be added to the chart just to init them
     *
     *
     * @param traces
     */
    private void initTraces(List<ITrace2D> traces) {
        for (ITrace2D trace : traces) {
            chart2D1.addTrace(trace);
        }
        chart2D1.removeAllTraces();
    }

    public void cleanUp() {
        topologyVisualisation.getSimulationFacade().removeSimulationTimerListener(textualStatisticsPanel);
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
        jPanel3 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        panelChart = new javax.swing.JPanel();
        chart2D1 = new info.monitorenter.gui.chart.Chart2D();
        jLabel1 = new javax.swing.JLabel();

        jPanel3.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 905, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(NetworkNodeStatisticsTopComponent.class, "NetworkNodeStatisticsTopComponent.jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        jPanel2.setLayout(new java.awt.BorderLayout());

        panelChart.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout chart2D1Layout = new javax.swing.GroupLayout(chart2D1);
        chart2D1.setLayout(chart2D1Layout);
        chart2D1Layout.setHorizontalGroup(
            chart2D1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 905, Short.MAX_VALUE)
        );
        chart2D1Layout.setVerticalGroup(
            chart2D1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 269, Short.MAX_VALUE)
        );

        panelChart.add(chart2D1, java.awt.BorderLayout.CENTER);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(NetworkNodeStatisticsTopComponent.class, "NetworkNodeStatisticsTopComponent.jLabel1.text")); // NOI18N
        panelChart.add(jLabel1, java.awt.BorderLayout.SOUTH);

        jPanel2.add(panelChart, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(NetworkNodeStatisticsTopComponent.class, "NetworkNodeStatisticsTopComponent.jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 910, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 311, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private info.monitorenter.gui.chart.Chart2D chart2D1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JPanel panelChart;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    /**
     * there were some changes in traces that should be shown in chart
     *
     * @param map
     */
    public void updateChart(List<UsageStatistics> list) {
        //first remove all charts from chart
        chart2D1.removeAllTraces();
        for (NetworkNodeStatisticsBean.TraceIdentifier traceIdentifier : showingTraces) {
            traceIdentifier.setVisible(false);
        }
        showingTraces.clear();

        for (UsageStatistics usageStatistics : list) {
            NetworkNodeStatisticsBean.TraceIdentifier traceIdentifier = topologyVisualisation.getNetworkNodeStatsManager().getTrace(usageStatistics);
            chart2D1.addTrace(traceIdentifier.getTrace());
            traceIdentifier.setVisible(true);
        }
    }

    @Getter
    public static class MonitoringNode {

        private TopologyVertex vertex;
        private Set<UsageStatistics> selectedUsage;

        public MonitoringNode(TopologyVertex vertex) {
            this.vertex = vertex;
            selectedUsage = new TreeSet<UsageStatistics>();
        }

        public void addSelectedUsage(UsageStatistics prop) {
            selectedUsage.add(prop);
        }

        public void removeSelectedUsage(UsageStatistics prop) {
            selectedUsage.remove(prop);
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 73 * hash + (this.vertex != null ? this.vertex.hashCode() : 0);
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
            final MonitoringNode other = (MonitoringNode) obj;
            if (this.vertex != other.vertex && (this.vertex == null || !this.vertex.equals(other.vertex))) {
                return false;
            }
            return true;
        }
    }
}
