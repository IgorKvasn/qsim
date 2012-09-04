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
public class DscpDefinition implements Serializable {

    private static final long serialVersionUID = - 5681956667334834337L;
    private final String query;
    private int queueNumber;

    public DscpDefinition(String query, int queueNumber) {
        if (queueNumber < 0) {
            throw new IllegalArgumentException("queue number is below 0");
        }
        this.query = query;
        this.queueNumber = queueNumber;
    }


    public int getQueueNumber() {
        return queueNumber;
    }

    public String getQuery() {
        return query;
    }
}
