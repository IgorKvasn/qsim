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
public class WeightedRoundRobinScheduling extends PacketScheduling {

    private static final long serialVersionUID = - 4165594410407914293L;
    private transient int currentClassNumber = 0; //number of currently processing class
    private transient int[] currentQueues;//currently processing queue for each class

    private transient int[] unprocessedPacketsInClass;

    public static final String CLASS_DEFINITIONS = "class_definitions";

    public WeightedRoundRobinScheduling(Map<String, Object> parameters) {
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
        if (outputQueuePackets == null) throw new IllegalArgumentException("outputQueuePackets is NULL");

        if (outputQueuePackets.isEmpty()) {//there are no output queues??? are you serious????
            throw new IllegalStateException("no output queues defined - cannot perform packet scheduling");
        }

        ClassDefinition[] classDefinitions = (ClassDefinition[]) parameters.get(CLASS_DEFINITIONS);


        currentQueues = new int[classDefinitions.length];
        unprocessedPacketsInClass = new int[classDefinitions.length];

        for (int i = 0; i < unprocessedPacketsInClass.length; i++) {
            unprocessedPacketsInClass[i] = Integer.MAX_VALUE;//it is difficult and useless to calculate unprocessed packets - this will guarantee, that at least one round robin will be done
        }

        List<List<Packet>> outputQueuePacketsCopy = outputQueueMakeCopy(outputQueuePackets);

        int classCount = classDefinitions.length;
//        int classSize = getClassSize(outputQueuePacketsCopy.size(), classCount);

        List<Packet> packets = new LinkedList<Packet>();
        int inactiveQueue = 0;
        int startClass = currentClassNumber;
        int packetsToProcess = calculatePacketsToProcess(outputQueuePacketsCopy.size(), classCount);//how many packets can be processed in one round robin run

        while (true) {

            if (isClassEmpty(currentClassNumber)) {       //this queue has no more packets left
                inactiveQueue++;
            } else {
                //-----------perform inner round robin
                List<List<Packet>> qosClass = extractQosClass(classDefinitions[currentClassNumber], outputQueuePacketsCopy);

                performClassRoundRobin(currentClassNumber, qosClass, packets, packetsToProcess);
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
