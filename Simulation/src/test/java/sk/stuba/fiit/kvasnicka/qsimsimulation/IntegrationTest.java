package sk.stuba.fiit.kvasnicka.qsimsimulation;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.SwQueues;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.PacketTypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.helpers.DelayHelper;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PacketManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.SimulationManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.TopologyManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanism;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Igor Kvasnicka
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(DelayHelper.class)
public class IntegrationTest {
    QosMechanism qosMechanism;
    TopologyManager topologyManager;
    NetworkNode node1, node2;
    Edge edge;
    private SimulationManager simulationManager;

    @Before
    public void before() {


        SwQueues.QueueDefinition[] q = new SwQueues.QueueDefinition[1];
        q[0] = new SwQueues.QueueDefinition(50, "queue 1");
        SwQueues swQueues = new SwQueues(q);

        SwQueues.QueueDefinition[] q2 = new SwQueues.QueueDefinition[1];
        q2[0] = new SwQueues.QueueDefinition(50, "queue 1");
        SwQueues swQueues2 = new SwQueues(q2);


        qosMechanism = EasyMock.createMock(QosMechanism.class);
        EasyMock.expect(qosMechanism.classifyAndMarkPacket(EasyMock.anyObject(Packet.class))).andReturn(0).times(100);
        EasyMock.expect(qosMechanism.decitePacketsToMoveFromOutputQueue(EasyMock.anyObject(List.class), EasyMock.anyObject(SwQueues.class))).andAnswer(new IAnswer<List<Packet>>() {
            @Override
            public List<Packet> answer() throws Throwable {
                return (List<Packet>) EasyMock.getCurrentArguments()[0];
            }
        }).times(100);
        EasyMock.replay(qosMechanism);

        node1 = new Router("node1", qosMechanism, 2, swQueues, 10, 10, 10, 10);
        node2 = new Router("node2", qosMechanism, 2, swQueues2, 10, 10, 10, 10);


        edge = new Edge(100, node1, node2, 100);
        edge.setLength(2);

        topologyManager = new TopologyManager(Arrays.asList(edge), Arrays.asList(node1, node2));
        node1.setTopologyManager(topologyManager);
        node2.setTopologyManager(topologyManager);


        node1.addRoute("node2", "node2");
        node2.addRoute("node1", "node1");
    }

    /**
     * there are no simulation rules, so simulation timer should end after the first call
     */
    @Test
    public void testNosimulationRules() {
        SimulationTimer timer = new SimulationTimer(Arrays.asList(edge), Arrays.asList(node1, node2));

        simulationManager = new SimulationManager();

        timer.startSimulationTimer(simulationManager);     //here timer is started, however JUnit cannot handle Timers, so I have to simulate timer scheduling (see lines below)

        timer.actionPerformed(null);

        assertFalse(timer.isRunning());
    }

    /**
     * simulate one packet
     */
    @Test
    public void testSinglePacketSimulation() throws NoSuchFieldException, IllegalAccessException {

        System.out.println("Processing delay node2: " + DelayHelper.calculateProcessingDelay(node2));
        System.out.println("Serialisation delay: " + DelayHelper.calculateSerialisationDelay(edge, 50));
        System.out.println("Propagation delay: " + DelayHelper.calculatePropagationDelay(edge));
        double sumTime = (DelayHelper.calculateProcessingDelay(node2) + 2 * DelayHelper.calculateSerialisationDelay(edge, 50) + DelayHelper.calculatePropagationDelay(edge));
        System.out.println("Delay sum: " + sumTime);

        SimulationTimer timer = new SimulationTimer(Arrays.asList(edge), Arrays.asList(node1, node2));

        simulationManager = new SimulationManager();
        SimulationRuleBean rule = new SimulationRuleBean(node1, node2, 1, 50, true, 0, 1, PacketTypeEnum.AUDIO_PACKET);
        simulationManager.addSimulationRule(rule);

        timer.startSimulationTimer(simulationManager);     //here timer is started, however JUnit cannot handle Timers, so I have to simulate timer scheduling (see lines below)

        timer.actionPerformed(null);
        timer.actionPerformed(null);


        assertFalse(timer.isRunning());

        checkNoPacketsInTopology(timer);
    }


    /**
     * create multiple packets and see if all of them reach their destination
     */
    @Test
    public void testMultiplePacketsSimulation() throws NoSuchFieldException, IllegalAccessException {

        //        System.out.println("Processing delay node2: " + DelayHelper.calculateProcessingDelay(node2));
        //        System.out.println("Serialisation delay: " + DelayHelper.calculateSerialisationDelay(edge, 50));
        //        System.out.println("Propagation delay: " + DelayHelper.calculatePropagationDelay(edge));
        double sumTime = (DelayHelper.calculateProcessingDelay(node2) + 2 * DelayHelper.calculateSerialisationDelay(edge, 50) + DelayHelper.calculatePropagationDelay(edge));
        //        System.out.println("Delay sum: " + sumTime);

        SimulationTimer timer = new SimulationTimer(Arrays.asList(edge), Arrays.asList(node1, node2));

        simulationManager = new SimulationManager();
        SimulationRuleBean rule = new SimulationRuleBean(node1, node2, 2, 50, true, 0, 1, PacketTypeEnum.AUDIO_PACKET);
        simulationManager.addSimulationRule(rule);

        timer.startSimulationTimer(simulationManager);     //here timer is started, however JUnit cannot handle Timers, so I have to simulate timer scheduling (see lines below)

        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);

