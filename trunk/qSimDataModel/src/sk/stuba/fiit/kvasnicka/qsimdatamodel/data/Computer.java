package sk.stuba.fiit.kvasnicka.qsimdatamodel.data;

import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanism;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.Map;

/**
 * @author Igor Kvasnicka
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Computer extends NetworkNode {

    /**
     * creates new instanceEdge
     *
     * @param name         name of the computer
     * @param qosMechanism
     * @param markDelay
     * @param queues    sizes of all queues, size is in Bytes
     */
    public Computer(String name, QosMechanism qosMechanism, int markDelay, QueueDefinition[] queues) {
        super(name, qosMechanism, markDelay, queues); //computer has got only one queue
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

    @Override
    public boolean isQosCapable() {
        return false;
    }
}
