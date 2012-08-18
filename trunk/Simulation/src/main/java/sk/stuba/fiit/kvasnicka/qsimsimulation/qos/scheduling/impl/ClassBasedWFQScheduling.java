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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Igor Kvasnicka
 */
public class ClassBasedWFQScheduling extends PacketScheduling {

    private int currentClassNumber = 0; //number of currently processing class

    private int[] unprocessedPacketsInClass;
    private int[][] savedBits; //first parameter is class number, second is queue number within the class
    /**
     * parameter of this packet scheduling defining
     * <b>number of classes</b>
     * value must be int (Integer)
     */
    public static final String CLASS_COUNT = "class_count";

    public ClassBasedWFQScheduling(Map<String, Object> parameters) {
        super(parameters);
        if (parameters == null) throw new IllegalArgumentException("no parameters defined - parameter Map is NULL");
        if (! parameters.containsKey(CLASS_COUNT)) {
            throw new IllegalArgumentException("class count was not defined in parameters - use CLASS_COUNT constant");
        }
        if (! (parameters.get(CLASS_COUNT) instanceof Integer)) {
            throw new IllegalArgumentException("class count parameter must has Integer as value - actual value of defined parameter is " + parameters.get(CLASS_COUNT).getClass());
        }

        unprocessedPacketsInClass = new int[(Integer) parameters.get(CLASS_COUNT)];
    }

    @Override
    public List<Packet> decitePacketsToMoveFromOutputQueue(NetworkNode networkNode, List<List<Packet>> outputQueuePackets) {
        if (outputQueuePackets == null) throw new IllegalArgumentException("outputQueuePackets is NULL");

        if (outputQueuePackets.isEmpty()) {//there are no output queues??? are you serious????
            throw new IllegalStateException("no output queues defined - cannot perform packet scheduling");
        }
        int classCount = (Integer) parameters.get(CLASS_COUNT);
        int classSize = getClassSize(outputQueuePackets.size(), classCount);

        savedBits = new int[(Integer) parameters.get(CLASS_COUNT)][classSize];

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
                List<List<Packet>> outputQueuePacketsSubList = outputQueuePacketsCopy.subList(currentClassNumber * classSize, getEndOfClass(currentClassNumber, classSize, outputQueuePacketsCopy.size()));

                performClassWFQ(currentClassNumber, outputQueuePacketsSubList, packets, packetsToProcess, classCount);
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
     * calculates index number of last queue in class
     *
     * @param currentClassNumber
     * @param classSize
     * @param numberOfQueues
     * @return
     */
    private int getEndOfClass(int currentClassNumber, int classSize, int numberOfQueues) {
        int result = (currentClassNumber + 1) * classSize;
        if (result > numberOfQueues) {
            return numberOfQueues;
        }
        return result;
    }

    /**
     * calculates class size
     *
     * @param queueCount
     * @param classCount
     * @return
     */
    private int getClassSize(int queueCount, int classCount) {
        if (queueCount % classCount != 0) {
            return (queueCount / classCount) + 1;
        }
        return (queueCount / classCount);
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
            bitsToProcess[i] = savedBits[classNumber][i] + bitsToProcessAll; //I can process given number of bits (by outer round-robin) + bits that I have saved from previous run
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
                    savedBits[classNumber][queueNumber] = bitsToProcess[queueNumber];
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

            removePackets(toSchedule, outputQueuePackets, classCount);//remove them from output queue so they no longer will be first
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
    private void removePackets(List<Packet> toSchedule, List<List<Packet>> outputQueue, int classCount) {
        for (Packet p : toSchedule) {
            if (p.getQosQueue() == - 1) {
                throw new IllegalStateException("packet has not been marked - how is this possible????");
            }
            if (! outputQueue.get(p.getQosQueue() % classCount).remove(p)) {
                throw new IllegalStateException("unable to remove packet from output queue - it was not found there");
            }
        }
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