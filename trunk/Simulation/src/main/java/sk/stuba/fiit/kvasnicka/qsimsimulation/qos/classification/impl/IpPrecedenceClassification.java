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

package sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.impl;

import lombok.Getter;
import org.apache.log4j.Logger;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.IpPrecedence;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.PacketClassification;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.utils.ClassificationException;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.utils.ClassificationUtil;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.utils.ParameterException;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.utils.QosUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Igor Kvasnicka
 */
public class IpPrecedenceClassification extends PacketClassification {
    private static final long serialVersionUID = 5340123815747300402L;

    private static Logger logg = Logger.getLogger(IpPrecedenceClassification.class);


    public static final String IP_DEFINITIONS = "dscp_definitions";
    public static final String OUTPUT_QUEUE_COUNT = "output_queue_count";
    public static final String NOT_DEFINED_QUEUE = "not_defined_queue";

    @Getter
    private final IpDefinition[] definitions;
    @Getter
    private IpDefinition notDefinedQueue;
    private HashMap<IpPrecedence, Integer> convertIpToQueue = new HashMap<IpPrecedence, Integer>();

    public IpPrecedenceClassification(HashMap<String, Object> parameters) {
        super(parameters);
        IpDefinition[] defs;
        int outputQueueNumber;
        IpDefinition notDefinedQueue;
        try {
            QosUtils.checkParameter(parameters, IpDefinition[].class, IP_DEFINITIONS);
            QosUtils.checkParameter(parameters, Integer.class, OUTPUT_QUEUE_COUNT);
            QosUtils.checkParameter(parameters, IpDefinition.class, NOT_DEFINED_QUEUE);

            defs = (IpDefinition[]) parameters.get(IP_DEFINITIONS);
            outputQueueNumber = (Integer) parameters.get(OUTPUT_QUEUE_COUNT);
            notDefinedQueue = (IpDefinition) parameters.get(NOT_DEFINED_QUEUE);
        } catch (ParameterException e) {
            throw new IllegalStateException(e);
        }


        definitions = new IpDefinition[defs.length];
        System.arraycopy(defs, 0, definitions, 0, defs.length);
        Arrays.sort(definitions);
        this.notDefinedQueue = notDefinedQueue;

        Set<IpPrecedence> enums = new TreeSet<IpPrecedence>(new Comparator<IpPrecedence>() {
            @Override
            public int compare(IpPrecedence ipValuesEnum, IpPrecedence ipValuesEnum2) {
                return ipValuesEnum.compareTo(ipValuesEnum2) * - 1;
            }
        });
        for (IpDefinition def : definitions) {
            enums.add(def.ipPrecedence);
        }
        enums.add(notDefinedQueue.ipPrecedence);

        int queue = 0;
        for (IpPrecedence e : enums) {
            if (queue >= outputQueueNumber) {//there are more definitions then output queues
                convertIpToQueue.put(e, outputQueueNumber - 1);
            } else {
                convertIpToQueue.put(e, queue);
                queue++;
            }
        }
    }

    @Override
    public int classifyAndMarkPacket(NetworkNode networkNode, Packet packet) {
        if (packet == null) {
            throw new IllegalArgumentException("packet is NULL");
        }
        if (packet == null) throw new IllegalArgumentException("packet is NULL");
        for (IpDefinition def : definitions) {
            try {
                if (ClassificationUtil.isClassificationRuleApplied(def.getAcl(), packet)) {
                    return convertIpToQueue.get(def.getIpPrecedence());
                }
            } catch (ClassificationException e) {
                logg.error(e.getMessage());
            }
        }
        //none of the DSCP definitions were satisfied
        return convertIpToQueue.get(notDefinedQueue.getIpPrecedence());
    }

    @Getter
    public static class IpDefinition implements Serializable, Comparable<IpDefinition> {
        private static final long serialVersionUID = 393926449914492190L;
        private IpPrecedence ipPrecedence;
        private String acl;

        public IpDefinition(IpPrecedence ipPrecedence, String acl) {
            this.ipPrecedence = ipPrecedence;
            this.acl = acl;
        }

        @Override
        public int compareTo(IpDefinition ipDefinition) {
            return this.ipPrecedence.compareTo(ipDefinition.getIpPrecedence()) *-1;
        }
    }
}
