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
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.OutputQueueManager;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.buffers.RxBuffer;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.buffers.TxBuffer;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.queues.InputQueue;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.queues.OutputQueue;
import sk.stuba.fiit.kvasnicka.qsimsimulation.SimulationTimer;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.IpPrecedence;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.PacketTypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.exceptions.NotEnoughBufferSpaceException;
import sk.stuba.fiit.kvasnicka.qsimsimulation.logs.SimulationLogUtils;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PacketManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.TopologyManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanism;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static sk.stuba.fiit.kvasnicka.TestUtils.getPropertyWithoutGetter;
import static sk.stuba.fiit.kvasnicka.TestUtils.initNetworkNode;

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
    private final int MAX_TX_SIZE = 200;
    private final int MTU = 100;
    private final Layer4TypeEnum layer4 = Layer4TypeEnum.UDP;

    @Before
    public void before() throws NotEnoughBufferSpaceException {
        simulationTime = 10L;

        qosMechanism = EasyMock.createMock(QosMechanism.class);


        OutputQueue q1 = new OutputQueue(50, "queue 1");
        OutputQueue q2 = new OutputQueue(50, "queue 2");
        OutputQueueManager outputQueueManager1 = new OutputQueueManager(new OutputQueue[]{q1});
        OutputQueueManager outputQueueManager2 = new OutputQueueManager(new OutputQueue[]{q2});

        EasyMock.expect(qosMechanism.classifyAndMarkPacket(EasyMock.anyObject(NetworkNode.class), EasyMock.anyObject(Packet.class))).andReturn(0).times(100);
        EasyMock.expect(qosMechanism.decitePacketsToMoveFromOutputQueue(EasyMock.anyObject(NetworkNode.class), EasyMock.anyObject(List.class))).andAnswer(new IAnswer<List<Packet>>() {
            @Override
            public List<Packet> answer() throws Throwable {
                return ((List<List<Packet>>) EasyMock.getCurrentArguments()[1]).get(0);
            }
        }).times(100);
        EasyMock.replay(qosMechanism);


        node1 = new Router("node1", qosMechanism, outputQueueManager1, MAX_TX_SIZE, 10, 10, 2, 100, 0, 0, null);//max processing packets are set to 2
        node2 = new Router("node2", qosMechanism, outputQueueManager2, MAX_TX_SIZE, 10, 10, 2, 100, 0, 0, null);
        SimulationLogUtils simulationLogUtils = new SimulationLogUtils();
        initNetworkNode(node1, simulationLogUtils);
        initNetworkNode(node2, simulationLogUtils);

        edge = new Edge(100, MTU, 2, 0, node1, node2);

        topologyManager = new TopologyManager(Arrays.asList(edge), Arrays.asList(node1, node2));
        node1.setTopologyManager(topologyManager);
        node2.setTopologyManager(topologyManager);

        node1.setTopologyManager(topologyManager);
        node2.setTopologyManager(topologyManager);

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
        Packet p1 = new Packet(64, packetManager, null, 10);
        Packet p2 = new Packet(64, packetManager, null, 30);

        initRoute(p1, p2);

        node1.addToTxBuffer(p1, 100);
        node1.addToTxBuffer(p2, 100);

        TxBuffer outputInterface = node1.getTxInterfaces().get(node2);
        outputInterface.serialisePackets(50);     //adds two packets to the edge

        //test method
        edge.moveFragmentsToNetworkNode(100);

        //assert
        RxBuffer inputInterface = node2.getRxInterfaces().get(node1);
        assertNotNull(inputInterface);
        assertEquals(0, inputInterface.getNumberOfFragments()); //all fragments are put directly into processing
        assertNull(node2.getRxInterfaces().get(node2));
        InputQueue inputQueue = (InputQueue) getPropertyWithoutGetter(NetworkNode.class, node2, "inputQueue");
        assertTrue(inputQueue.isEmpty());//also input queue should be empty

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
        Packet p1 = new Packet(64, packetManager, null, 10);
        Packet p2 = new Packet(64, packetManager, null, 30);
        Packet p3 = new Packet(64, packetManager, null, 30);

        initRoute(p1, p2, p3);

        node1.addToTxBuffer(p1, 100);
        node1.addToTxBuffer(p2, 100);

        TxBuffer outputInterface = node1.getTxInterfaces().get(node2);
        outputInterface.serialisePackets(50);     //adds two packets to the edge
        node2.addPacketToProcessing(p3); //some packets are already processing

        //test method
        edge.moveFragmentsToNetworkNode(100);

        //assert
        RxBuffer inputInterface = node2.getRxInterfaces().get(node1);
        assertNotNull(inputInterface);
        assertEquals(0, inputInterface.getNumberOfFragments()); //all fragments are put directly into processing
        assertNull(node2.getRxInterfaces().get(node2));
        InputQueue inputQueue = (InputQueue) getPropertyWithoutGetter(NetworkNode.class, node2, "inputQueue");
        assertEquals(1, inputQueue.getUsage());

        assertEquals(2, node2.getPacketsInProcessing().size());
    }


    private void initRoute(Packet... packets) {
        SimulationRuleBean simulationRuleBean = new SimulationRuleBean("", node1, node2, 1, 1, 10, PacketTypeEnum.AUDIO_PACKET, layer4, IpPrecedence.IP_PRECEDENCE_0);
        simulationRuleBean.setRoute(Arrays.asList(node1, node2));

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
