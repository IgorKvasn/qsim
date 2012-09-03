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

package sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling.impl;

import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling.PacketScheduling;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.utils.ClassDefinition;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.utils.ParameterException;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.utils.QosUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Igor Kvasnicka
 */
public class ClassBasedWFQScheduling extends PacketScheduling {

    private static final long serialVersionUID = 953487501873159118L;
    private transient int currentClassNumber = 0; //number of currently processing class

    private transient int[] unprocessedPacketsInClass;
    private transient List<List<Integer>> savedBits; //first parameter is class number, second is queue number within the class

    public static final String CLASS_DEFINITIONS = "class_definitions";

    public ClassBasedWFQScheduling(Map<String, Object> parameters) {
        super(parameters);
        try {
            QosUtils.checkParameter(parameters, ClassDefinition[].class, CLASS_DEFINITIONS);
            QosUtils.checkClassDefinition((ClassDefinition[]) parameters.get(CLASS_DEFINITIONS));
        } catch (ParameterException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }


    @Override
    public List<Packet> decitePacketsToMoveFromOutputQueue(NetworkNode networkNode, List<List<Packet>> outputQueuePackets) {
        ClassDefinition[] classDefinitions = (ClassDefinition[]) parameters.get(CLASS_DEFINITIONS);

        unprocessedPacketsInClass = new int[classDefinitions.length];

        if (outputQueuePackets == null) throw new IllegalArgumentException("outputQueuePackets is NULL");

        if (outputQueuePackets.isEmpty()) {//there are no output queues??? are you serious????
            throw new IllegalStateException("no output queues defined - cannot perform packet scheduling");
        }
        int classCount = classDefinitions.length;

        savedBits = new LinkedList<List<Integer>>();
        for (int i = 0; i < classCount; i++) {
            List<Integer> qClass = new LinkedList<Integer>();
            for (int j = 0; j < classDefinitions[i].getQueueNumbers().size(); j++) {
                qClass.add(j);
            }
            savedBits.add(qClass);
        }

        for (int i = 0; i < unprocessedPacketsInClass.length; i++) {
            unprocessedPacketsInClass[i] = Integer.MAX_VALUE;//it is difficult and useless to calculate unprocessed packets - this will guarantee, that at least one round robin will be done
        }

        List<List<Packet>> outputQueuePacketsCopy = outputQueueMakeCopy(outputQueuePackets);


        List<Packet> packets = new LinkedList<Packet>();
        int inactiveQueue = 0;
        int startClass = currentClassNumber;
        int packetsToProcess = calculateBitsToProcess(calculateAllPacketsSize(outputQueuePacketsCopy), classCount);//how many packets can be processed in one round robin run

        while (true) {

            if (isClassEmpty(currentClassNumber)) {       //this queue has no more packets left
                inactiveQueue++;
            } else {
                //-----------perform inner WFQ
                List<List<Packet>> qosClass = extractQosClass(classDefinitions[currentClassNumber], outputQueuePacketsCopy);

                performClassWFQ(currentClassNumber, qosClass, packets, packetsToProcess, classCount);
                //--------------------------------------
            }
            currentClassNumber++;
            currentClassNumber %= classCount;
            if (currentClassNumber == startClass) {//I have performed one round robin circle

                if (inactiveQueue == classCount) {
                    break;//there are no more packets in output queue
                } else {
                    inactiveQueue = 0;
                }
            }
        }

        return packets;
    }

    /**
     * returns all queues within same class
     *
     * @param classDefinition
     * @param allQueues
     * @return
     */
    private List<List<Packet>> extractQosClass(ClassDefinition classDefinition, List<List<Packet>> allQueues) {
        List<List<Packet>> result = new LinkedList<List<Packet>>();
        for (int queue : classDefinition.getQueueNumbers()) {
            result.add(allQueues.get(queue));
        }
        return result;
    }

    /**
     * calculates the sum of all packet sizes
     *
     * @param outputQueues
     * @return
     */
    private int calculateAllPacketsSize(List<List<Packet>> outputQueues) {
        int size = 0;
        for (List<Packet> queue : outputQueues) {
            for (Packet p : queue) {
                size += p.getPacketSize();
            }
        }
        return size;
    }

    /**
     * copies output queues to new List so that I can remove packets from it
     *
     * @param outputQueuePackets
     * @return
     */
    private List<List<Packet>> outputQueueMakeCopy(List<List<Packet>> outputQueuePackets) {
        List<List<Packet>> result = new LinkedList<List<Packet>>();
        for (List<Packet> q : outputQueuePackets) {
            List<Packet> list = new LinkedList<Packet>();
            list.addAll(q);
            result.add(list);
        }
        return result;
    }

    /**
     * detects if class is empty
     *
     * @param classNo
     * @return
     */
    private boolean isClassEmpty(int classNo) {
        return unprocessedPacketsInClass[classNo] == 0;
    }

    /**
     * calculates, how many bits can be processed in one round robin
     *
     * @param allPacketsSize
     * @param classCount
     * @return
     */
    private int calculateBitsToProcess(int allPacketsSize, int classCount) {
        return allPacketsSize / classCount;
    }

    /**
     * round robin over one class
     *
     * @return
     */
    private void performClassWFQ(int classNumber, List<List<Packet>> outputQueuePackets, List<Packet> result, int bitsToProcessAll, int classCount) {

        List<Packet> firstPackets = new LinkedList<Packet>();//here are first packet from all queues
        int[] bitsToProcess = new int[outputQueuePackets.size()]; //how many bits can be processed within this run; all unused bits will be stored in "savedBits"
        for (int i = 0; i < bitsToProcess.length; i++) {
            bitsToProcess[i] = savedBits.get(classNumber).get(i) + bitsToProcessAll; //I can process given number of bits (by outer round-robin) + bits that I have saved from previous run
        }

        boolean end = false;

        for (; ; ) {
            if (end) {
                break;
            }
            end = true;
            for (int queueNumber = 0, outputQueuePacketsSize = outputQueuePackets.size(); queueNumber < outputQueuePacketsSize; queueNumber++) {
                List<Packet> queue = outputQueuePackets.get(queueNumber);
                if (queue.isEmpty()) {//no more packets in this queue
                    continue;
                }
                if (queue.get(0).getPacketSize() > bitsToProcess[queueNumber]) {//there is no time for this packet to be processed
                    savedBits.get(classNumber).set(queueNumber, bitsToProcess[queueNumber]);
                    continue;
                }
                end = false;//I have added at least one packet to packet to be scheduled, so maybe there is time for the next WFQ cycle - if not, I simply made one circle more, but never mind that
                firstPackets.add(queue.get(0));
                bitsToProcess[queueNumber] -= queue.get(0).getPacketSize();
            }

            if (firstPackets.isEmpty()) {//there are no more packets in output queues
                break;
            }
            List<Packet> toSchedule = findSmallestPacket(firstPackets);

            removePackets(toSchedule, outputQueuePackets, classNumber);//remove them from output queue so they no longer will be first
            result.addAll(toSchedule);
            firstPackets.clear();
        }


        //calculate number of unprocessed packets
        unprocessedPacketsInClass[classNumber] = 0;
        for (List<Packet> queue : outputQueuePackets) {
            unprocessedPacketsInClass[classNumber] += queue.size();
        }
    }

    /**
     * packets will be removed from output queues (note, that this is just a copy of output queue)
     *
     * @param toSchedule
     * @param outputQueue
     */
    private void removePackets(List<Packet> toSchedule, List<List<Packet>> outputQueue, int classNumber) {
        for (Packet p : toSchedule) {
            removePacketFromClass(p, outputQueue);
        }
    }

    private void removePacketFromClass(Packet packet, List<List<Packet>> outputQueue) {
        for (List<Packet> list : outputQueue) {
            if (list.remove(packet)) {
                return;//packet was removed
            }
        }
        throw new IllegalStateException("unable to remove packet from output queue - it was not found there");
    }

    /**
     * finds packet that has the smallest size
     * however there can be multiple packets with the same, smallest size - this method will return all of these packets
     *
     * @param packets
     * @return
     */
    private List<Packet> findSmallestPacket(List<Packet> packets) {
        List<Packet> smallest = new LinkedList<Packet>();
        for (Packet p : packets) {
            if (smallest.isEmpty()) {
                smallest.add(p);
                continue;
            }

            if (smallest.get(0).getPacketSize() == p.getPacketSize()) {//another packet with this (smallest) size
                smallest.add(p);
            }
            if (smallest.get(0).getPacketSize() > p.getPacketSize()) {//I have found smaller packet
                smallest.clear();
                smallest.add(p);
            }
        }
        return smallest;
    }
}
