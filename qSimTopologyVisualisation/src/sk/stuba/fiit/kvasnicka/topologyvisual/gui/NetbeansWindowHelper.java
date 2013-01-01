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
package sk.stuba.fiit.kvasnicka.topologyvisual.gui;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import org.apache.log4j.Logger;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.openide.util.*;
import org.openide.windows.TopComponent;
import sk.stuba.fiit.kvasnicka.topologyvisual.events.multiview.MultiviewChangedEvent;
import sk.stuba.fiit.kvasnicka.topologyvisual.events.multiview.MultiviewChangedListener;
import sk.stuba.fiit.kvasnicka.topologyvisual.filetype.descriptors.TopologyVisualisationDescription;
import sk.stuba.fiit.kvasnicka.topologyvisual.filetype.gui.TopologyVisualisation;
import sk.stuba.fiit.kvasnicka.topologyvisual.topology.Topology;

/**
 *
 * @author Igor Kvasnicka
 */
public class NetbeansWindowHelper {

    private final static NetbeansWindowHelper INSTANCE = new NetbeansWindowHelper();
    private static Logger logg = Logger.getLogger(NetbeansWindowHelper.class);
    private transient javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();
    
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
        TopologyVisualisation t = getActiveTopologyVisualisation();
        if (t == null) {
            logg.info("activeTopologyTopComponent is NULL");
            return null;
        }
        return t.getTopology();
    }

    /**
     * sets specified TopologyVisualisation to active state
     *
     * @param activeVisual
     */
    public void activateWindow(TopologyVisualisation activeVisual) {
        logg.debug("activating window " + activeVisual.getDataObject().getPrimaryFile().getNameExt());
        //to make sure I first go through all opened TopolVisuals and set them as not active
        TopComponent.Registry registry = TopComponent.getRegistry();
        Set<TopComponent> opened = registry.getOpened();
        for (TopComponent tc : opened) {
            MultiViewHandler mh = MultiViews.findMultiViewHandler(tc);
            if (mh == null) {//it is not a multiview top component
                continue;
            }
            if (mh.getPerspectives().length != 2) {//double-check that it is really what I want
                continue;
            }
            if (!("TopologyInfoMultiview".equals(mh.getPerspectives()[0].preferredID()))) {
                continue;
            }
            if (!("TopologyVisualisationDescription".equals(mh.getPerspectives()[1].preferredID()))) {
                continue;
            }
            //allright now, I am 100% sure it is what I am looking for :)
            if ("TopologyVisualisationDescription".equals(mh.getSelectedPerspective().preferredID())) {
                TopologyVisualisation topolVisual = getTopologyVisualisationByReflection(mh.getSelectedPerspective());
                topolVisual.setActive(false);
            }
        }
        //now set specified TopologyVisual as active
        activeVisual.setActive(true);

    }

    /**
     * returns null if no Active topology visualisation Element found
     *
     * @return
     */
    public TopologyVisualisation getActiveTopologyVisualisation() {
        TopComponent.Registry registry = TopComponent.getRegistry();
        Set<TopComponent> opened = registry.getOpened();
        for (TopComponent tc : opened) {
            MultiViewHandler mh = MultiViews.findMultiViewHandler(tc);
            if (mh == null) {//it is not a multiview top component
                continue;
            }
            if (mh.getPerspectives().length != 2) {//double-check that it is really what I want
                continue;
            }
            if (!("TopologyInfoMultiview".equals(mh.getPerspectives()[0].preferredID()))) {
                continue;
            }
            if (!("TopologyVisualisationDescription".equals(mh.getPerspectives()[1].preferredID()))) {
                continue;
            }
            //allright now, I am 100% sure it is what I am looking for :)
            if ("TopologyVisualisationDescription".equals(mh.getSelectedPerspective().preferredID())) {
                TopologyVisualisation topolVisual = getTopologyVisualisationByReflection(mh.getSelectedPerspective());
                if (topolVisual.isActive()) {
                    return topolVisual;
                } else {
                    continue;
                }
            }
        }
        logg.warn("No active TopologyVisualisation found");
        return null;
    }

    private TopologyVisualisation getTopologyVisualisationByReflection(MultiViewPerspective selectedPerspective) {
        try {
            Method privateStringMethod = MultiViewPerspective.class.getDeclaredMethod("getDescription");
            privateStringMethod.setAccessible(true);
            TopologyVisualisationDescription returnValue = (TopologyVisualisationDescription) privateStringMethod.invoke(selectedPerspective);
            if (returnValue == null) {
                return null;
            }
            return returnValue.getTopologyVisualisation();
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SecurityException ex) {
            Exceptions.printStackTrace(ex);
        }
        throw new IllegalStateException("could not retrieve active TopologyVisualisation");
    }

    public void addMultiviewChangedListener(MultiviewChangedListener listener) {
        listenerList.add(MultiviewChangedListener.class, listener);
    }

    public void removeMultiviewChangedListener(MultiviewChangedListener listener) {
        listenerList.remove(MultiviewChangedListener.class, listener);
    }

    private void fireMultiviewChangedEvent(MultiviewChangedEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i].equals(MultiviewChangedListener.class)) {
                ((MultiviewChangedListener) listeners[i + 1]).multiviewChangedOccurred(evt);
            }
        }
    }
}
