package sk.stuba.fiit.kvasnicka.topologyvisual.data;

import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * @author Igor Kvasnicka
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Computer extends NetworkNode {

    /**
     * creates new instanceEdge
     *
     * @param name name of the computer
     */
    public Computer(String name) {
        super(name);
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
