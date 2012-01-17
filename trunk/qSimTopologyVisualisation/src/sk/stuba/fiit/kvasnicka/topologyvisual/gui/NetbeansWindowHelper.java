/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui;

import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.openide.windows.TopComponent;
import sk.stuba.fiit.kvasnicka.topologyvisual.Topology;
import sk.stuba.fiit.kvasnicka.topologyvisual.palette.gui.TopolElementTopComponent;

/**
 *
 * @author Igor Kvasnicka
 */
public class NetbeansWindowHelper {

    private final static NetbeansWindowHelper INSTANCE = new NetbeansWindowHelper();
    private static Logger logg = Logger.getLogger(NetbeansWindowHelper.class);
    private TopolElementTopComponent activeTopologyTopComponent;

    public static NetbeansWindowHelper getInstance() {
        return INSTANCE;
    }

    private NetbeansWindowHelper() {
    }

    public Topology getActiveTopComponentTopology() {
        TopolElementTopComponent t = getActiveTopoloElementTopComp();
        if (t == null) {
            logg.error("topCompoenent is NULL");
            return null;
        }
        return t.getTopology();
    }

    public void setActiveTopologyTopComponent(TopolElementTopComponent activeTopologyTopComponent) {
        this.activeTopologyTopComponent = activeTopologyTopComponent;
    }

    public TopolElementTopComponent getActiveTopoloElementTopComp() {
        return activeTopologyTopComponent;
    }

    public void clearActiveTopologyTopComponent() {
        activeTopologyTopComponent = null;
    }
}