        assertFalse(timer.isRunning());
        checkNoPacketsInTopology(timer);
    }


    /**
     * create multiple packets and see if all of them reach their destination
     */
    @Test
    public void testMultiplePacketsSimulation_simultaneous() throws NoSuchFieldException, IllegalAccessException {
        //all packets will be created at once - packet creation delay is set to 0
        PowerMock.mockStatic(DelayHelper.class);
        EasyMock.expect(DelayHelper.calculatePacketCreationDelay(EasyMock.anyObject(NetworkNode.class), EasyMock.anyInt(), EasyMock.anyObject(PacketTypeEnum.class))).andReturn(0.0).times(10);
        EasyMock.expect(DelayHelper.calculatePropagationDelay(EasyMock.anyObject(Edge.class))).andReturn(3.0).times(10);
        EasyMock.expect(DelayHelper.calculateProcessingDelay(EasyMock.anyObject(NetworkNode.class))).andReturn(1.0).times(10);
        EasyMock.expect(DelayHelper.calculateSerialisationDelay(EasyMock.anyObject(Edge.class),EasyMock.anyInt())).andReturn(0.2).times(10);

        PowerMock.replay(DelayHelper.class);


        //        System.out.println("Processing delay node2: " + DelayHelper.calculateProcessingDelay(node2));
        //        System.out.println("Serialisation delay: " + DelayHelper.calculateSerialisationDelay(edge, 50));
        //        System.out.println("Propagation delay: " + DelayHelper.calculatePropagationDelay(edge));
        double sumTime = (DelayHelper.calculateProcessingDelay(node2) + 2 * DelayHelper.calculateSerialisationDelay(edge, 50) + DelayHelper.calculatePropagationDelay(edge));
        //        System.out.println("Delay sum: " + sumTime);

        SimulationTimer timer = new SimulationTimer(Arrays.asList(edge), Arrays.asList(node1, node2));

        simulationManager = new SimulationManager();
        SimulationRuleBean rule = new SimulationRuleBean(node1, node2, 2, 50, true, 0, 1, PacketTypeEnum.AUDIO_PACKET);
        simulationManager.addSimulationRule(rule);

        timer.startSimulationTimer(simulationManager);     //here timer is started, however JUnit cannot handle Timers, so I have to simulate timer scheduling (see lines below)

        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);

        assertFalse(timer.isRunning());
        checkNoPacketsInTopology(timer);
    }

    /**
     * simulate one packet
     */
    @Test
    public void testSimulationRuleActivationDelay() throws NoSuchFieldException, IllegalAccessException {

        //        System.out.println("Processing delay node2: " + DelayHelper.calculateProcessingDelay(node2));
        //        System.out.println("Serialisation delay: " + DelayHelper.calculateSerialisationDelay(edge, 50));
        //        System.out.println("Propagation delay: " + DelayHelper.calculatePropagationDelay(edge));
        double sumTime = (DelayHelper.calculateProcessingDelay(node2) + 2 * DelayHelper.calculateSerialisationDelay(edge, 50) + DelayHelper.calculatePropagationDelay(edge));
        //        System.out.println("Delay sum: " + sumTime);

        SimulationTimer timer = new SimulationTimer(Arrays.asList(edge), Arrays.asList(node1, node2));

        simulationManager = new SimulationManager();
        SimulationRuleBean rule = new SimulationRuleBean(node1, node2, 1, 50, true, 2, 1, PacketTypeEnum.AUDIO_PACKET);
        simulationManager.addSimulationRule(rule);

        timer.startSimulationTimer(simulationManager);     //here timer is started, however JUnit cannot handle Timers, so I have to simulate timer scheduling (see lines below)

        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);


        assertFalse(timer.isRunning());


        checkNoPacketsInTopology(timer);
    }


    /**
     * there are no simulation rules, so simulation timer should end after the first call
     *
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     * @throws NoSuchMethodException
     * @throws NoSuchMethodException
     * @throws java.lang.reflect.InvocationTargetException
     *
     */
    @Test
    public void testStopAndClearTimer() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {


        SimulationTimer timer = new SimulationTimer(Arrays.asList(edge), Arrays.asList(node1, node2));

        simulationManager = new SimulationManager();
        SimulationRuleBean rule = new SimulationRuleBean(node1, node2, 2, 50, true, 0, 3, PacketTypeEnum.AUDIO_PACKET);
        simulationManager.addSimulationRule(rule);

        timer.startSimulationTimer(simulationManager);     //here timer is started, however JUnit cannot handle Timers, so I have to simulate timer scheduling (see lines below)

        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.stopTimer();
        timer.clearSimulationData();

        assertFalse(timer.isRunning());
        checkNoPacketsInTopology(timer);
    }


    private void checkNoPacketsInTopology(SimulationTimer timer) throws NoSuchFieldException, IllegalAccessException {
        Field privateStringField = SimulationTimer.class.getDeclaredField("packetManager");
        privateStringField.setAccessible(true);
        PacketManager packetManager = (PacketManager) privateStringField.get(timer);
        assertTrue(packetManager.checkNoPacketsInSimulation());
    }
}
