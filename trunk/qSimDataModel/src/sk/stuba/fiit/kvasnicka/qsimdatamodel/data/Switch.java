/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.qsimdatamodel.data;

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
    public Switch(String name, QosMechanism qosMechanism, int markDelay, int queueCount, QueueDefinition[] queues) {
        super(name, qosMechanism, markDelay, queues);
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

    @Override
    public boolean isQosCapable() {
        return true;
    }
}
