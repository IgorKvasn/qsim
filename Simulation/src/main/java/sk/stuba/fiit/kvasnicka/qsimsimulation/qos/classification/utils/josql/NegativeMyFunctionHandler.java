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

import java.util.List;

/**
 * @author Igor Kvasnicka
 */
public class NegativeMyFunctionHandler extends MyFunctionHandler {
    public Boolean notDestinationIn(List<String> nodesToSearch) {
        return ! super.destinationIn(nodesToSearch);
    }

    public Boolean notDestination(String node) {
        return ! super.destination(node);
    }

    public Boolean notSourceIn(List<String> nodesToSearch) {
        return ! super.sourceIn(nodesToSearch);
    }

    public Boolean notSource(String node) {
        return ! super.source(node);
    }
}
