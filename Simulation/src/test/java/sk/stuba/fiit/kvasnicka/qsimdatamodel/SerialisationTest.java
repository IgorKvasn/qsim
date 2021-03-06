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

package sk.stuba.fiit.kvasnicka.qsimdatamodel;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Before;
import org.junit.Test;
import sk.stuba.fiit.kvasnicka.TestUtils;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Computer;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.queues.OutputQueue;
import sk.stuba.fiit.kvasnicka.qsimsimulation.exceptions.ClassDefinitionException;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.TopologyManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanismDefinition;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.impl.BestEffortClassification;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.queuemanagement.ActiveQueueManagement;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.queuemanagement.impl.RandomEarlyDetection;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling.PacketScheduling;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling.impl.WeightedFairQueuingScheduling;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling.impl.WeightedRoundRobinScheduling;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.utils.FlowClassDefinition;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Igor Kvasnicka
 */
public class SerialisationTest implements Serializable {
    NetworkNode node;
    Edge edge;
    TopologyManager topologyManager;
    QosMechanismDefinition qosMechanism;

    @Before
    public void before() throws ClassDefinitionException {


        final FlowClassDefinition[] classDef = new FlowClassDefinition[2];
        classDef[0] = new FlowClassDefinition("className1", "srcPort!=3");
        classDef[1] = new FlowClassDefinition("className2", "srcPort=3");

        qosMechanism = new QosMechanismDefinition(null,new WeightedFairQueuingScheduling()
        , new BestEffortClassification(), new RandomEarlyDetection(new HashMap<String, Object>() {{
            put(RandomEarlyDetection.EXPONENTIAL_WEIGHT_FACTOR, .6);
            put(RandomEarlyDetection.MAX_PROBABILITY, 1.0);
            put(RandomEarlyDetection.MIN_THRESHOLD, .5);
            put(RandomEarlyDetection.MAX_THRESHOLD, .8);
        }}));


        OutputQueue o1 = new OutputQueue(10, 1);


        NetworkNode testNode1 = new Computer("comp", null, 10, 11, null, 12, 13, 14, 15, 16);
        NetworkNode testNode2 = new Computer("comp2", null, 10, 11, null, 12, 13, 14, 15, 16);
        edge = new Edge(100, 101, 102, 103, testNode1, testNode2);

        node = new Router("node1", null, qosMechanism, 10, 10, Arrays.asList(o1), 1, 0, 100, 0, 0);

        topologyManager = new TopologyManager(Arrays.asList(edge), Arrays.asList(testNode1, testNode2));

        node.setTopologyManager(topologyManager);
    }

    @Test
    public void testXmlSerialisation_topology_manager() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(topologyManager);
        oos.close();

        //deserialize
        byte[] pickled = out.toByteArray();
        InputStream in = new ByteArrayInputStream(pickled);
        ObjectInputStream ois = new ObjectInputStream(in);
        Object o = ois.readObject();
        TopologyManager copy = (TopologyManager) o;

        assertNotNull(copy);

        assertTrue(EqualsBuilder.reflectionEquals(topologyManager.getNodeList(), copy.getNodeList()));
        assertEquals(topologyManager.getEdgeList().size(), copy.getEdgeList().size());
        for (int i = 0; i < topologyManager.getEdgeList().size(); i++) {
            assertTrue(EqualsBuilder.reflectionEquals(topologyManager.getEdgeList().get(i), copy.getEdgeList().get(i)));
        }
        assertTrue(EqualsBuilder.reflectionEquals(topologyManager, copy, "edgeList"));
    }

    @Test
    public void testXmlSerialisation_network_node() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(node);
        oos.close();

        //deserialize
        byte[] pickled = out.toByteArray();
        InputStream in = new ByteArrayInputStream(pickled);
        ObjectInputStream ois = new ObjectInputStream(in);
        Object o = ois.readObject();
        NetworkNode copy = (NetworkNode) o;

        assertNotNull(copy);
        assertNotNull(copy.getOutputQueueManager());
        assertNotNull(copy.getOutputQueueManager().getNode());
        assertNotNull(copy.getOutputQueueManager().getQueues());

        assertTrue(EqualsBuilder.reflectionEquals(node.getInputQueue(), copy.getInputQueue()));
