/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.panels.simulationdata.networknode;

import java.awt.Component;
import java.awt.FlowLayout;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListModel;
import javax.swing.RowFilter;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import lombok.Getter;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.UsageStatistics;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.buffers.RxBuffer;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.buffers.TxBuffer;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.timer.SimulationTimerEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.timer.SimulationTimerListener;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.components.treetable.MyDataModel;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.components.treetable.MyDataNode;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.components.treetable.MyTreeTable;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.components.treetable.MyTreeTableModelAdapter;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.simulationdata.NetworkNodeStatisticsTopComponent;

/**
 *
 * @author Igor Kvasnicka
 */
public class TextualStatisticsPanel extends javax.swing.JPanel implements SimulationTimerListener {

    private DefaultListModel listModel;
    private List<TopologyVertex> nodeList;
    private NetworkNode selectedNode;
    private Map<NetworkNode, MyTreeTable> treeTableModelCache;//cache TreeTableModels, because it is quite expensive to build one...
    private final NetworkNodeStatisticsTopComponent networkNodeStatisticsTopComponent;
    private MyTreeTable myTreeTable;

    /**
     * Creates new form TextualStatisticsPanel
     */
    public TextualStatisticsPanel(List<TopologyVertex> nodeList, NetworkNodeStatisticsTopComponent networkNodeStatisticsTopComponent) {
        this.nodeList = nodeList;
        this.networkNodeStatisticsTopComponent = networkNodeStatisticsTopComponent;

        initComponents();

        listModel = new DefaultListModel();
        listTextualNodes.setModel(listModel);


        initTextualNodesList();

        treeTableModelCache = new HashMap<NetworkNode, MyTreeTable>();

        selectedNode = nodeList.get(0).getDataModel();
        myTreeTable = getTreeTable(selectedNode);
        listTextualNodes.setSelectedIndex(0);//select the first node

        jPanel1.setLayout(new FlowLayout());
        jPanel1.add(new JScrollPane(myTreeTable));
        myTreeTable.setVisible(true);

    }

