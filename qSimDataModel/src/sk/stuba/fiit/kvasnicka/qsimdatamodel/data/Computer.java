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

package sk.stuba.fiit.kvasnicka.qsimdatamodel.data;

import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.OutputQueueManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanism;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.Map;

/**
 * @author Igor Kvasnicka
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Computer extends NetworkNode {
    private static final long serialVersionUID = 1L;

    /**
     * creates new instanceEdge
     *
     * @param name         name of the computer
     * @param qosMechanism
     * @param queues       sizes of all queues, size is in Bytes
     */
    public Computer(String name, QosMechanism qosMechanism, OutputQueueManager queues, int maxTxBufferSize, int maxRxBufferSize, int maxIntputQueueSize, int maxProcessingPackets, double tcpDelay, double minProcessingDelay, double maxProcessingDelay) {
        super(name, qosMechanism, queues, maxTxBufferSize, maxRxBufferSize, maxIntputQueueSize, maxProcessingPackets, tcpDelay, minProcessingDelay, maxProcessingDelay);
    }

    /**
     * for JAXB purposes only
     */
    public Computer() {
    }

    @Override
    protected void fillForbiddenRoutingRules(Map<Class, Integer> routingRules) {
        routingRules.put(Computer.class, 0);//no computers as neighbours
    }
}