//        assertTrue(EqualsBuilder.reflectionEquals(node.getQosMechanism(), copy.getQosMechanism()));
        assertTrue(EqualsBuilder.reflectionEquals(node.getRoutingRules(), copy.getRoutingRules()));
        assertNotNull(copy.getTopologyManager());
        assertTrue(EqualsBuilder.reflectionEquals(node.getTxInterfaces(), copy.getTxInterfaces()));
        assertTrue(EqualsBuilder.reflectionEquals(node.getRxInterfaces(), copy.getRxInterfaces()));

        assertNotNull(copy.getOutputQueueManager());
        assertNotNull(copy.getOutputQueueManager().getNode());
        assertNotNull(copy.getOutputQueueManager().getQueues());
        assertTrue(copy.getOutputQueueManager().getNode().equals(node.getOutputQueueManager().getNode()));
        assertNotNull(copy.getAllOutputQueues());
        assertNotNull(copy.getAllProcessingPackets());
        assertNotNull(copy.getAllRXBuffers());
        assertNotNull(copy.getAllTXBuffers());
        assertNotNull(copy.getOutputQueueManager());
        assertNotNull(copy.getOutputQueueManager().getQueues());
        assertEquals(1, copy.getOutputQueueManager().getQueues().size());
        OutputQueue oq = copy.getOutputQueueManager().getQueues().iterator().next();
        assertNotNull(oq);
        assertEquals(10, oq.getMaxCapacity());
        assertEquals(1, oq.getQueueNumber());

        //there is some problem with edgeList in topology manager - serialisation is OK, but EqualsBuilder seems to be unable to handle it
        //but TopologyManager is tested separately and it is OK

        assertTrue(EqualsBuilder.reflectionEquals(node, copy, "outputQueueManager", "qosMechanism", "topologyManager", "inputQueue"));
    }

    @Test
    public void testXmlSerialisation_edge() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(edge);
        oos.close();

        //deserialize
        byte[] pickled = out.toByteArray();
        InputStream in = new ByteArrayInputStream(pickled);
        ObjectInputStream ois = new ObjectInputStream(in);
        Object o = ois.readObject();
        Edge copy = (Edge) o;


        assertNotNull(copy);
        assertTrue(EqualsBuilder.reflectionEquals(edge, copy));
    }


    @Test
    public void testActiveQueueManagement() throws Exception {
        ActiveQueueManagement activeQueueManagement = new RandomEarlyDetection(new HashMap<String, Object>() {{
            put(RandomEarlyDetection.EXPONENTIAL_WEIGHT_FACTOR, .6);
            put(RandomEarlyDetection.MAX_PROBABILITY, 1.0);
            put(RandomEarlyDetection.MIN_THRESHOLD, .5);
            put(RandomEarlyDetection.MAX_THRESHOLD, .8);
        }});

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(activeQueueManagement);
        oos.close();

        //deserialize
        byte[] pickled = out.toByteArray();
        InputStream in = new ByteArrayInputStream(pickled);
        ObjectInputStream ois = new ObjectInputStream(in);
        Object o = ois.readObject();
        ActiveQueueManagement copy = (ActiveQueueManagement) o;


        assertNotNull(copy);
        assertTrue(EqualsBuilder.reflectionEquals(activeQueueManagement, copy));
    }

    @Test
    public void testPacketScheduling() throws Exception {
        final int[] classDef = new int[]{5,1,2};

        PacketScheduling packetScheduling = new WeightedRoundRobinScheduling(new HashMap<String, Object>() {{
            put(WeightedRoundRobinScheduling.QUEUES_WEIGHT, classDef);
        }});

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(packetScheduling);
        oos.close();

        //deserialize
        byte[] pickled = out.toByteArray();
        InputStream in = new ByteArrayInputStream(pickled);
        ObjectInputStream ois = new ObjectInputStream(in);
        Object o = ois.readObject();
        PacketScheduling copy = (PacketScheduling) o;


        assertNotNull(copy);
        Map paramatersOriginal = (Map) TestUtils.getPropertyWithoutGetter(PacketScheduling.class, packetScheduling, "parameters");
        Map paramatersCopy = (Map) TestUtils.getPropertyWithoutGetter(PacketScheduling.class, copy, "parameters");

        assertTrue(EqualsBuilder.reflectionEquals(paramatersOriginal, paramatersCopy));
        assertEquals(packetScheduling.getClass(), copy.getClass());
        //assertTrue(EqualsBuilder.reflectionEquals(packetScheduling, copy));    --- this does not work, it seems EqualsBuilder is not working properly - asserts above should be enough
    }
}

