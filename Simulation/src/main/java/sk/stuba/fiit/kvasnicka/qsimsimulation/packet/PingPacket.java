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

package sk.stuba.fiit.kvasnicka.qsimsimulation.packet;

import org.apache.log4j.Logger;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PacketManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PingManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

/**
 * @author Igor Kvasnicka
 */
public class PingPacket extends Packet {
    private static Logger logg = Logger.getLogger(PingPacket.class);

    private NetworkNode originalSourceNetworknode;
    private PingManager pingManager;
    private String originalSimulRuleID;


    public PingPacket(PingManager pingManager, int size, PacketManager packetManager, SimulationRuleBean simulationRule, double creationTime) {
        super(size, Layer4TypeEnum.ICMP, packetManager, simulationRule, creationTime);
        this.pingManager = pingManager;
        originalSourceNetworknode = simulationRule.getSource();
        originalSimulRuleID = simulationRule.getUniqueID();
    }

    /**
     * switches source node and destination node
     * this happens when ping packet is send from A-B and then this packet should be send back to A
     */
    private void switchSourceDestination() {
        if (logg.isDebugEnabled()) {
            logg.debug("switching source <-> destination");
        }
        //assign new simulation rule
        simulationRule = pingManager.createBackRule(getSimulationRule());
    }

    private void restoreSourceDestination() {
        if (logg.isDebugEnabled()) {
            logg.debug("restoring source <-> destination");
        }
        //assign original simulation rule
        simulationRule = pingManager.getOriginalRule(originalSimulRuleID);
    }

    @Override
    public boolean isPacketDelivered(NetworkNode currentNode) {
        boolean delivered = super.isPacketDelivered(currentNode);
        if (delivered) {
            if (currentNode.equals(originalSourceNetworknode)) {//is a ping packet really delivered?
                restoreSourceDestination();
                return true;
            } else {//this is only a half way to victory; ping from A to B just came to B, but it has to be send back to A
                switchSourceDestination();
                return false; //this is not the end, yet
            }
        }
        return delivered;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode(){
        return super.hashCode();
    }
}
