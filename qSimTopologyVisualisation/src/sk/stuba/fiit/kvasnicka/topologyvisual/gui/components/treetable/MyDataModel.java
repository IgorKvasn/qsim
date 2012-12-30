package sk.stuba.fiit.kvasnicka.topologyvisual.gui.components.treetable;

/**
 * *****************************************************************************
 * This file is part of qSim.
 *
 * qSim is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * qSim is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * qSim. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
/**
 * @author Igor Kvasnicka
 */
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JProgressBar;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.OutputQueueManager;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.buffers.RxBuffer;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.buffers.TxBuffer;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.queues.OutputQueue;

public class MyDataModel extends MyAbstractTreeTableModel {

    private MyDataNode myroot;
    public MyDataNode inputQueueNode;
    public MyDataNode processingNode;
    public MyDataNode rxRootNode;
    public Map<NetworkNode, MyDataNode> rxNodes;//key=netork node on the other side of the edge; value = tree node in tree table
    public MyDataNode txRootNode;
    public Map<NetworkNode, MyDataNode> txNodes;
    public MyDataNode outputRootNode;
    public List<MyDataNode> outputNodes;

    public MyDataModel(NetworkNode node) {
        if (node == null) {
            return;
        }

        myroot = new MyDataNode("root - should not be visible", 0, null, false);


        //init input queue
        inputQueueNode = new MyDataNode("Input queue", node.getMaxIntputQueueSize(), node.getInputQueue(), false);
        myroot.getChildren().add(inputQueueNode);

        //init output nodes
        outputRootNode = new MyDataNode("Output queue", node.getMaxOutputQueueSize(), node.getAllOutputQueues(), false);
        myroot.getChildren().add(outputRootNode);

        outputNodes = generateOutputNodes(node);
        for (MyDataNode treeNode : outputNodes) {
            outputRootNode.getChildren().add(treeNode);
        }

        //init RX
        rxRootNode = new MyDataNode("RX buffers", node.getMaxRxSizeTotal(), node.getAllRXBuffers(), false);
        myroot.getChildren().add(rxRootNode);

        rxNodes = generateRxNodes(node);

        for (MyDataNode treeNode : rxNodes.values()) {
            rxRootNode.getChildren().add(treeNode);
        }

        //init TX
        txRootNode = new MyDataNode("TX buffers", node.getMaxTxSizeTotal(), node.getAllTXBuffers(), false);
        myroot.getChildren().add(txRootNode);

        txNodes = generateTxNodes(node);

        for (MyDataNode treeNode : txNodes.values()) {
            txRootNode.getChildren().add(treeNode);
        }

        //init processing
        processingNode = new MyDataNode("Processing packets", node.getMaxProcessingPackets(), node.getAllProcessingPackets(), false);
        myroot.getChildren().add(processingNode);

        super.setRoot(myroot);
    }

    private Map<NetworkNode, MyDataNode> generateRxNodes(NetworkNode node) {
        Map<NetworkNode, MyDataNode> map = new HashMap<NetworkNode, MyDataNode>();

        for (Map.Entry<NetworkNode, RxBuffer> e : node.getRxInterfaces().entrySet()) {
            MyDataNode treeNode = new MyDataNode(e.getKey().getName(), node.getMaxRxBufferSize(), e.getValue(), true);
            map.put(e.getKey(), treeNode);
        }

        return map;
    }

    private Map<NetworkNode, MyDataNode> generateTxNodes(NetworkNode node) {
        Map<NetworkNode, MyDataNode> map = new HashMap<NetworkNode, MyDataNode>();

        for (Map.Entry<NetworkNode, TxBuffer> e : node.getTxInterfaces().entrySet()) {
            MyDataNode treeNode = new MyDataNode(e.getKey().getName(), node.getMaxTxBufferSize(), e.getValue(), true);
            map.put(e.getKey(), treeNode);
        }

        return map;
    }

    private List<MyDataNode> generateOutputNodes(NetworkNode node) {
        List<MyDataNode> nodes = new LinkedList<MyDataNode>();
        OutputQueueManager outputQueues = node.getOutputQueueManager();
        int index = 1;
        for (OutputQueue qDef : outputQueues.getQueues()) {
            MyDataNode n = new MyDataNode("#" + index, qDef.getMaxCapacity(), qDef, true);
            nodes.add(n);
        }
        return nodes;
    }

    @Override
    public Object getChild(Object parent, int index) {
        return ((MyDataNode) parent).getChildren().get(index);
    }

    @Override
    public int getChildCount(Object parent) {
        return ((MyDataNode) parent).getChildren().size();
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
    public Object getValueAt(Object node, int column) {
        MyDataNode treenode = (MyDataNode) node;
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
                return treenode.isInChart();
            default:
                throw new IllegalStateException("unknown column number: " + column);
        }
    }

    @Override
    public boolean isCellEditable(Object node, int column) {
        if (column == 4) {
            return true;
        }
        if (column == 0) {
            return true;
        }
        return false; // Important to activate TreeExpandListener
    }

    @Override
    public void setValueAt(Object aValue, Object node, int column) {
        if (column == 4) {
            ((MyDataNode) node).setInChart((Boolean) aValue);
        }
    }
}