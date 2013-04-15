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

package sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.utils.dscp;

import java.io.Serializable;

/**
 * @author Igor Kvasnicka
 */
public class DscpDefinition implements Serializable, Comparable<DscpDefinition> {

    private static final long serialVersionUID = - 2152606228943815485L;
    private final String query;
    private DscpValuesEnum dscpValue;

    public DscpDefinition(String query, DscpValuesEnum dscpValue) {
        this.query = query;
        this.dscpValue = dscpValue;
    }


    public DscpValuesEnum getDscpValue() {
        return dscpValue;
    }

    public String getQuery() {
        return query;
    }

    @Override
    public int compareTo(DscpDefinition dscpDefinition) {
        return this.dscpValue.compareTo(dscpDefinition.dscpValue) *-1;
    }
}
