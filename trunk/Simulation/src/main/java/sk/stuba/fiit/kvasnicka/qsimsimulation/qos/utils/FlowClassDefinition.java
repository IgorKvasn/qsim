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
import sk.stuba.fiit.kvasnicka.qsimsimulation.exceptions.ClassDefinitionException;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.utils.ClassificationException;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.utils.ClassificationUtil;

import java.io.Serializable;

/**
 * similar to ClassDefinition class, but for flow-based QoS mechanisms
 *
 * @author Igor Kvasnicka
 */
public class FlowClassDefinition implements Serializable {
    private static String[] forbiddenPacketFields = new String[]{"size", "ipTos", "dscp", "protocol"};
    private static final long serialVersionUID = 2952133632407116099L;
    @Getter
    private String name;
    @Getter
    private String acl;

    public FlowClassDefinition(String name, String acl) throws ClassDefinitionException {
        checkForbiddenFields(acl);
        this.name = name;
        this.acl = acl;
    }

    public static void checkForbiddenFields(String acl) throws ClassDefinitionException {
        for (String forbidden:forbiddenPacketFields){
            if (acl.contains(forbidden)) throw new ClassDefinitionException("ACL contains one or more forbidden fields.");
        }
    }

    public boolean isPacketInFlow(Packet packet) throws ClassificationException {
        return ClassificationUtil.isClassificationRuleApplied(acl, packet);
    }
}
