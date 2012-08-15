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

package sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.queues;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.OutputQueueManager;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.UsageStatistics;

/**
 * @author Igor Kvasnicka
 */
@EqualsAndHashCode
@Getter

public class OutputQueue implements UsageStatistics {

    private int maxCapacity;
    private String queueLabel;
    private int qosNumber = - 1;
    private OutputQueueManager queueManager = null;

    /**
     * creates new QoS queue
     *
     * @param maxCapacity maximum capacity of this queue; all packets above this capacity will be dropped
     * @param queueLabel  user defined label for this queue - e.g.: "high priority"
     */
    public OutputQueue(int maxCapacity, String queueLabel) {
        this.maxCapacity = maxCapacity;
        this.queueLabel = queueLabel;
    }


    public void setQosNumber(int qosNumber) {
        if (this.qosNumber == - 1) {
            this.qosNumber = qosNumber;
        } else {
            throw new IllegalStateException("QoS number is already set to: " + this.qosNumber + " new QoS number should be " + qosNumber);
        }
    }

    public void setQueueManager(OutputQueueManager queueManager) {
        if (this.queueManager == null) {
            this.queueManager = queueManager;
        } else {
            throw new IllegalStateException("OutputQueueManager is already set");
        }
    }

    @Override
    public int getUsage() {
        return queueManager.getQueueUsedCapacity(qosNumber, queueManager.getOutputQueue());
    }
}
