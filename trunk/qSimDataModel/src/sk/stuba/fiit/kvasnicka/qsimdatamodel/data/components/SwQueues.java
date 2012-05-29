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

package sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;

import javax.xml.bind.annotation.XmlTransient;
import java.util.List;

/**
 * this class represents software queues in network nodes
 *
 * @author Igor Kvasnicka
 */

//todo cela trieda ma byt JAXB serializovatelna - alebo nie?

public class SwQueues {
    @XmlTransient
    private QueueDefinition[] queues;

    public SwQueues(QueueDefinition[] queues) {
        this.queues = new QueueDefinition[queues.length];
        System.arraycopy(queues, 0, this.queues, 0, queues.length); //http://pmd.sourceforge.net/rules/sunsecure.html
    }

    public int getQueueCount() {
        return queues.length;
    }


    /**
     * returns max size of appropriate output queue
     *
     * @param queueNumber queue number
     * @return queue size
     */
    public int getQueueMaxCapacity(int queueNumber) {
        if (queueNumber > queues.length) {
            throw new IllegalArgumentException("Invalid queueNumber: " + queueNumber);
        }
        QueueDefinition queueDefinition = queues[queueNumber];
        return queueDefinition.getMaxCapacity();
    }

    /**
     * returns used capacity of appropriate output queue
     *
     * @param queueNumber queue number
     * @param outputQueue all packets in output queue - regardless of QoS queue number
     * @return queue size
     */
    public int getQueueUsedCapacity(int queueNumber, List<Packet> outputQueue) {
        if (queueNumber >= queues.length) {
            throw new IllegalArgumentException("Invalid queueNumber - max number of QoS queue: " + (queues.length - 1) + ", but method argument was queue number " + queueNumber);
        }
        if (queueNumber == - 1) {
            throw new IllegalStateException("This packet has not been marked and classified - QoS queue number is -1 (default value)");
        }
        int size = 0;
        for (Packet p : outputQueue) {
            if (p.getQosQueue() == - 1) throw new IllegalStateException("packet is not marked");
            if (p.getQosQueue() == queueNumber) size++;
        }
        return size;
    }

    /**
     * this class is used to define (output) queue
     */
    @Getter
    @EqualsAndHashCode
    public static class QueueDefinition {

        private int maxCapacity;
        private String queueLabel;

        /**
         * creates new QoS queue
         *
         * @param maxCapacity maximum capacity of this queue; all packets above this capacity will be dropped
         * @param queueLabel  user defined label for this queue - e.g.: "high priority"
         */
        public QueueDefinition(int maxCapacity, String queueLabel) {
            this.maxCapacity = maxCapacity;
            this.queueLabel = queueLabel;
        }
    }
}
