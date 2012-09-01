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
 * note that implementation requires that queues that should be in the same class must be "near" each other
 * e.g. if 2 queues are in each class, queues no. 1 and 5 will be in different classes
 * if you want them to be in one class, you must "move" queues (e.g. queue 5 swap with queue no.2)
 * but do this BEFORE you run simulation - changing order of queues on runtime may produce some strange behaviour.... however in theory it may work...
 *
 * @author Igor Kvasnicka
 */
public class WeightedRoundRobinScheduling extends PacketScheduling {

    private static final long serialVersionUID = - 4165594410407914293L;
    private transient int currentClassNumber = 0; //number of currently processing class
    private transient int[] currentQueues;//currently processing queue for each class

    private transient int[] unprocessedPacketsInClass;
    /**
     * parameter of this packet scheduling defining
     * <b>number of classes</b>
     * value must be int (Integer)
     */
    public static final String CLASS_COUNT = "class_count";

    public WeightedRoundRobinScheduling(Map<String, Object> parameters) {
        super(parameters);
        if (parameters == null) throw new IllegalArgumentException("no parameters defined - parameter Map is NULL");
        if (! parameters.containsKey(CLASS_COUNT)) {
            throw new IllegalArgumentException("class count was not defined in parameters - use CLASS_COUNT constant");
        }
        if (! (parameters.get(CLASS_COUNT) instanceof Integer)) {
            throw new IllegalArgumentException("class count parameter must has Integer as value - actual value of defined parameter is " + parameters.get(CLASS_COUNT).getClass());
        }
    }

    @Override
    public List<Packet> decitePacketsToMoveFromOutputQueue(NetworkNode networkNode, List<List<Packet>> outputQueuePackets) {
        if (outputQueuePackets == null) throw new IllegalArgumentException("outputQueuePackets is NULL");

        if (outputQueuePackets.isEmpty()) {//there are no output queues??? are you serious????
            throw new IllegalStateException("no output queues defined - cannot perform packet scheduling");
        }

        currentQueues = new int[(Integer) parameters.get(CLASS_COUNT)];
        unprocessedPacketsInClass = new int[(Integer) parameters.get(CLASS_COUNT)];

        for (int i = 0; i < unprocessedPacketsInClass.length; i++) {
            unprocessedPacketsInClass[i] = Integer.MAX_VALUE;//it is difficult and useless to calculate unprocessed packets - this will guarantee, that at least one round robin will be done
        }

        List<List<Packet>> outputQueuePacketsCopy = outputQueueMakeCopy(outputQueuePackets);

        int classCount = (Integer) parameters.get(CLASS_COUNT);
        int classSize = getClassSize(outputQueuePacketsCopy.size(), classCount);

        List<Packet> packets = new LinkedList<Packet>();
        int inactiveQueue = 0;
        int startClass = currentClassNumber;
        int packetsToProcess = calculatePacketsToProcess(outputQueuePacketsCopy.size(), classCount);//how many packets can be processed in one round robin run

        while (true) {

            if (isClassEmpty(currentClassNumber)) {       //this queue has no more packets left
                inactiveQueue++;
            } else {
                //-----------perform inner round robin
                List<List<Packet>> outputQueuePacketsSubList = outputQueuePacketsCopy.subList(currentClassNumber * classSize, getEndOfClass(currentClassNumber, classSize, outputQueuePacketsCopy.size()));

                performClassRoundRobin(currentClassNumber, outputQueuePacketsSubList, packets, packetsToProcess);
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
     * creates copy of output queues, so that packets can be removed from it
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
     * calculates how many packets cen be processed in one round robin (outer round robin)
     *
     * @param queueCount
     * @param classCount
     * @return
     */
    private int calculatePacketsToProcess(int queueCount, int classCount) {
        return queueCount / classCount;
    }

    /**
     * calculates index of last queue in a class
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
     * calculates class size (how many queues are in the class)
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
    private void performClassRoundRobin(int classNumber, List<List<Packet>> outputQueuePackets, List<Packet> result, int packetsToProcess) {

        int numberOfQueues = outputQueuePackets.size();
        int inactiveQueue = 0;
        int startQueueClass = currentQueues[classNumber];
        int processed = 0;

        while (true) {
            if (packetsToProcess == processed) {
                break;//I have used all my time - I need to wait for another round to finish
            }

            List<Packet> queue = outputQueuePackets.get(currentQueues[classNumber]);

            if (queue.size() <= 0) {       //this queue has no more packets left
                inactiveQueue++;
            } else {
                result.add(queue.get(0));
                queue.remove(0);
                processed++;
            }

            currentQueues[classNumber]++;
            currentQueues[classNumber] %= numberOfQueues;
            if (currentQueues[classNumber] == startQueueClass) {//I have performed one round robin circle

                if (inactiveQueue == numberOfQueues) {
                    break;//there are no more packets in output queue
                } else {
                    inactiveQueue = 0;
                }
            }
        }

        unprocessedPacketsInClass[classNumber] = 0;
        for (List<Packet> queue : outputQueuePackets) {
            unprocessedPacketsInClass[classNumber] += queue.size();
        }
    }
}
