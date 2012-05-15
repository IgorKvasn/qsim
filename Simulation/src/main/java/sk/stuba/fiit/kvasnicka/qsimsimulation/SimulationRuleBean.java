/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.qsimsimulation;

import lombok.Getter;
import lombok.Setter;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.PacketTypeEnum;

/**
 * this bean represents user definition of a simulation rule
 *
 * @author Igor Kvasnicka
 */
@Getter
public class SimulationRuleBean {
    @Setter
    private boolean active;
    private int repeat;//-1 if infinity;
    private PacketTypeEnum packetTypeEnum;
    private NetworkNode source;
    private NetworkNode destination;
    /**
     * simulation time when this rule became active
     */
    private double activationTime; //(msec)
    /**
     * number of packets yet to create - this field changes
     *
     * @see #decreaseNumberOfPackets()
     */
    private int numberOfPackets;
    private int packetSize;

    /**
     * true if this rule is automatic = it starts when simulation starts
     * manual (non-automatic) rules starts when user says so
     */
    private boolean automatic;

    /**
     * creates new simulation rule
     * all arguments are self-explained
     *
     * @param source
     * @param destination
     * @param numberOfPackets
     * @param packetSize
     * @param automatic
     * @param activeDelay
     * @param repeat          -1 if infinity
     * @param packetTypeEnum
     */
    public SimulationRuleBean(NetworkNode source, NetworkNode destination, int numberOfPackets, int packetSize, boolean automatic, double activeDelay, int repeat, PacketTypeEnum packetTypeEnum) {
        this.activationTime = activeDelay;
        this.repeat = repeat;
        this.packetTypeEnum = packetTypeEnum;
        this.active = false;
        this.source = source;
        this.destination = destination;
        this.numberOfPackets = numberOfPackets;
        this.packetSize = packetSize;
        this.automatic = automatic;
    }

    /**
     * each rule has set number of repetitions. this method assures that rule is used exact number of times
     * it decreases repeat number of repeat count - when it reaches 0 simulation rule is set to "finished"
     */
    public void decreaseRuleRepetition() {
        if (repeat <= 0) return;//so when rule is finished (repeat=0) or if it is set to infinity (repeat=-1)
        repeat--;
    }

    public void decreaseNumberOfPackets() {
        numberOfPackets--;
    }

    /**
     * sometimes simulation rule needs more time quantums to be finished
     *
     * @param timeQuantum
     */
    public void increaseActivationTime(double timeQuantum) {
        activationTime += timeQuantum;
    }

    public boolean isFinished() {
        return repeat == 0 && numberOfPackets == 0;
    }
}