    private void initTextualNodesList() {
        listTextualNodes.setRowFilter(null);


        listModel.clear();
        for (TopologyVertex v : nodeList) {
            listModel.addElement(new ListItem(v.getName(), v.getDataModel()));
        }

        listTextualNodes.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                networkNodeSelectedChanged();
            }
        });
    }

    private void networkNodeSelectedChanged() {
        if (listTextualNodes.getSelectedIndex() == -1) {//nothing is selected
            return;
        }
        selectedNode = ((ListItem) listTextualNodes.getSelectedValue()).value;
        myTreeTable = getTreeTable(selectedNode);
        jPanel1.removeAll();
        jPanel1.add(new JScrollPane(myTreeTable));

        myTreeTable.repaint();
    }

    @Override
    public void simulationTimerOccurred(SimulationTimerEvent ste) {
        updateStatistics();
    }

    private void updateStatistics() {
        if (selectedNode == null) {
            return;
        }
        MyDataModel model = (MyDataModel) ((MyTreeTableModelAdapter) myTreeTable.getModel()).getTreeTableModel();

        model.inputQueueNode.setCurrentUsage(selectedNode.getInputQueueUsage());
        model.processingNode.setCurrentUsage(selectedNode.getProcessingPackets());
        model.outputRootNode.setCurrentUsage(selectedNode.getAllOutputQueueUsage());
        model.rxRootNode.setCurrentUsage(selectedNode.getRXUsage());
        model.txRootNode.setCurrentUsage(selectedNode.getTXUsage());

        //update RX nodes
        for (Map.Entry<NetworkNode, RxBuffer> e : selectedNode.getRxInterfaces().entrySet()) {
            MyDataNode treeNode = model.rxNodes.get(e.getKey());
            if (treeNode == null) {
                throw new IllegalStateException("network node was not found in tree nodes " + e.getKey().getName());
            }
            treeNode.setCurrentUsage(e.getValue().getUsage());
        }
        //update TX nodes
        for (Map.Entry<NetworkNode, TxBuffer> e : selectedNode.getTxInterfaces().entrySet()) {
            MyDataNode treeNode = model.txNodes.get(e.getKey());
            if (treeNode == null) {
                throw new IllegalStateException("network node was not found in tree nodes " + e.getKey().getName());
            }
            treeNode.setCurrentUsage(e.getValue().getUsage());
        }

        //update output queue nodes
        for (int i = 0; i < model.outputNodes.size(); i++) {
            MyDataNode treeNode = model.outputNodes.get(i);
            treeNode.setCurrentUsage(selectedNode.getOutputQueueManager().getQueueUsedCapacity(i));
        }

        //update progress bars
        myTreeTable.repaint();
    }

    /**
     * saves and shows selected statistics in chart
     */
    public void saveChangesToChart() {
        //following map contains: key = network node; value = list of all UsageStatistics (buffers/queues) that should be in the graph
        List<UsageStatistics> usages = new LinkedList<UsageStatistics>();
        for (Map.Entry<NetworkNode, MyTreeTable> e : treeTableModelCache.entrySet()) {
            MyDataModel model = (MyDataModel) ((MyTreeTableModelAdapter) myTreeTable.getModel()).getTreeTableModel();

            //input queue
            if (model.inputQueueNode.isInChart()) {
                usages.add(model.inputQueueNode.getUsageStatistics());
            }
            //processing
            if (model.processingNode.isInChart()) {
                usages.add(model.processingNode.getUsageStatistics());
            }
            //output queues
            for (MyDataNode oNode : model.outputNodes) {
                if (oNode.isInChart()) {
                    usages.add(oNode.getUsageStatistics());
                }
            }
            //RX
            for (MyDataNode rNode : model.rxNodes.values()) {
                if (rNode.isInChart()) {
                    usages.add(rNode.getUsageStatistics());
                }
            }
            //TX
            for (MyDataNode tNode : model.txNodes.values()) {
                if (tNode.isInChart()) {
                    usages.add(tNode.getUsageStatistics());
                }
            }
        }

        networkNodeStatisticsTopComponent.updateChart(usages);

    }

    private MyTreeTable getTreeTable(NetworkNode node) {
        MyTreeTable myTreeTable = null;
        if (treeTableModelCache.containsKey(node)) {
            myTreeTable = treeTableModelCache.get(node);
        } else {
            myTreeTable = new MyTreeTable(new MyDataModel(node));//this will also populate model
            myTreeTable.setDefaultRenderer(JProgressBar.class, new ProgressRenderer());

            treeTableModelCache.put(node, myTreeTable);
            myTreeTable.expandAll();
        }
        return myTreeTable;
    }

    private class ProgressRenderer extends JProgressBar implements TableCellRenderer {

        public ProgressRenderer() {
            super(JProgressBar.HORIZONTAL);
            setBorderPainted(false);
            setStringPainted(true);
            setMinimum(0);
            setMaximum(100);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus,
                int row, int column) {

            setValue(safeLongToInt(Math.round((Double) table.getValueAt(row, 3))));
            return this;
        }

        /**
         * safely converts long to int
         *
         * @param l
         * @return
         */
        private int safeLongToInt(long l) {
            if (l < Integer.MIN_VALUE) {
                return Integer.MIN_VALUE;
            }

            if (l > Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }

            return (int) l;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        listTextualNodes = new org.jdesktop.swingx.JXList();
        jPanel1 = new javax.swing.JPanel();

        setPreferredSize(new java.awt.Dimension(905, 284));

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jScrollPane2.setViewportView(listTextualNodes);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(16, Short.MAX_VALUE))
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 619, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 220, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(18, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(48, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane2;
    private org.jdesktop.swingx.JXList listTextualNodes;
    // End of variables declaration//GEN-END:variables

    @Getter
    private class ListItem {

        private String label;
        private NetworkNode value;

        public ListItem(String label, NetworkNode value) {
            this.label = label;
            this.value = value;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
