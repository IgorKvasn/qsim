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

package sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.utils.josql;

import org.josql.functions.AbstractFunctionHandler;
import org.josql.functions.NotFixedResults;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;

import java.util.List;

/**
 * @author Igor Kvasnicka
 */
public class MyFunctionHandler extends AbstractFunctionHandler implements NotFixedResults {

    public MyFunctionHandler() {
    }

    public Boolean destinationIn(List<String> nodesToSearch) {
        Packet packet = (Packet) this.q.getCurrentObject();
        return isNodeInList(packet.getDestination().getName(), nodesToSearch);
    }

    public Boolean destination(String node) {
        Packet packet = (Packet) this.q.getCurrentObject();

        if (packet.getDestination().getName().matches(node)) {
            return true;
        }

        return false;
    }

    public Boolean sourceIn(List<String> nodesToSearch) {
        Packet packet = (Packet) this.q.getCurrentObject();
        return isNodeInList(packet.getSource().getName(), nodesToSearch);
    }

    public Boolean source(String node) {
        Packet packet = (Packet) this.q.getCurrentObject();

        if (packet.getSource().getName().matches(node)) {
            return true;
        }
        return false;
    }

    private Boolean isNodeInList(String node, List<String> nodesToSearch) {
        for (String dest : nodesToSearch) {
            if (node.matches(dest)) {
                return true;
            }
        }
        return false;
    }
}