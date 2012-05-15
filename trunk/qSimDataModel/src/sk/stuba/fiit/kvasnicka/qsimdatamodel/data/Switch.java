/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.qsimdatamodel.data;

import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.SwQueues;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanism;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.Map;

/**
 * @author Igor Kvasnicka
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Switch extends NetworkNode {

    /**
     * creates new Switch instance
     *
     * @param name default name of the switch
     */
    public Switch(String name, QosMechanism qosMechanism, int queueCount, SwQueues queues, int maxTxBufferSize, int maxIntputQueueSize, int maxOutputQueueSize, int maxProcessingPackets) {
        super(name, qosMechanism, queues, maxTxBufferSize, maxIntputQueueSize, maxOutputQueueSize, maxProcessingPackets);
    }

    /**
     * for JAXB purposes only
     */
    public Switch() {
    }

    @Override
    protected void fillForbiddenRoutingRules(Map<Class, Integer> routingRules) {
        routingRules.put(Router.class, 1);//at most 1 router as neighbour
    }
}
