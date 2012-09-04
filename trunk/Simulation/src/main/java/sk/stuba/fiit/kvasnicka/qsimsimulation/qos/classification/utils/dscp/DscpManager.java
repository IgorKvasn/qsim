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

import org.apache.log4j.Logger;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.utils.ClassificationException;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.utils.ClassificationUtil;

import java.io.Serializable;

/**
 * @author Igor Kvasnicka
 */
public class DscpManager implements Serializable {

    private static final long serialVersionUID = - 2613638503370201116L;
    private final DscpDefinition[] definitions;
    private int notDefinedQueue;
    private static Logger logg = Logger.getLogger(DscpManager.class);

    /**
     * creates new DSCP manager that handles all packets and marks them using DSCP definitions
     *
     * @param dscpDefinitions DSCP definitions
     * @param notDefinedQueue queue number where to put packets that does not meet definitions above
     */
    public DscpManager(DscpDefinition[] dscpDefinitions, int notDefinedQueue) {
        if (notDefinedQueue < 0) throw new IllegalArgumentException("notDefinedQueue must not be lower than 0");
        definitions = new DscpDefinition[dscpDefinitions.length];
        System.arraycopy(dscpDefinitions, 0, definitions, 0, dscpDefinitions.length);
        this.notDefinedQueue = notDefinedQueue;
    }


    /**
     * determines QoS marking according to DSCP definitions
     *
     * @param packet
     * @return
     */
    public int determineMarkingByDscpDefinitions(Packet packet) {
        if (packet == null) throw new IllegalArgumentException("packet is NULL");
        for (DscpDefinition def : definitions) {
            try {
                if (ClassificationUtil.isClassificationRuleApplied(def.getQuery(), packet)) {
                    return def.getQueueNumber();
                }
            } catch (ClassificationException e) {
                logg.error(e.getMessage());
            }
        }
        //none of the DSCP definitions were satisfied
        return notDefinedQueue;
    }
}
