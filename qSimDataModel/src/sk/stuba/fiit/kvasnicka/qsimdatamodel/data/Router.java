package sk.stuba.fiit.kvasnicka.qsimdatamodel.data;

import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.SwQueues;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanism;

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
    public Router(String name, QosMechanism qosMechanism, int queueCount, SwQueues queues, int maxTxBufferSize, int maxIntputQueueSize, int maxOutputQueueSize, int maxProcessingPackets) {
        super(name, qosMechanism, queues, maxTxBufferSize, maxIntputQueueSize, maxOutputQueueSize, maxProcessingPackets);
    }


    public Router() {
    }
}
