/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.simulationdata;

import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.traces.Trace2DSimple;
import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import lombok.Getter;
import org.apache.log4j.Logger;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import sk.stuba.fiit.kvasnicka.topologyvisual.filetype.gui.TopologyVisualisation;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.simulationdata.NetworkNodeRemoveStatDialog;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.simulationdata.NetworkNodeAddStatDialog;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.simulationdata.NetworkNodeRemoveStatDialog.AddRemove;
import sk.stuba.fiit.kvasnicka.topologyvisual.simulation.nodes.NetworkNodePropertyEnum;
import sk.stuba.fiit.kvasnicka.topologyvisual.simulation.nodes.NetworkNodeStatisticsBean.TraceIdentifier;

/**
 * Top component which displays something.
 */
//@ConvertAsProperties(
//    dtd = "-//sk.stuba.fiit.kvasnicka.topologyvisual.simulation//NetworkNodeStatistics//EN",
//autostore = false)
@TopComponent.Description(
    preferredID = "NetworkNodeStatisticsTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "output", openAtStartup = false)
@ActionID(category = "Window", id = "sk.stuba.fiit.kvasnicka.topologyvisual.simulation.NetworkNodeStatisticsTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
//@TopComponent.OpenActionRegistration(
//    displayName = "#CTL_NetworkNodeStatisticsAction",
//preferredID = "NetworkNodeStatisticsTopComponent")
@Messages({
    "CTL_NetworkNodeStatisticsAction=NetworkNodeStatistics",
    "CTL_NetworkNodeStatisticsTopComponent=NetworkNodeStatistics Window",
    "HINT_NetworkNodeStatisticsTopComponent=This is a NetworkNodeStatistics window"
})
public final class NetworkNodeStatisticsTopComponent extends TopComponent {

    private static Logger logg = Logger.getLogger(NetworkNodeStatisticsTopComponent.class);
    private NetworkNodeAddStatDialog addDialog;
    private NetworkNodeRemoveStatDialog removeDialog;
    private TopologyVisualisation topologyVisualisation;
    private Set<MonitoringNode> showingTraceSet;
    private Map<ChartTraces, ITrace2D> traceMap;

    public NetworkNodeStatisticsTopComponent(TopologyVisualisation topologyVisualisation) {
        initComponents();
        setName(Bundle.CTL_NetworkNodeStatisticsTopComponent());
        setToolTipText(Bundle.HINT_NetworkNodeStatisticsTopComponent());
        addDialog = new NetworkNodeAddStatDialog(this, topologyVisualisation);
        removeDialog = new NetworkNodeRemoveStatDialog(this, topologyVisualisation);
        this.topologyVisualisation = topologyVisualisation;
        showingTraceSet = new HashSet<MonitoringNode>(topologyVisualisation.getTopology().getVertexFactory().getAllVertices().size() * 4 / 3);
        traceMap = new HashMap<ChartTraces, ITrace2D>(topologyVisualisation.getTopology().getVertexFactory().getAllVertices().size() * 4 / 3);
    }

    private void showAddDialog() {
        addDialog.showDialog();
    }

    private void showRemoveDialog() {
        removeDialog.showDialog(showingTraceSet);
    }

    public void addNetworkNodes(List<TopologyVertex> nodeList, Set<NetworkNodePropertyEnum> selectedProperties) {
        for (TopologyVertex v : nodeList) {
            MonitoringNode monitoring = new MonitoringNode(v);
            for (NetworkNodePropertyEnum propertyEnum : selectedProperties) {
                monitoring.addNetworkNodeProperty(propertyEnum);
            }

            showingTraceSet.add(monitoring);

            showTraces();
        }
    }

    public void removeNetworkNodes(Set<AddRemove> nodesToRemoveSet) {
        //todo remove from chart
        for (AddRemove addRemove : nodesToRemoveSet) {

            TraceIdentifier traceIdentifier = topologyVisualisation.getNetworkNodeStatsManager().getTrace(addRemove.getV().getDataModel(), addRemove.getPropertyEnum());

            if (addRemove.isSelected()) {//add trace               
                if (traceIdentifier.isVisible()) {//this should not happen
                    logg.warn("trace is already visible - there is something wrong with NetworkNodeRemoveStatDialog - node: " + addRemove.getV().getName() + " property: " + addRemove.getPropertyEnum());
                    continue;
                }
                traceIdentifier.setVisible(true);
                chart2D1.addTrace(traceIdentifier.getTrace());
            } else {//remove trace
                if (!traceIdentifier.isVisible()) {//this should not happen
                    logg.warn("trace is NOT visible - there is something wrong with NetworkNodeRemoveStatDialog - node: " + addRemove.getV().getName() + " property: " + addRemove.getPropertyEnum());
                    continue;
                }
                traceIdentifier.setVisible(false);
                chart2D1.removeTrace(traceIdentifier.getTrace());
            }
        }
    }

    private void showTraces() {
        for (MonitoringNode monitoringNode : showingTraceSet) {
            for (NetworkNodePropertyEnum prop : monitoringNode.getPropertyEnumSet()) {
                TraceIdentifier traceIdentifier = topologyVisualisation.getNetworkNodeStatsManager().getTrace(monitoringNode.vertex.getDataModel(), prop);

                if (traceIdentifier.isVisible()) {
                    continue;
                }

                traceIdentifier.setVisible(true);

                chart2D1.addTrace(traceIdentifier.getTrace());
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        panelChart = new javax.swing.JPanel();
        chart2D1 = new info.monitorenter.gui.chart.Chart2D();
        panelButtons = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();

        jPanel2.setLayout(new java.awt.BorderLayout());

        panelChart.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout chart2D1Layout = new javax.swing.GroupLayout(chart2D1);
        chart2D1.setLayout(chart2D1Layout);
        chart2D1Layout.setHorizontalGroup(
            chart2D1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 778, Short.MAX_VALUE)
        );
        chart2D1Layout.setVerticalGroup(
            chart2D1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 284, Short.MAX_VALUE)
        );

        panelChart.add(chart2D1, java.awt.BorderLayout.CENTER);

        jPanel2.add(panelChart, java.awt.BorderLayout.CENTER);

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/add.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(NetworkNodeStatisticsTopComponent.class, "NetworkNodeStatisticsTopComponent.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/edit.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(NetworkNodeStatisticsTopComponent.class, "NetworkNodeStatisticsTopComponent.jButton2.text")); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelButtonsLayout = new javax.swing.GroupLayout(panelButtons);
        panelButtons.setLayout(panelButtonsLayout);
        panelButtonsLayout.setHorizontalGroup(
            panelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelButtonsLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(panelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        panelButtonsLayout.setVerticalGroup(
            panelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2)
                .addContainerGap(214, Short.MAX_VALUE))
        );

        jPanel2.add(panelButtons, java.awt.BorderLayout.WEST);

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(NetworkNodeStatisticsTopComponent.class, "NetworkNodeStatisticsTopComponent.jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 905, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 284, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(NetworkNodeStatisticsTopComponent.class, "NetworkNodeStatisticsTopComponent.jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

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

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        showAddDialog();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        showRemoveDialog();
    }//GEN-LAST:event_jButton2ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private info.monitorenter.gui.chart.Chart2D chart2D1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JPanel panelButtons;
    private javax.swing.JPanel panelChart;
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

    public static class ChartTraces {

        private TopologyVertex vertex;
        private NetworkNodePropertyEnum propertyEnum;

        public ChartTraces(TopologyVertex vertex, NetworkNodePropertyEnum propertyEnum) {
            this.vertex = vertex;
            this.propertyEnum = propertyEnum;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 17 * hash + (this.vertex != null ? this.vertex.hashCode() : 0);
            hash = 17 * hash + (this.propertyEnum != null ? this.propertyEnum.hashCode() : 0);
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
            final ChartTraces other = (ChartTraces) obj;
            if (this.vertex != other.vertex && (this.vertex == null || !this.vertex.equals(other.vertex))) {
                return false;
            }
            if (this.propertyEnum != other.propertyEnum) {
                return false;
            }
            return true;
        }
    }

    @Getter
    public static class MonitoringNode {

        private TopologyVertex vertex;
        private Set<NetworkNodePropertyEnum> propertyEnumSet;

        private MonitoringNode(TopologyVertex vertex) {
            this.vertex = vertex;
            propertyEnumSet = new TreeSet<NetworkNodePropertyEnum>();
        }

        public void addNetworkNodeProperty(NetworkNodePropertyEnum prop) {
            propertyEnumSet.add(prop);
        }

        public void removeNetworkNodeProperty(NetworkNodePropertyEnum prop) {
            propertyEnumSet.remove(prop);
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
