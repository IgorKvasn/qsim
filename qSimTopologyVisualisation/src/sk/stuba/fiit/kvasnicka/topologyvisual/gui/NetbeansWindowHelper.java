/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.apache.log4j.Logger;
import org.openide.windows.TopComponent;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.events.TopologyWindowChangeEvent;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.events.TopologyWindowChangeListener;
import sk.stuba.fiit.kvasnicka.topologyvisual.topology.Topology;
import sk.stuba.fiit.kvasnicka.topologyvisual.palette.gui.TopologyMultiviewElement;

/**
 *
 * @author Igor Kvasnicka
 */
public class NetbeansWindowHelper {

    private final static NetbeansWindowHelper INSTANCE = new NetbeansWindowHelper();
    private static Logger logg = Logger.getLogger(NetbeansWindowHelper.class);
    private TopologyMultiviewElement activeTopologyMultiviewElement;

    public static NetbeansWindowHelper getInstance() {
        return INSTANCE;
    }

    private NetbeansWindowHelper() {
    }

    /**
     * retrieves Topology object from currently active TopologyMultiviewElement
     * window.
     *
     * @return returns null if no active TopologyMultiviewElement window
     */
    public Topology getActiveTopology() {
        TopologyMultiviewElement t = getActiveTopologyMultiviewElement();
        if (t == null) {
            logg.info("activeTopologyTopComponent is NULL");
            return null;
        }
        return t.getTopology();
    }

    public void setActiveTopologyMultiviewElement(TopologyMultiviewElement activeTopologyTopComponent) {
        this.activeTopologyMultiviewElement = activeTopologyTopComponent;
        fireTopologyWindowChangeOccurred(new TopologyWindowChangeEvent(this));
    }

    public TopologyMultiviewElement getActiveTopologyMultiviewElement() {
        return activeTopologyMultiviewElement;
    }

    public void clearActiveTopologyMultiviewElement() {
        activeTopologyMultiviewElement = null;
        fireTopologyWindowChangeOccurred(new TopologyWindowChangeEvent(this));
    }
    //----listeners
    private javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();

    public void addTopologyWindowChangeListener(TopologyWindowChangeListener listener) {
        listenerList.add(TopologyWindowChangeListener.class, listener);
    }

    public void removeTopologyWindowChangeListener(TopologyWindowChangeListener listener) {
        listenerList.remove(TopologyWindowChangeListener.class, listener);
    }

    private synchronized void fireTopologyWindowChangeOccurred(TopologyWindowChangeEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        // Each listener occupies two elements - the first is the listener class
        // and the second is the listener instance
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == TopologyWindowChangeListener.class) {
                ((TopologyWindowChangeListener) listeners[i + 1]).topologyWindowChangeOccurred(evt);
            }
        }
    }
}
