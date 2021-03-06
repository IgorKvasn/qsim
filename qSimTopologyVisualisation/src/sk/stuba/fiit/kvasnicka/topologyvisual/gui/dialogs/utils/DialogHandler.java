/*
 * This file is part of qSim.
 *
 * qSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * qSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with qSim.  If not, see <http://www.gnu.org/licenses/>.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.utils;

import org.apache.log4j.Logger;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Computer;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Switch;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.utils.VertexFactory;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.topology.SwitchConfigurationDialog;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.topology.ComputerConfigurationDialog;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.topology.EdgeConfigurationDialog;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.topology.RouterConfigurationDialog;

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
        Router resultObject = (Router) bl.getUserInput();
        if (resultObject == null) {
            vertexFactory.decrementNumberOfRouters();
            throw new IllegalStateException("user hit cancel");
        }

        return resultObject;
    }

    /**
     * shows dialog with router configuration
     *
     * null if user hit cancel
     */
    public Router showRouterConfigurationDialog(Router router) {
        BlockingDialog bl = new RouterConfigurationDialog(router, router.getName(),false);
        bl.showDialog();
        Router resultObject = (Router) bl.getUserInput();
        //user hitting cancel is handled elsewhere
        return resultObject;
    }

    /**
     * shows dialog with computer configuration
     */
    public Computer showComputerConfigurationDialog() {      
        String computerName = vertexFactory.createComputerName();
        BlockingDialog bl = new ComputerConfigurationDialog(computerName);
        bl.showDialog();
        Computer resultObject = (Computer) bl.getUserInput();
        if (resultObject == null) {
            vertexFactory.decrementNumberOfRouters();
            throw new IllegalStateException("user hit cancel");
        }
        return resultObject;
    }
    
       /**
     * shows dialog with computer configuration
     *
     * null if user hit cancel
     */
    public Computer showComputerConfigurationDialog(Computer computer) {
        BlockingDialog bl = new ComputerConfigurationDialog(computer, computer.getName(), false);
        bl.showDialog();
        Computer resultObject = (Computer) bl.getUserInput();
        //user hitting cancel is handled elsewhere
        return resultObject;
    }

   /**
     * shows dialog with router configuration
     */
    public Switch showSwitchConfigurationDialog() {
        String switchName = vertexFactory.createSwitchName();
        BlockingDialog bl = new SwitchConfigurationDialog(switchName);
        bl.showDialog();
        Switch resultObject = (Switch) bl.getUserInput();
        if (resultObject == null) {
            vertexFactory.decrementNumberOfSwitches();
            throw new IllegalStateException("user hit cancel");
        }

        return resultObject;
    }

    /**
     * shows dialog with router configuration
     *
     * null if user hit cancel
     */
    public Switch showSwitchConfigurationDialog(Switch witch) {
        BlockingDialog bl = new SwitchConfigurationDialog(witch, witch.getName(),false);
        bl.showDialog();
        Switch resultObject = (Switch) bl.getUserInput();
        //user hitting cancel is handled elsewhere
        return resultObject;
    }

    /**
     * shows dialog with router configuration
     *
     */
    public Edge showEdgeConfigurationDialog(long defaultSpeed, NetworkNode node1, NetworkNode node2) {
        EdgeConfigurationDialog bl = new EdgeConfigurationDialog(node1, node2, defaultSpeed);
        bl.showDialog();
        Edge resultObject = bl.getUserInput();
        if (resultObject == null) {
            throw new IllegalStateException("user hit cancel");
        }

        return resultObject;
    }
}
