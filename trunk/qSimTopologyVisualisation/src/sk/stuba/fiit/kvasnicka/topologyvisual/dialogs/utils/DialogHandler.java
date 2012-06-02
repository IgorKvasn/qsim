package sk.stuba.fiit.kvasnicka.topologyvisual.dialogs.utils;

import org.apache.log4j.Logger;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.*;

import sk.stuba.fiit.kvasnicka.topologyvisual.graph.utils.VertexFactory;
import sk.stuba.fiit.kvasnicka.topologyvisual.dialogs.topology.ComputerConfigurationDialog;
import sk.stuba.fiit.kvasnicka.topologyvisual.dialogs.topology.EdgeConfigurationDialog;
import sk.stuba.fiit.kvasnicka.topologyvisual.dialogs.topology.RouterConfigurationDialog;

/**
 * handles all requests to show dialogs
 *
 * @author Igor Kvasnicka
 */
public class DialogHandler {

    private static Logger logg = Logger.getLogger(DialogHandler.class);
    private VertexFactory vertexFactory;

    public DialogHandler() {
        vertexFactory = new VertexFactory();
    }

    public DialogHandler(int routerCount, int switchCount, int computerCount) {
        vertexFactory = new VertexFactory(routerCount, switchCount, computerCount);
    }

    /**
     * shows dialog with router configuration
     */
    public Router showRouterConfigurationDialog() {
        String routerName = vertexFactory.createRouterName();
        BlockingDialog bl = new RouterConfigurationDialog(routerName);
        bl.showDialog();
        RouterConfigurationDialog.ResultObject resultObject = (RouterConfigurationDialog.ResultObject) bl.getUserInput();
        if (resultObject == null) {
            vertexFactory.decrementNumberOfRouters();
            throw new IllegalStateException("user hit cancel");
        }

        //Router router = new Router(resultObject.getName(),resultObject.getQosMechanism(),resultObject.getSwQueues(),resultObject.getMaxTxBufferSize(),resultObject.getMaxIntputQueueSize(), resultObject.getMaxProcessingPackets(), resultObject.getTcpDelay());
        Router router = new Router();//todo use constructor above instead
        return router;
    }

    /**
     * shows dialog with computer configuration
     */
    public Computer showComputerConfigurationDialog() {
        logg.debug("showing computer configuration dialog");
        String computerName = vertexFactory.createComputerName();
        BlockingDialog bl = new ComputerConfigurationDialog(computerName);
        bl.showDialog();
        ComputerConfigurationDialog.ResultObject obj = (ComputerConfigurationDialog.ResultObject) bl.getUserInput();
        if (obj == null) {
            vertexFactory.decrementNumberOfRouters();
            throw new IllegalStateException("user hit cancel");
        }

        //Computer computer = new Computer(resultObject.getName(), resultObject.getQosMechanism(), resultObject.getSwQueues(), resultObject.getMaxTxBufferSize(), resultObject.getMaxIntputQueueSize(), resultObject.getMaxProcessingPackets(), resultObject.getTcpDelay());
        Computer computer = new Computer();//todo use constructor above instead

        return computer;
    }

    /**
     * shows dialog with switch configuration
     */
    public Switch showSwitchConfigurationDialog() {
        logg.debug("showing switch configuration dialog");
        String switchName = vertexFactory.createSwitchName();
//        BlockingDialog bl = new SwitchConfigurationDialog(switchName);
//        bl.showDialog();
//        Object obj = bl.getUserInput();
//        if (obj == null) {
//            VertexFactory.getInstance().decrementNumberOfRouters();
//            throw new IllegalStateException("user hit cancel");
//        }
        //Switch sw = new Switch(resultObject.getName(), resultObject.getQosMechanism(), resultObject.getSwQueues(), resultObject.getMaxTxBufferSize(), resultObject.getMaxIntputQueueSize(), resultObject.getMaxProcessingPackets(), resultObject.getTcpDelay());
        Switch sw = new Switch();//todo use constructor above instead
        return sw;
    }

    /**
     * shows dialog with router configuration
     *
     * @param edge edge with default values depending on user choice
     * (Ethernet/FastEthernet/GigabitEthernet/Custom)
     */
    public Edge showEdgeConfigurationDialog(Edge edge) {
        BlockingDialog bl = new EdgeConfigurationDialog(edge);
        bl.showDialog();
        EdgeConfigurationDialog.ResultObject resultObject = (EdgeConfigurationDialog.ResultObject) bl.getUserInput();
        if (resultObject == null) {
            throw new IllegalStateException("user hit cancel");
        }

        edge.setLength(resultObject.getLength());
        edge.setSpeed(resultObject.getSpeed());

        return edge;
    }
}
