package sk.stuba.fiit.kvasnicka.topologyvisual.dialogs.utils;

import org.apache.log4j.Logger;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Computer;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Switch;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.utils.VertexFactory;
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
        Router router = vertexFactory.createRouter();
        BlockingDialog bl = new RouterConfigurationDialog(router);
        bl.showDialog();
        RouterConfigurationDialog.ResultObject resultObject = (RouterConfigurationDialog.ResultObject) bl.getUserInput();
        if (resultObject == null) {
            vertexFactory.decrementNumberOfRouters();
            throw new IllegalStateException("user hit cancel");
        }

        router.setName(resultObject.getName());

        return router;
    }

    /**
     * shows dialog with computer configuration
     */
    public Computer showComputerConfigurationDialog() {
        logg.debug("showing computer configuration dialog");
        Computer computer = vertexFactory.createComputer();
        BlockingDialog bl = new ComputerConfigurationDialog(computer);
        bl.showDialog();
        ComputerConfigurationDialog.ResultObject obj = (ComputerConfigurationDialog.ResultObject) bl.getUserInput();
        if (obj == null) {
            vertexFactory.decrementNumberOfRouters();
            throw new IllegalStateException("user hit cancel");
        }

        computer.setName(obj.getName());

        return computer;
    }

    /**
     * shows dialog with switch configuration
     */
    public NetworkNode showSwitchConfigurationDialog() {
        logg.debug("showing switch configuration dialog");
        Switch sw = vertexFactory.createSwitch();
//        BlockingDialog bl = new SwitchConfigurationDialog(sw);
//        bl.showDialog();
//        Object obj = bl.getUserInput();
//        if (obj == null) {
//            VertexFactory.getInstance().decrementNumberOfRouters();
//            throw new IllegalStateException("user hit cancel");
//        }
        return sw;
    }

    /**
     * shows dialog with router configuration
     *
     * @param edge edge with default values depending on user choice (Ethernet/FastEthernet/GigabitEthernet/Custom)
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
