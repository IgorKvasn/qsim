package sk.stuba.fiit.kvasnicka.qsimdatamodel.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * @author Igor Kvasnicka
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Router extends NetworkNode {

    /**
     * creates new Router instance
     *
     * @param name default name of the router
     */
    public Router(String name) {
        super(name);
    }

    /**
     * for JAXB purposes only
     */
    public Router() {
    }
}
