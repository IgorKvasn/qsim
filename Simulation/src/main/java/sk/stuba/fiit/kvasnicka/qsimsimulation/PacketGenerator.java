package sk.stuba.fiit.kvasnicka.qsimsimulation;

import org.apache.log4j.Logger;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.PacketTypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.helpers.DelayHelper;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PacketManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;

import java.util.LinkedList;
import java.util.List;

/**
 * this class is used to generate packets according to simulation rules
 *
 * @author Igor Kvasnicka
 */
public class PacketGenerator {
    private static final Logger logg = Logger.getLogger(PacketGenerator.class);

    private List<SimulationRuleBean> simulationRules;
    private SimulationTimer simulationTimer;
    private PacketManager packetManager;


    public PacketGenerator(List<SimulationRuleBean> simulationRules, SimulationTimer simulationTimer, List<Edge> edgeList, List<NetworkNode> nodeList) {
        this.simulationRules = simulationRules;
        this.simulationTimer = simulationTimer;
        packetManager = simulationTimer.getPacketManager();
    }

    /**
     * creates new packets for all network nodes
     *
     * @param simulationTime current simulation time
     * @param timeQuantum    time quantum - this is used when creating new packets to not create all packets at once, but as many as time quantum allows
     */
    public void generatePackets(double simulationTime, double timeQuantum) {
        //todo paralelize me - ExecutorService
        for (SimulationRuleBean rule : simulationRules) {
            if (rule.isFinished()) continue; //I don't care about finished simulation rules
            if (rule.isActive()) {//rule has been activated and it is not finished yet
                addPacketsToNetworkNode(timeQuantum, rule.getActivationTime(), rule);
            } else {//check if the time came to activate this rule
                if (checkRuleActivate(rule, simulationTime)) {//yes, I should activate it
                    rule.setActive(true);
                    addPacketsToNetworkNode(timeQuantum, rule.getActivationTime(), rule);
                    rule.increaseActivationTime(timeQuantum);
                }
            }
            //after the rule is used I will take care of rule repetition
            rule.decreaseRuleRepetition();
        }
    }

    private void addPacketsToNetworkNode(double timeQuantum, double creationTime, SimulationRuleBean rule) {
        List<Packet> packets = generatePacketsFromSimulRule(rule, timeQuantum);
        packetManager.initPackets(rule.getSource(), packets, creationTime);
    }

    /**
     * checks if not active rule should became active (initial delay has expired)
     *
     * @param rule
     * @param simulationTime
     * @return true if rule should be set to active state
     */
    private boolean checkRuleActivate(SimulationRuleBean rule, double simulationTime) {
        return simulationTime >= rule.getActivationTime();
    }

    /**
     * creates new packets for one simulation rule
     * <p/>
     * creates as much packets as possible - each packet takes some time ("serialisationDelay")
     * to create and there is only a small amount of time ("timeQuantum") to work with
     *
     * @param rule
     * @param timeQuantum
     * @return
     */
    private List<Packet> generatePacketsFromSimulRule(SimulationRuleBean rule, double timeQuantum) {
        List<Packet> packets = new LinkedList<Packet>();
        double timeSpent = 0;
        while (timeSpent <= timeQuantum && rule.getNumberOfPackets() > 0) {
            double creationDelay = DelayHelper.calculatePacketCreationDelay(rule.getSource(), rule.getPacketSize(), rule.getPacketTypeEnum());
            if (timeSpent + creationDelay > timeQuantum) break; //no time left to spent
            packets.add(createPacket(rule.getPacketSize(), rule.getDestination(), rule.getSource(), rule.getPacketTypeEnum(), rule.getActivationTime() + timeSpent));
            timeSpent += creationDelay;//I have spent some time
            rule.decreaseNumberOfPackets();
        }
        logg.debug("Packets created: " + packets.size());
        return packets;
    }

    /**
     * creates one packet
     *
     * @param packetSize
     * @param destination
     * @param source
     * @param packetTypeEnum
     * @param creationTime
     * @return
     */
    private Packet createPacket(int packetSize, NetworkNode destination, NetworkNode source, PacketTypeEnum packetTypeEnum, double creationTime) {
        Packet packet = new Packet(packetSize, destination, source, packetManager, packetTypeEnum, creationTime);
        int mark = source.getQosMechanism().markPacket(packet); //todo mark delay
        packet.setQosQueue(mark);
        return packet;
    }
}
