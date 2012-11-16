/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.panels.simulationdata.networknode;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.ListModel;
import javax.swing.RowFilter;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import lombok.Getter;
import org.jdesktop.swingx.JXSearchField;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.OutputQueueManager;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.UsageStatistics;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.buffers.RxBuffer;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.buffers.TxBuffer;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.queues.OutputQueue;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.timer.SimulationTimerEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.timer.SimulationTimerListener;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.simulationdata.NetworkNodeStatisticsTopComponent;

/**
 *
 * @author Igor Kvasnicka
 */
public class TextualStatisticsPanel extends javax.swing.JPanel implements SimulationTimerListener {

    private static DecimalFormat twoDForm = new DecimalFormat("#.##");
    private DefaultListModel listModel;
    private RowFilter<ListModel, Object> listFilter;
    private List<TopologyVertex> nodeList;
    private MyTreeTableModel tableModel = new MyTreeTableModel(null);
    private NetworkNode selectedNode;
    private Map<NetworkNode, MyTreeTableModel> treeTableModelCache;//cache TreeTableModels, because it is quite expensive to build one...
    private final NetworkNodeStatisticsTopComponent networkNodeStatisticsTopComponent;

    /**
     * Creates new form TextualStatisticsPanel
     */
    public TextualStatisticsPanel(List<TopologyVertex> nodeList, NetworkNodeStatisticsTopComponent networkNodeStatisticsTopComponent) {
        this.nodeList = nodeList;
        this.networkNodeStatisticsTopComponent = networkNodeStatisticsTopComponent;

        initComponents();

        listModel = new DefaultListModel();
        listTextualNodes.setModel(listModel);

        jXSearchField1.setSearchMode(JXSearchField.SearchMode.INSTANT);
        jXSearchField1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterList(e.getActionCommand());
            }
        });

        initTextualNodesList();

        jXTreeTable1.setDefaultRenderer(JProgressBar.class, new ProgressRenderer());

        jXTreeTable1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        ListSelectionListener listener = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {//value is not definitely set (user did not release mouse button)
                    return;
                }
                int rowIndex = jXTreeTable1.getSelectedRow();
                int colIndex = jXTreeTable1.getSelectedColumn();

                if ((rowIndex == -1) || (colIndex == -1)) {//just in case
                    return;
                }

                if (colIndex == 4) {//column 4 is boolean column
                    boolean oldValue = (Boolean) jXTreeTable1.getValueAt(rowIndex, colIndex);
                    jXTreeTable1.setValueAt(!oldValue, rowIndex, colIndex);                                                           
                }

            }
        };
        jXTreeTable1.getSelectionModel().addListSelectionListener(listener);
        jXTreeTable1.getColumnModel().getSelectionModel().addListSelectionListener(listener);


        treeTableModelCache = new HashMap<NetworkNode, MyTreeTableModel>();

        listTextualNodes.setSelectedIndex(0);//select the first node
        networkNodeSelectedChanged();

    }

    private void filterList(String text) {
        listFilter = RowFilter.regexFilter(text);
        listTextualNodes.setRowFilter(listFilter);
    }

    private void initTextualNodesList() {
        jXSearchField1.setText("");
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

        if (treeTableModelCache.containsKey(selectedNode)) {
            tableModel = treeTableModelCache.get(selectedNode);
        } else {
            tableModel = new MyTreeTableModel(selectedNode);//this will also populate model
            treeTableModelCache.put(selectedNode, tableModel);
        }
        jXTreeTable1.setTreeTableModel(tableModel);
        jXTreeTable1.expandAll();

        jXTreeTable1.repaint();
    }

    @Override
    public void simulationTimerOccurred(SimulationTimerEvent ste) {
        updateStatistics();
    }

    private void updateStatistics() {
        if (selectedNode == null) {
            return;
        }
        tableModel.inputQueueNode.currentUsage = selectedNode.getInputQueueUsage();
        tableModel.processingNode.currentUsage = selectedNode.getProcessingPackets();

        //update RX nodes
        for (Map.Entry<NetworkNode, RxBuffer> e : selectedNode.getRxInterfaces().entrySet()) {
            MyTreeNode treeNode = tableModel.rxNodes.get(e.getKey());
            if (treeNode == null) {
                throw new IllegalStateException("network node was not found in tree nodes " + e.getKey().getName());
            }
            treeNode.setCurrentUsage(e.getValue().getNumberOfFragments());
        }
        //update TX nodes
        for (Map.Entry<NetworkNode, TxBuffer> e : selectedNode.getTxInterfaces().entrySet()) {
            MyTreeNode treeNode = tableModel.txNodes.get(e.getKey());
            if (treeNode == null) {
                throw new IllegalStateException("network node was not found in tree nodes " + e.getKey().getName());
            }
            treeNode.setCurrentUsage(e.getValue().getFragmentsCount());
        }

        //update output queue nodes
        for (int i = 0; i < tableModel.outputNodes.size(); i++) {
            MyTreeNode treeNode = tableModel.outputNodes.get(i);
            treeNode.setCurrentUsage(selectedNode.getOutputQueueManager().getQueueUsedCapacity(i));
        }

        //update progress bars
        jXTreeTable1.repaint();
    }

    /**
     * saves and shows selected statistics in chart
     */
    public void saveChangesToChart() {
        //following map contains: key = network node; value = list of all UsageStatistics (buffers/queues) that should be in the graph
        List<UsageStatistics> usages = new LinkedList<UsageStatistics>();
        for (Map.Entry<NetworkNode, MyTreeTableModel> e : treeTableModelCache.entrySet()) {

            //input queue
            if (e.getValue().inputQueueNode.inChart) {
                usages.add(e.getValue().inputQueueNode.getUsageStatistics());
            }
            //processing
            if (e.getValue().processingNode.inChart) {
                usages.add(e.getValue().processingNode.getUsageStatistics());
            }
            //output queues
            for (MyTreeNode oNode : e.getValue().outputNodes) {
                if (oNode.inChart) {
                    usages.add(oNode.getUsageStatistics());
                }
            }
            //RX
            for (MyTreeNode rNode : e.getValue().rxNodes.values()) {
                if (rNode.inChart) {
                    usages.add(rNode.getUsageStatistics());
                }
            }
            //TX
            for (MyTreeNode tNode : e.getValue().txNodes.values()) {
                if (tNode.inChart) {
                    usages.add(tNode.getUsageStatistics());
                }
            }
        }

        networkNodeStatisticsTopComponent.updateChart(usages);

    }

    private class MyTreeTableModel extends AbstractTreeTableModel {

        private MyTreeNode myroot;
        private MyTreeNode inputQueueNode;
        private MyTreeNode processingNode;
        private MyTreeNode rxRootNode;
        private Map<NetworkNode, MyTreeNode> rxNodes;//key=netork node on the other side of the edge; value = tree node in tree table
        private MyTreeNode txRootNode;
        private Map<NetworkNode, MyTreeNode> txNodes;
        private MyTreeNode outputRootNode;
        private List<MyTreeNode> outputNodes;

        private MyTreeTableModel(NetworkNode node) {
            if (node == null) {
                return;
            }

            myroot = new MyTreeNode("root", 0, null);

            //init input queue
            inputQueueNode = new MyTreeNode("Input queue", node.getMaxIntputQueueSize(), node.getInputQueue());
            myroot.getChildren().add(inputQueueNode);

            //init output nodes
            outputRootNode = new MyTreeNode("Output queue", node.getMaxOutputQueueSize(), node.getAllOutputQueues());
            myroot.getChildren().add(outputRootNode);

            outputNodes = generateOutputNodes(node);
            for (MyTreeNode treeNode : outputNodes) {
                outputRootNode.getChildren().add(treeNode);
            }

            //init RX
            rxRootNode = new MyTreeNode("RX buffers", node.getMaxRxSizeTotal(), node.getAllRXBuffers());
            myroot.getChildren().add(rxRootNode);

            rxNodes = generateRxNodes(node);

            for (MyTreeNode treeNode : rxNodes.values()) {
                rxRootNode.getChildren().add(treeNode);
            }

            //init TX
            txRootNode = new MyTreeNode("TX buffers", node.getMaxTxSizeTotal(), node.getAllTXBuffers());
            myroot.getChildren().add(txRootNode);

            txNodes = generateTxNodes(node);

            for (MyTreeNode treeNode : txNodes.values()) {
                txRootNode.getChildren().add(treeNode);
            }

            //init processing
            processingNode = new MyTreeNode("Processing packets", node.getMaxProcessingPackets(), node.getAllProcessingPackets());
            myroot.getChildren().add(processingNode);
        }

        private Map<NetworkNode, MyTreeNode> generateRxNodes(NetworkNode node) {
            Map<NetworkNode, MyTreeNode> map = new HashMap<NetworkNode, MyTreeNode>();

            for (Map.Entry<NetworkNode, RxBuffer> e : node.getRxInterfaces().entrySet()) {
                MyTreeNode treeNode = new MyTreeNode(e.getKey().getName(), node.getMaxRxBufferSize(), e.getValue());
                map.put(e.getKey(), treeNode);
            }

            return map;
        }

        private Map<NetworkNode, MyTreeNode> generateTxNodes(NetworkNode node) {
            Map<NetworkNode, MyTreeNode> map = new HashMap<NetworkNode, MyTreeNode>();

            for (Map.Entry<NetworkNode, TxBuffer> e : node.getTxInterfaces().entrySet()) {
                MyTreeNode treeNode = new MyTreeNode(e.getKey().getName(), node.getMaxTxBufferSize(), e.getValue());
                map.put(e.getKey(), treeNode);
            }

            return map;
        }

        private List<MyTreeNode> generateOutputNodes(NetworkNode node) {
            List<MyTreeNode> nodes = new LinkedList<MyTreeNode>();
            OutputQueueManager outputQueues = node.getOutputQueueManager();
            for (OutputQueue qDef : outputQueues.getQueues()) {
                MyTreeNode n = new MyTreeNode("", qDef.getMaxCapacity(), qDef);
                nodes.add(n);
            }
            return nodes;
        }

        @Override
        public int getColumnCount() {
            return 5;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return "Name";
                case 1:
                    return "Usage";
                case 2:
                    return "Usage [%]";
                case 3:
                    return "Usage";
                case 4:
                    return "In chart";
                default:
                    throw new IllegalStateException("unknown column number: " + column);
            }
        }

        @Override
        public Object getValueAt(Object node, int column) {
            MyTreeNode treenode = (MyTreeNode) node;
            switch (column) {
                case 0:
                    return treenode.getName();
                case 1:
                    return treenode.getCurrentUsage();
                case 2:
                    return treenode.calculateUsage();
                case 3:
                    return treenode.calculateUsageAsDouble(); //progress bar
                case 4:
                    return treenode.getInChart();
                default:
                    throw new IllegalStateException("unknown column number: " + column);
            }
        }

        @Override
        public Class<?> getColumnClass(int column) {
            switch (column) {
                case 0:
                    return String.class;
                case 1:
                    return Integer.class;
                case 2:
                    return String.class;
                case 3:
                    return JProgressBar.class;
                case 4:
                    return Boolean.class;
                default:
                    throw new IllegalStateException("unknown column number: " + column);
            }
        }

        @Override
        public boolean isCellEditable(Object node, int column) {
            if (column == 4) {//only column with checkbox (Boolean) is editable
                return true;
            }
            return false;
        }

        @Override
        public void setValueAt(Object value, Object node, int column) {
            if (column == 4) {//this is the columne with check box (boolean)
                ((MyTreeNode) node).setInChart((Boolean) value);
                return;
            }
            super.setValueAt(value, node, column);
        }

        @Override
        public Object getChild(Object node, int index) {
            MyTreeNode treenode = (MyTreeNode) node;
            return treenode.getChildren().get(index);
        }

        @Override
        public int getChildCount(Object parent) {
            MyTreeNode treenode = (MyTreeNode) parent;
            return treenode.getChildren().size();
        }

        @Override
        public int getIndexOfChild(Object parent, Object child) {
            MyTreeNode treenode = (MyTreeNode) parent;
            for (int i = 0; i > treenode.getChildren().size(); i++) {
                if (treenode.getChildren().get(i) == child) {
                    return i;
                }
            }

            return 0;
        }

        @Override
        public boolean isLeaf(Object node) {
            MyTreeNode treenode = (MyTreeNode) node;
            if (treenode.getChildren().size() > 0) {
                return false;
            }
            return true;
        }

        @Override
        public Object getRoot() {
            return myroot;
        }
    }

    private class MyTreeNode {

        private String name;
        private double currentUsage;
        private int maxCapacity;
        private UsageStatistics usageStatistics;
        private List<MyTreeNode> children = new ArrayList<MyTreeNode>();
        private Boolean inChart;

        private MyTreeNode(String name, int maxCapacity, UsageStatistics usageStatistics) {
            this.name = name;
            this.maxCapacity = maxCapacity;
            this.inChart = false;
            this.usageStatistics = usageStatistics;
        }

        public UsageStatistics getUsageStatistics() {
            return usageStatistics;
        }

        /**
         * formats usage to two decimal places
         *
         * @return
         */
        private String calculateUsage() {
            if (maxCapacity == 0) {//this prevents division by zero
                return String.valueOf(0);
            }
            return twoDForm.format(calculateUsageAsDouble());
        }

        private Double calculateUsageAsDouble() {
            if (maxCapacity == 0) {//this prevents division by zero
                return 0.0;
            }
            return (currentUsage * 100) / maxCapacity;
        }

        public Boolean getInChart() {
            return inChart;
        }

        public void setInChart(Boolean inChart) {
            this.inChart = inChart;
        }

        public double getCurrentUsage() {
            return currentUsage;
        }

        public int getMaxCapacity() {
            return maxCapacity;
        }

        public void setCurrentUsage(double currentUsage) {
            this.currentUsage = currentUsage;
        }

        public String getName() {
            return name;
        }

        public List<MyTreeNode> getChildren() {
            return children;
        }
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
        jXSearchField1 = new org.jdesktop.swingx.JXSearchField();
        jScrollPane2 = new javax.swing.JScrollPane();
        listTextualNodes = new org.jdesktop.swingx.JXList();
        jScrollPane1 = new javax.swing.JScrollPane();
        jXTreeTable1 = new org.jdesktop.swingx.JXTreeTable();

        setPreferredSize(new java.awt.Dimension(905, 284));

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jXSearchField1.setText(org.openide.util.NbBundle.getMessage(TextualStatisticsPanel.class, "TextualStatisticsPanel.jXSearchField1.text_1")); // NOI18N
        jXSearchField1.setToolTipText(org.openide.util.NbBundle.getMessage(TextualStatisticsPanel.class, "TextualStatisticsPanel.jXSearchField1.toolTipText")); // NOI18N
        jXSearchField1.setLayoutStyle(org.jdesktop.swingx.JXSearchField.LayoutStyle.VISTA);

        jScrollPane2.setViewportView(listTextualNodes);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jXSearchField1, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addComponent(jXSearchField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE)
                .addContainerGap())
        );

        jXTreeTable1.setColumnSelectionAllowed(true);
        jXTreeTable1.setShowGrid(false);
        jScrollPane1.setViewportView(jXTreeTable1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(659, Short.MAX_VALUE))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(279, 279, 279)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 578, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(48, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(23, 23, 23)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(23, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private org.jdesktop.swingx.JXSearchField jXSearchField1;
    private org.jdesktop.swingx.JXTreeTable jXTreeTable1;
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
