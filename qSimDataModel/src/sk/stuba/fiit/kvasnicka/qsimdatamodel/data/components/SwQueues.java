package sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlTransient;

/**
 * this class represents software queues in network nodes
 *
 * @author Igor Kvasnicka
 */

//todo cela trieda ma byt JAXB serializovatelna - alebo nie?

public class SwQueues {
    @XmlTransient
    @Getter
    private QueueDefinition[] queues;

    public SwQueues(QueueDefinition[] queues) {
        this.queues = queues;
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
     * @return queue size
     */
    public int getQueueUsedCapacity(int queueNumber) {
        if (queueNumber > queues.length) {
            throw new IllegalArgumentException("Invalid queueNumber: " + queueNumber);
        }
        QueueDefinition queueDefinition = queues[queueNumber];
        return queueDefinition.getUsedCapacity();
    }

    /**
     * this class is used to define (output) queue
     */
    @Getter
    @EqualsAndHashCode
    public static class QueueDefinition {

        private int maxCapacity;
        @Setter
        private int usedCapacity;

        public QueueDefinition(int maxCapacity) {
            this.maxCapacity = maxCapacity;
            this.usedCapacity = 0;
        }
    }
}
