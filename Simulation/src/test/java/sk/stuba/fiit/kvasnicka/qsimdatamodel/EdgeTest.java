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

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.SwQueues;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.buffers.InputInterface;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.buffers.OutputInterface;
import sk.stuba.fiit.kvasnicka.qsimsimulation.SimulationRuleBean;
import sk.stuba.fiit.kvasnicka.qsimsimulation.SimulationTimer;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.PacketTypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.exceptions.NotEnoughBufferSpaceException;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PacketManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.TopologyManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanism;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author Igor Kvasnicka
 */
public class EdgeTest {

    PacketManager packetManager;
    SimulationTimer timer;
    QosMechanism qosMechanism;
    double simulationTime;
    TopologyManager topologyManager;
    NetworkNode node1, node2;
    Edge edge;
    SwQueues swQueues, swQueues2;
    private final int MAX_TX_SIZE = 200;
    private final int MTU = 100;
    private final Layer4TypeEnum layer4 = Layer4TypeEnum.UDP;

    @Before
    public void before() throws NotEnoughBufferSpaceException {
        simulationTime = 10L;

        qosMechanism = EasyMock.createMock(QosMechanism.class);


        SwQueues.QueueDefinition[] q = new SwQueues.QueueDefinition[1];
        q[0] = new SwQueues.QueueDefinition(50, "queue 1");
        swQueues = new SwQueues(q);

        SwQueues.QueueDefinition[] q2 = new SwQueues.QueueDefinition[1];
        q2[0] = new SwQueues.QueueDefinition(50, "queue 1");
        swQueues2 = new SwQueues(q2);

        EasyMock.expect(qosMechanism.classifyAndMarkPacket(EasyMock.anyObject(Packet.class))).andReturn(0).times(100);
        EasyMock.expect(qosMechanism.decitePacketsToMoveFromOutputQueue(EasyMock.anyObject(List.class), EasyMock.anyObject(SwQueues.class))).andAnswer(new IAnswer<List<Packet>>() {
            @Override
            public List<Packet> answer() throws Throwable {
                return (List<Packet>) EasyMock.getCurrentArguments()[0];
            }
        }).times(100);
        EasyMock.replay(qosMechanism);


        node1 = new Router("node1", qosMechanism, swQueues, MAX_TX_SIZE, 10, 2, 100);//max processing packets are set to 2
        node2 = new Router("node2", qosMechanism, swQueues2, MAX_TX_SIZE, 10, 2, 100);


        edge = new Edge(100, node1, node2);
        edge.setMtu(MTU);
        edge.setPacketErrorRate(0.0);
        edge.setLength(2);

        topologyManager = new TopologyManager(Arrays.asList(edge), Arrays.asList(node1, node2));
        node1.setTopologyManager(topologyManager);
        node2.setTopologyManager(topologyManager);

        node1.setTopologyManager(topologyManager);
        node2.setTopologyManager(topologyManager);
//        node1.addRoute("node2", "node2");
//        node2.addRoute("node1", "node1");

        timer = EasyMock.createMock(SimulationTimer.class);
        EasyMock.expect(timer.getTopologyManager()).andReturn(topologyManager).times(100);
        EasyMock.replay(timer);

        packetManager = new PacketManager(timer);
    }

    /**
     * test preparation:
     * adds two packets on the wire
     * test:
     * moving packets from the wire to second network node
     * <p/>
     * all packets should end on the other network node in processing
     * these packets are all 1 fragment big and they are not put into input queue, because processing can handle it
     *
     * @throws NotEnoughBufferSpaceException
     */
    @Test
    public void testMoveFragmentsToNetworkNode() throws NotEnoughBufferSpaceException {
        //prepare - add two packets on the edge
        Packet p1 = new Packet(64, layer4, packetManager, null, 10);
        Packet p2 = new Packet(64, layer4, packetManager, null, 30);

        initRoute(p1, p2);

        node1.addToTxBuffer(p1, 100);
        node1.addToTxBuffer(p2, 100);

        OutputInterface outputInterface = node1.getTxInterfaces().get(node2);
        outputInterface.serialisePackets(50);     //adds two packets to the edge

        //test method
        edge.moveFragmentsToNetworkNode(100);

        //assert
        InputInterface inputInterface = node2.getRxInterfaces().get(node1);
        assertNotNull(inputInterface);
        assertEquals(0, inputInterface.getNumberOfFragments()); //all fragments are put directly into processing
        assertNull(node2.getRxInterfaces().get(node2));
        List<Packet> inputQueue = (List<Packet>) getPropertyWithoutGetter(NetworkNode.class, node2, "inputQueue");
        assertEquals(0, inputQueue.size());//also input queue should be empty

        assertEquals(2, node2.getPacketsInProcessing().size());
    }

    /**
     * just like previous test, but this time, there are some packets in processing, so test packets
     * should be put into input queue - however, there is one space left in processing, so only one packet should be in input queue
     *
     * @throws NotEnoughBufferSpaceException
     */
    @Test
    public void testMoveFragmentsToNetworkNode_multifragment() throws NotEnoughBufferSpaceException {
        //prepare - add two packets on the edge
        Packet p1 = new Packet(64, layer4, packetManager, null, 10);
        Packet p2 = new Packet(64, layer4, packetManager, null, 30);
        Packet p3 = new Packet(64, layer4, packetManager, null, 30);

        initRoute(p1, p2, p3);

        node1.addToTxBuffer(p1, 100);
        node1.addToTxBuffer(p2, 100);

        OutputInterface outputInterface = node1.getTxInterfaces().get(node2);
        outputInterface.serialisePackets(50);     //adds two packets to the edge
        node2.addPacketToProcessing(p3); //some packets are already processing

        //test method
        edge.moveFragmentsToNetworkNode(100);

        //assert
        InputInterface inputInterface = node2.getRxInterfaces().get(node1);
        assertNotNull(inputInterface);
        assertEquals(0, inputInterface.getNumberOfFragments()); //all fragments are put directly into processing
        assertNull(node2.getRxInterfaces().get(node2));
        List<Packet> inputQueue = (List<Packet>) getPropertyWithoutGetter(NetworkNode.class, node2, "inputQueue");
        assertEquals(1, inputQueue.size());//also input queue should be empty

        assertEquals(2, node2.getPacketsInProcessing().size());
    }

    private Object getPropertyWithoutGetter(Class klass, Object bean, String field) {
        Field f = null;
        try {
            f = klass.getDeclaredField(field);
            f.setAccessible(true);
            return f.get(bean);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void initRoute(Packet... packets) {
        SimulationRuleBean simulationRuleBean = new SimulationRuleBean(node1, node2, 1, 1, 10, PacketTypeEnum.AUDIO_PACKET, Layer4TypeEnum.UDP, false);
        simulationRuleBean.addRoute(Arrays.asList(node1, node2));

        for (Packet p : packets) {
            Field f = null;
            try {
                f = Packet.class.getDeclaredField("simulationRule");
                f.setAccessible(true);
                f.set(p, simulationRuleBean);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
