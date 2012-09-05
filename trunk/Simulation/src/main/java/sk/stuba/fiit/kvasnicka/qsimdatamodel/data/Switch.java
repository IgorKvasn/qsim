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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.qsimdatamodel.data;

import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanism;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.utils.dscp.DscpManager;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.Map;

/**
 * @author Igor Kvasnicka
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Switch extends NetworkNode {
    private static final long serialVersionUID = 1L;

    /**
     * creates new Switch instance
     * <p/>
     * see NetworkNode javadoc for more explanation
     *
     * @see NetworkNode#NetworkNode(String, sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanism, int, int, int, int, int, double, double, double, sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.utils.dscp.DscpManager)
     */
    public Switch(String name, QosMechanism qosMechanism, int maxTxBufferSize, int maxRxBufferSize, int maxOutputQueueSize, int maxIntputQueueSize, int maxProcessingPackets, double tcpDelay, double minProcessingDelay, double maxProcessingDelay, DscpManager dscpManager) {
        super(name, qosMechanism, maxTxBufferSize, maxRxBufferSize, maxIntputQueueSize, maxOutputQueueSize, maxProcessingPackets, tcpDelay, minProcessingDelay, maxProcessingDelay, dscpManager);
    }


    @Override
    protected void fillForbiddenRoutingRules(Map<Class, Integer> routingRules) {
        routingRules.put(Router.class, 1);//at most 1 router as neighbour
    }
}