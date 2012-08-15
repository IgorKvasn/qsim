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

package sk.stuba.fiit.kvasnicka.qsimsimulation.helpers;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Test;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.OutputQueueManager;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.queues.OutputQueue;
import sk.stuba.fiit.kvasnicka.qsimsimulation.SimulationTimer;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PacketManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.TopologyManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Fragment;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanism;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Igor Kvasnicka
 */
public class QueueingHelperTest {

    /**
     * tests method that calculates, how many fragments is needed for a packet
     */
    @Test
    public void testCalculateFragmentSize() {

        int frSize = QueueingHelper.calculateFragmentSize(1, 2, 10, 5);
        assertEquals(5, frSize);

        int frSize2 = QueueingHelper.calculateFragmentSize(1, 2, 10, 11);
        assertEquals(10, frSize2);

        int frSize3 = QueueingHelper.calculateFragmentSize(2, 2, 10, 11);
        assertEquals(1, frSize3);

        int frSize4 = QueueingHelper.calculateFragmentSize(2, 2, 5, 9);
        assertEquals(4, frSize4);

        int frSize5 = QueueingHelper.calculateFragmentSize(1, 3, 5, 10);
        assertEquals(5, frSize5);


        try {
            QueueingHelper.calculateFragmentSize(2, 2, 10, 30);
            fail("this should throw exception, because I need more fragments for this packet - this is a problem in QueueingHelper.calculateNumberOfFragments()");
        } catch (IllegalStateException e) {
            //OK
        }

        try {
            QueueingHelper.calculateFragmentSize(3, 2, 10, 11);
            fail("this should throw exception, because fragment index is bigger than max fragment count");
        } catch (IllegalArgumentException e) {
            //OK
        }
    }

    @Test
    public void testcalculateNumberOfFragments() {
        int frCount1 = QueueingHelper.calculateNumberOfFragments(10, 6);
        assertEquals(2, frCount1);

        int frCount2 = QueueingHelper.calculateNumberOfFragments(10, 5);
        assertEquals(2, frCount2);

        int frCount3 = QueueingHelper.calculateNumberOfFragments(10, 11);
        assertEquals(1, frCount3);

        try {
            QueueingHelper.calculateNumberOfFragments(10, 0);
            fail("this should throw exception, because MTU must not be 0");
        } catch (IllegalArgumentException e) {
            //OK
        }

        try {
            QueueingHelper.calculateNumberOfFragments(10, - 1);
            fail("this should throw exception, because MTU must not be negative");
        } catch (IllegalArgumentException e) {
            //OK
        }
    }

    @Test
    public void testCreateFragments() {
        //prepare - I need network nodes, so that means I have to initialise the whole topology,...


        QosMechanism qosMechanism = EasyMock.createMock(QosMechanism.class);


        OutputQueue q1 = new OutputQueue(50, "queue 1");
        OutputQueue q2 = new OutputQueue(50, "queue 2");
        OutputQueueManager outputQueueManager1 = new OutputQueueManager(new OutputQueue[]{q1});
        OutputQueueManager outputQueueManager2 = new OutputQueueManager(new OutputQueue[]{q2});

        EasyMock.expect(qosMechanism.classifyAndMarkPacket(EasyMock.anyObject(Packet.class))).andReturn(0).times(100);
        EasyMock.expect(qosMechanism.decitePacketsToMoveFromOutputQueue(EasyMock.anyObject(List.class), EasyMock.anyObject(OutputQueueManager.class))).andAnswer(new IAnswer<List<Packet>>() {
            @Override
            public List<Packet> answer() throws Throwable {
                return (List<Packet>) EasyMock.getCurrentArguments()[0];
            }
        }).times(100);
        EasyMock.replay(qosMechanism);


        NetworkNode node1 = new Router("node1", qosMechanism, outputQueueManager1, 10, 10, 10, 10, 100, 0, 0);
        NetworkNode node2 = new Router("node2", qosMechanism, outputQueueManager2, 10, 10, 10, 10, 100, 0, 0);


        Edge edge = new Edge(100, node1, node2);
        edge.setMtu(10);
        edge.setPacketErrorRate(0.0);
        edge.setLength(2);

        TopologyManager topologyManager = new TopologyManager(Arrays.asList(edge), Arrays.asList(node1, node2));
        node1.setTopologyManager(topologyManager);
        node2.setTopologyManager(topologyManager);

//        node1.setRoute("node2", "node2");
//        node2.setRoute("node1", "node1");

        SimulationTimer timer = EasyMock.createMock(SimulationTimer.class);
        EasyMock.expect(timer.getTopologyManager()).andReturn(topologyManager).times(100);
        EasyMock.replay(timer);

        PacketManager packetManager = new PacketManager(timer);

        Packet p1 = new Packet(10, Layer4TypeEnum.UDP, packetManager, null, 10);

        //test method... finally ... and test it on multiple test cases

        Fragment[] fragments = QueueingHelper.createFragments(p1, 10, node1, node2);
        assertEquals(1, fragments.length);

        fragments = QueueingHelper.createFragments(p1, 5, node1, node2);
        assertEquals(2, fragments.length);

        try {
            QueueingHelper.createFragments(p1, 0, node1, node2);
            fail("MTU must not be zero or negative");
        } catch (IllegalArgumentException e) {
            //OK
        }
    }
}
