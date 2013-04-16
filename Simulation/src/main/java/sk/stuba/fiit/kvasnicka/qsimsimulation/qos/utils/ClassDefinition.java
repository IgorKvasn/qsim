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

package sk.stuba.fiit.kvasnicka.qsimsimulation.qos.utils;

import lombok.Getter;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.IpPrecedence;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.PacketClassification;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.utils.dscp.DscpValuesEnum;

import java.io.Serializable;
import java.util.List;

/**
 * this is a definition of a QoS class
 * each class consists of a group of QoS queues
 *
 * @author Igor Kvasnicka
 */

public class ClassDefinition implements Serializable {
    private static final long serialVersionUID = 4352954903028246499L;

    @Getter
    private List<IpPrecedence> ipPrecedenceList = null;
    @Getter
    private List<DscpValuesEnum> dscpValuesEnums = null;
    @Getter
    private String name;
    private PacketClassification classification;

    public ClassDefinition(List<IpPrecedence> ipPrecedenceList, List<DscpValuesEnum> dscpValuesEnums, String name) {
        if (ipPrecedenceList == null && dscpValuesEnums == null) {
            throw new IllegalArgumentException("none of classifications is defined");
        }

        if (name == null) {
            this.name = "";
        } else {
            this.name = name;
        }
        this.ipPrecedenceList = ipPrecedenceList;
        this.dscpValuesEnums = dscpValuesEnums;
    }

    public ClassDefinition(List<IpPrecedence> ipPrecedenceList, List<DscpValuesEnum> dscpValuesEnums) {
        this(ipPrecedenceList, dscpValuesEnums, "N/A");
    }

    public List<Integer> getQueueNumbers() {
        if (classification==null) throw new IllegalStateException("classification is NULL");
        return classification.convertClassificationToQueue(ipPrecedenceList, dscpValuesEnums);
    }

    public void setClassification(PacketClassification classification) {
        this.classification = classification;
    }


//    public ClassDefinition(Integer... queueNumbers) {
//        if (queueNumbers == null) throw new IllegalArgumentException("queueNumbers is NULL");
//        if (queueNumbers.length == 0) throw new IllegalArgumentException("queueNumbers is empty");
//
//        this.queueNumbers = new ArrayList<Integer>(Arrays.asList(queueNumbers));
//    }

//    public ClassDefinition(String name, Integer... queueNumbers) {
//        if (name == null) {
//            this.name = "";
//        } else {
//            this.name = name;
//        }
//
//        if (queueNumbers == null) throw new IllegalArgumentException("queueNumbers is NULL");
//        if (queueNumbers.length == 0) throw new IllegalArgumentException("queueNumbers is empty");
//
//        this.queueNumbers = new ArrayList<Integer>(Arrays.asList(queueNumbers));
//    }
}
