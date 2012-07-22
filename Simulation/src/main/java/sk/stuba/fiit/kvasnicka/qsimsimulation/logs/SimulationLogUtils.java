/*******************************************************************************
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
 ******************************************************************************/

package sk.stuba.fiit.kvasnicka.qsimsimulation.logs;

import sk.stuba.fiit.kvasnicka.qsimsimulation.events.log.SimulationLogEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.log.SimulationLogListener;

/**
 * util class handling simulation logs
 *
 * @author Igor Kvasnicka
 */
public final class SimulationLogUtils {
    public static final String SOURCE_GENERAL = "<GENERAL>";

    private transient javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();


    public SimulationLogUtils() {
    }


    /**
     * logs new simulation log and notifies all SimulationLogListener
     *
     * @param log
     */
    public void log(SimulationLog log) {
        firePingRuleAddedEvent(new SimulationLogEvent(this, log));
    }


    public void addSimulationLogListener(SimulationLogListener listener) {
        listenerList.add(SimulationLogListener.class, listener);
    }

    public void removeSimulationLogListener(SimulationLogListener listener) {
        listenerList.remove(SimulationLogListener.class, listener);
    }

    private void firePingRuleAddedEvent(SimulationLogEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i].equals(SimulationLogListener.class)) {
                ((SimulationLogListener) listeners[i + 1]).simulationLogOccurred(evt);
            }
        }
    }
}
