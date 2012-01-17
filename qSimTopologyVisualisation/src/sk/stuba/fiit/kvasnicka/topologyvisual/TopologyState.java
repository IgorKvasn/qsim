/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual;

import org.apache.log4j.Logger;
import org.openide.awt.StatusDisplayer;

/**
 *
 * @author Igor Kvasnicka
 */
public abstract class TopologyState {

    private static Logger logg = Logger.getLogger(TopologyState.class);
    private static State topologyState = State.NORMAL;

    public static State getTopologyState() {
        return topologyState;
    }

    public static void setTopologyState(State newState) {
        logg.debug("topology state has been changed from " + topologyState + " to " + newState);
        if (newState.statusBarText != null) {
            StatusDisplayer.getDefault().setStatusText(newState.statusBarText);
        }
        TopologyState.topologyState = newState;
    }

    public static enum State {

        NORMAL(null),//no special state
        SIMULATION("Simulating"), //simulation in progress
        ROUTE_EDITING("Create or edit routes"),//creating/editing routes
        ROUTING_SOURCE_SELECTED("Select route desitnation")//user is defining routing path; he has already selecte source node
        ;
        private String statusBarText;

        private State(String statusBarText) {
            if (statusBarText == null) {
                statusBarText = "";
            }
            this.statusBarText = statusBarText;

        }
    }
}
