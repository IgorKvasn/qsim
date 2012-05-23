package sk.stuba.fiit.kvasnicka.qsimsimulation.packet;

import lombok.Getter;
import lombok.Setter;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.SimulationRuleBean;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PacketManager;

/**
 * @author Igor Kvasnicka
 */
public class PingPacket extends Packet {
    @Getter
    @Setter
    public double roundTriptime = 0;


    public PingPacket(int size, NetworkNode destination, NetworkNode source, Layer4TypeEnum layer4, PacketManager packetManager, SimulationRuleBean simulationRule, double creationTime) {
        super(size, destination, source, layer4, packetManager, simulationRule, creationTime);
    }
}
