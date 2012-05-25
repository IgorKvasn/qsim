package sk.stuba.fiit.kvasnicka.qsimsimulation.packet;

import org.apache.log4j.Logger;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.SimulationRuleBean;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PacketManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.ping.PingManager;

/**
 * @author Igor Kvasnicka
 */
public class PingPacket extends Packet {
    private static final Logger logg = Logger.getLogger(PingPacket.class);

    private NetworkNode originalSourceNetworknode;
    private PingManager pingManager;
    private String originalSimulRuleID;


    public PingPacket(PingManager pingManager, int size, NetworkNode destination, NetworkNode source, Layer4TypeEnum layer4, PacketManager packetManager, SimulationRuleBean simulationRule, double creationTime) {
        super(size, destination, source, layer4, packetManager, simulationRule, creationTime);
        this.pingManager = pingManager;
        originalSourceNetworknode = simulationRule.getSource();
        originalSimulRuleID = simulationRule.getUniqueID();
    }

    /**
     * switches source node and destination node
     * this happens when ping packet is send from A-B and then this packet should be send back to A
     */
    private void switchSourceDestination() {
        logg.debug("switching source <-> destination");
        //switch nodes
        NetworkNode temp = destination;
        destination = source;
        source = temp;
        //assign new simulation rule
        simulationRule = pingManager.createBackRule(getSimulationRule());
    }

    private void restoreSourceDestination() {
        logg.debug("restoring source <-> destination");
        //switch nodes
        NetworkNode temp = destination;
        destination = source;
        source = temp;
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
}
