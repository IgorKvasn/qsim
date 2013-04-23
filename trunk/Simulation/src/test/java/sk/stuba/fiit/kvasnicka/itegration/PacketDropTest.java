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

package sk.stuba.fiit.kvasnicka.itegration;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.queues.OutputQueue;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.utils.PacketCreationDelayFunction;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.utils.creationdelay.ConstantNoiseCreationDelay;
import sk.stuba.fiit.kvasnicka.qsimsimulation.SimulationTimer;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.IpPrecedence;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.drop.PacketDropEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.drop.PacketDropListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.log.SimulationLogEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.log.SimulationLogListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.ruleactivation.SimulationRuleActivationListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.helpers.DelayHelper;
import sk.stuba.fiit.kvasnicka.qsimsimulation.logs.LogCategory;
import sk.stuba.fiit.kvasnicka.qsimsimulation.logs.SimulationLogUtils;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PingManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.SimulationManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanismDefinition;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.impl.BestEffortClassification;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.queuemanagement.impl.BestEffortQueueManagement;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling.impl.FifoScheduling;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

import java.util.Arrays;
import java.util.LinkedList;

import static org.junit.Assert.fail;
import static sk.stuba.fiit.kvasnicka.TestUtils.initNetworkNode;

/**
 * this test will check if RED and TCP behaves correctly using packet drop listener
 *
 * @author Igo
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(DelayHelper.class)
public class PacketDropTest {
    QosMechanismDefinition qosMechanism;
    NetworkNode node1, node2, node3;
    Edge edge1, edge2;
    private SimulationManager simulationManager;
    int packetDelivered;
    PacketCreationDelayFunction creation1;
    Logger logg = Logger.getLogger(PacketDropTest.class);
    OutputQueue oQueue;
    SimulationLogUtils logUtils;


    @Before
    public void before() {

        packetDelivered = 0;
//
//        qosMechanism = new QosMechanismDefinition(new int[]{}, new FifoScheduling(), new BestEffortClassification(), new RandomEarlyDetection(new HashMap<String, Object>() {{
//            put(RandomEarlyDetection.EXPONENTIAL_WEIGHT_FACTOR, 0.9);
//            put(RandomEarlyDetection.MAX_PROBABILITY, 0.2);
//            put(RandomEarlyDetection.MIN_THRESHOLD, 4.0);
//            put(RandomEarlyDetection.MAX_THRESHOLD, 8.0);
//        }}));

        qosMechanism = new QosMechanismDefinition(new int[]{}, new FifoScheduling(), new BestEffortClassification(), new BestEffortQueueManagement());

        QosMechanismDefinition qosMechanismLeafs = new QosMechanismDefinition(new int[]{}, new FifoScheduling(), new BestEffortClassification(), new BestEffortQueueManagement());


        oQueue = new OutputQueue(10, 0);

        node1 = new Router("node1", null, qosMechanismLeafs, 100, 100, Arrays.asList(new OutputQueue(100, 0)), 10, 1, SimulationTimer.TIME_QUANTUM * 3 / 2, 0, 0);
        node2 = new Router("node2", null, qosMechanism, 1, 10, Arrays.asList(oQueue), 10, 10, SimulationTimer.TIME_QUANTUM * 3 / 2, 0, 0);
        node3 = new Router("node3", null, qosMechanismLeafs, 100, 100, Arrays.asList(new OutputQueue(100, 0)), 10, 10, SimulationTimer.TIME_QUANTUM * 3 / 2, 0, 0);

        logUtils = new SimulationLogUtils();
        initNetworkNode(node1, logUtils);
        initNetworkNode(node2, logUtils);
        initNetworkNode(node3, logUtils);


        edge1 = new Edge(10000, 10000, 200, 0, node1, node2);
        edge2 = new Edge(1, 100, 300, 0, node2, node3);

        creation1 = new ConstantNoiseCreationDelay(0.7, 0);
    }

    /**
     * simulate TCP packet drop - without RED
     */
    @Test
    @Ignore
    public void testSinglePacketSimulation() throws NoSuchFieldException, IllegalAccessException {
        SimulationTimer timer = new SimulationTimer(Arrays.asList(edge1, edge2), Arrays.asList(node1, node2, node3), logUtils);
        logUtils.addSimulationLogListener(new SimulationLogListener() {
            @Override
            public void simulationLogOccurred(SimulationLogEvent evt) {
                if (evt.getSimulationLog().getCategory() == LogCategory.ERROR) {
                    fail("error during simulation: " + evt.getSimulationLog().getCause());
                }
            }
        });
        logUtils.addPacketDropListener(new PacketDropListener() {
            @Override
            public void packetDropOccurred(PacketDropEvent evt) {
                System.out.print(evt.getWhen()+", ");
            }
        });
        simulationManager = new SimulationManager();

        SimulationRuleBean rule = new SimulationRuleBean("r1", node1, node3, creation1, - 1, 100, 0, Layer4TypeEnum.TCP, IpPrecedence.IP_PRECEDENCE_0, null, 0, 0);
        rule.setRoute(Arrays.asList(node1, node2, node3));
        SimulationRuleBean rule2 = new SimulationRuleBean("r2", node1, node3, creation1, - 1, 100, 0, Layer4TypeEnum.UDP, IpPrecedence.IP_PRECEDENCE_0, null, 0, 0);
        rule2.setRoute(Arrays.asList(node1, node2, node3));

        simulationManager.addSimulationRule(rule);
        simulationManager.addSimulationRule(rule2);

        timer.startSimulationTimer(simulationManager, new PingManager(), new LinkedList<SimulationRuleActivationListener>());     //here timer is started, however JUnit cannot handle Timers, so I have to simulate timer scheduling (see lines below)

        for (int i = 0; i < 500; i++) {
            timer.actionPerformed(null);
//            System.out.println("usage: " + node2.getAllOutputQueueUsage());
            if (oQueue.getPackets().size() != 0) {
//                  System.out.println("queue capacity: " + oQueue.getPackets().size());
            }
        }
    }
}
