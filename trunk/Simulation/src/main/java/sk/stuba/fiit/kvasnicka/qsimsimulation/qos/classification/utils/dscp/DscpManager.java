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

import lombok.Getter;
import org.apache.log4j.Logger;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.utils.ClassificationException;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.utils.ClassificationUtil;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Igor Kvasnicka
 */
public class DscpManager implements Serializable {

    private static final long serialVersionUID = - 2613638503370201116L;
    @Getter
    private final DscpDefinition[] definitions;
    @Getter
    private DscpValuesEnum notDefinedQueue;
    private HashMap<DscpValuesEnum, Integer> convertDscpToQueue = new HashMap<DscpValuesEnum, Integer>();
    private static Logger logg = Logger.getLogger(DscpManager.class);

    /**
     * creates new DSCP manager that handles all packets and marks them using DSCP definitions
     *
     * @param dscpDefinitions DSCP definitions
     * @param notDefinedQueue queue number where to put packets that does not meet definitions above
     */
    public DscpManager(DscpDefinition[] dscpDefinitions, DscpValuesEnum notDefinedQueue, int outputQueueNumber) {
        definitions = new DscpDefinition[dscpDefinitions.length];
        System.arraycopy(dscpDefinitions, 0, definitions, 0, dscpDefinitions.length);
        Arrays.sort(definitions);
        this.notDefinedQueue = notDefinedQueue;

        Set<DscpValuesEnum> enums = new TreeSet<DscpValuesEnum>(new Comparator<DscpValuesEnum>() {
            @Override
            public int compare(DscpValuesEnum dscpValuesEnum, DscpValuesEnum dscpValuesEnum2) {
                return dscpValuesEnum.compareTo(dscpValuesEnum2) * - 1;
            }
        });
        for (DscpDefinition def : definitions) {
            enums.add(def.getDscpValue());
        }
        enums.add(notDefinedQueue);

        int queue = 0;
        for (DscpValuesEnum e : enums) {
            if (queue >= outputQueueNumber) {//there are more definitions then output queues
                convertDscpToQueue.put(e, outputQueueNumber - 1);
            } else {
                convertDscpToQueue.put(e, queue);
                queue++;
            }
        }
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
                    packet.setMarking(null, def.getDscpValue());
                    return convertDscpToQueue.get(def.getDscpValue());
                }
            } catch (ClassificationException e) {
                logg.error(e.getMessage());
            }
        }
        //none of the DSCP definitions were satisfied
        packet.setMarking(null, notDefinedQueue);
        return convertDscpToQueue.get(notDefinedQueue);
    }

    public Integer convertDscpToQueue(DscpValuesEnum dscp) {
        try {
            return convertDscpToQueue.get(dscp);
        } catch (Exception e) {
            return 0;
        }
    }
}
