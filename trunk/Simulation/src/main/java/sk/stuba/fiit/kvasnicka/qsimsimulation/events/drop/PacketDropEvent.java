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

package sk.stuba.fiit.kvasnicka.qsimsimulation.events.drop;

import lombok.Getter;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

import java.util.EventObject;

/**
 * @author Igo
 */
public class PacketDropEvent extends EventObject {

    @Getter
    private final NetworkNode where;
    @Getter
    private final LocationEnum location;
    @Getter
    private final SimulationRuleBean rule;
    @Getter
    private final double when;


    public PacketDropEvent(Object source, NetworkNode where, LocationEnum location, SimulationRuleBean rule, double when) {
        super(source);
        this.where = where;
        this.location = location;
        this.rule = rule;
        this.when = when;
    }

    public enum LocationEnum {
        INPUT_QUEUE,
        OUTPUT_QUEUE
    }
}
