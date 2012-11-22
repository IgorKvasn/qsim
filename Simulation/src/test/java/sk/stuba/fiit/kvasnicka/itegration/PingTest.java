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
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;
import sk.stuba.fiit.kvasnicka.qsimsimulation.SimulationTimer;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.IpPrecedence;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.log.SimulationLogEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.log.SimulationLogListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.facade.SimulationFacade;
import sk.stuba.fiit.kvasnicka.qsimsimulation.logs.LogCategory;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanismDefinition;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static sk.stuba.fiit.kvasnicka.TestUtils.getPropertyWithoutGetter;
import static sk.stuba.fiit.kvasnicka.TestUtils.setWithoutSetter;

/**
 * @author Igor Kvasnicka
 */
public class PingTest {
    private int packetsDelivered;
    private QosMechanismDefinition qosMechanism;
    private NetworkNode node1, node2, node3;
    private Edge edge1, edge2;
    private ListenerClass listener;
    private static final Logger logg = Logger.getLogger(PingTest.class);
    int timerTicks = 0;
    int previousTimerTicks = 0;


    @Before
    public void before() {

        listener = new ListenerClass();
        packetsDelivered = 0;
        timerTicks = 0;
        previousTimerTicks = 0;

        qosMechanism = EasyMock.createMock(QosMechanismDefinition.class);
        EasyMock.expect(qosMechanism.classifyAndMarkPacket(EasyMock.anyObject(NetworkNode.class), EasyMock.anyObject(Packet.class))).andReturn(0).times(125);
        EasyMock.expect(qosMechanism.decitePacketsToMoveFromOutputQueue(EasyMock.anyObject(NetworkNode.class), EasyMock.anyObject(Map.class))).andAnswer(new IAnswer<List<Packet>>() {
            @Override
            public List<Packet> answer() throws Throwable {
                if (((Map<Integer, List<Packet>>) EasyMock.getCurrentArguments()[1]).get(0) == null) {
                    return new LinkedList<Packet>();
                }
                return ((Map<Integer, List<Packet>>) EasyMock.getCurrentArguments()[1]).get(0);
            }
        }).times(250);

        EasyMock.replay(qosMechanism);

        node1 = new Router("node1", null, qosMechanism, 1, 1, 1, 1, 1, 0, 0.5, 0.5);
        node2 = new Router("node2", null, qosMechanism, 1, 1, 1, 1, 1, 0, 0.5, 0.5);
        node3 = new Router("node3", null, qosMechanism, 1, 1, 1, 1, 1, 0, 0.5, 0.5);


        edge1 = new Edge(100000000, 100, 2000, 0, node1, node2);
        edge2 = new Edge(100, 100, 3, 0, node2, node3);
    }


    /**
     * simulate one packet
     */
    @Test
    public void testSinglePacketSimulation() throws NoSuchFieldException, IllegalAccessException {

        SimulationRuleBean rule = new SimulationRuleBean("", node1, node2, 2, 50, 0, Layer4TypeEnum.ICMP, IpPrecedence.IP_PRECEDENCE_0, null,  0, 0);
        rule.setRoute(Arrays.asList(node1, node2));

        SimulationFacade simulationFacade = new SimulationFacade();
        simulationFacade.addSimulationRule(rule);
        simulationFacade.initTimer(Arrays.asList(edge1), Arrays.asList(node1, node2));
        SimulationTimer timer = (SimulationTimer) getPropertyWithoutGetter(SimulationFacade.class, simulationFacade, "timer");
        simulationFacade.addSimulationLogListener(listener);

        simulationFacade.addSimulationLogListener(new SimulationLogListener() {
            @Override
            public void simulationLogOccurred(SimulationLogEvent evt) {
                if (evt.getSimulationLog().getCategory() == LogCategory.ERROR) {
                    fail("error during simulation: " + evt.getSimulationLog().getCause());
                }
            }
        });

        simulationFacade.startTimer();


        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);


        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);

        assertTrue(timer.isEndOfSimulation());
        assertEquals(2, packetsDelivered);
    }

    /**
     * this time route from source to destination consists of 3 network nodes
     */
    @Test
    public void testSinglePacket_3Nodes() throws NoSuchFieldException, IllegalAccessException {

        SimulationRuleBean rule = new SimulationRuleBean("", node1, node3, 1, 50, 0, Layer4TypeEnum.ICMP, IpPrecedence.IP_PRECEDENCE_0, null,  0, 0);
        rule.setRoute(Arrays.asList(node1, node2, node3));

        SimulationFacade simulationFacade = new SimulationFacade();
        simulationFacade.addSimulationRule(rule);
        simulationFacade.initTimer(Arrays.asList(edge1, edge2), Arrays.asList(node1, node2, node3));
        SimulationTimer timer = (SimulationTimer) getPropertyWithoutGetter(SimulationFacade.class, simulationFacade, "timer");
        simulationFacade.addSimulationLogListener(listener);

        simulationFacade.addSimulationLogListener(new SimulationLogListener() {
            @Override
            public void simulationLogOccurred(SimulationLogEvent evt) {
                if (evt.getSimulationLog().getCategory() == LogCategory.ERROR) {
                    fail("error during simulation: " + evt.getSimulationLog().getCause());
                }
            }
        });

        simulationFacade.startTimer();

        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);


        assertTrue(timer.isEndOfSimulation());
        assertEquals(1, packetsDelivered);
    }


    /**
     * this ping goes on and on
     *
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    @Test
    public void testSinglePacketSimulation_infinitePing() throws Exception {
        SimulationRuleBean rule = new SimulationRuleBean("", node1, node2, - 1, 50, 0, Layer4TypeEnum.ICMP, IpPrecedence.IP_PRECEDENCE_0, null,  0, 0);    //notice this -1
        rule.setRoute(Arrays.asList(node1, node2));

        SimulationFacade simulationFacade = new SimulationFacade();
        simulationFacade.addSimulationRule(rule);
        simulationFacade.initTimer(Arrays.asList(edge1), Arrays.asList(node1, node2));
        simulationFacade.startTimer();

        simulationFacade.addSimulationLogListener(new SimulationLogListener() {
            @Override
            public void simulationLogOccurred(SimulationLogEvent evt) {
                if (evt.getSimulationLog().getCategory() == LogCategory.ERROR) {
                    fail("error during simulation: " + evt.getSimulationLog().getCause());
                }
                if (evt.getSimulationLog().getCategory() == LogCategory.INFO) {
                    if (evt.getSimulationLog().getCause().contains("dropped")) {
                        fail("packet has been dropped");
                    }
                    if (evt.getSimulationLog().getCause().contains("Ping packet delivered in")) {
                        if (previousTimerTicks == 0) {
                            previousTimerTicks = timerTicks;
                            timerTicks = 0;
                            return;
                        }

                        if (Math.abs(timerTicks - previousTimerTicks) > 1) {
                            fail("number of timer ticks is not constant - previous number of ticks: " + previousTimerTicks + " current number: " + timerTicks);
                            logg.error("rozdiel v timer tickoch: povodny = " + previousTimerTicks + " novy: " + timerTicks);
                        }
                        previousTimerTicks = timerTicks;
                        timerTicks = 0;
                    }
                }
            }
        });

        SimulationTimer timer = (SimulationTimer) getPropertyWithoutGetter(SimulationFacade.class, simulationFacade, "timer");


        //simulate many timer ticks
        for (int i = 0; i < 125; i++) {
            timerTicks++;
            System.out.println("--------------- i = " + i);
            timer.actionPerformed(null);
        }

        assertFalse(timer.isEndOfSimulation());   //timer has not yet finished
    }

    /**
     * this ping goes on and on with edge that drops everything
     *
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    @Test
    public void testSinglePacketSimulation_infinitePing_crc_error() throws Exception {
        SimulationRuleBean rule = new SimulationRuleBean("", node1, node2, - 1, 50, 0, Layer4TypeEnum.ICMP, IpPrecedence.IP_PRECEDENCE_0, null,  0, 0);    //notice this -1
        rule.setRoute(Arrays.asList(node1, node2));

        setWithoutSetter(Edge.class, edge1, "packetErrorRate", 1.0);
        SimulationFacade simulationFacade = new SimulationFacade();
        simulationFacade.addSimulationRule(rule);
        simulationFacade.initTimer(Arrays.asList(edge1), Arrays.asList(node1, node2));
        simulationFacade.startTimer();

//        simulationFacade.addSimulationLogListener(new SimulationLogListener() {
//            @Override
//            public void simulationLogOccurred(SimulationLogEvent evt) {
//                if (evt.getSimulationLog().getCategory() == LogCategory.ERROR) {
//                    fail("error during simulation: " + evt.getSimulationLog().getCause());
//                }
//                if (evt.getSimulationLog().getCategory() == LogCategory.INFO) {
//                    if (evt.getSimulationLog().getCause().contains("dropped")) {
//                        fail("packet has been dropped");
//                    }
//                }
//            }
//        });
        simulationFacade.addSimulationLogListener(new SimulationLogListener() {
            @Override
            public void simulationLogOccurred(SimulationLogEvent evt) {
                if (evt.getSimulationLog().getCause().equals("Starting simulation timer")) return;
                if (evt.getSimulationLog().getCause().equals("Nothing to simulate - simulation stopped")) return;

                if (evt.getSimulationLog().getCause().startsWith("Ping packet delivered in:")) {
                    packetsDelivered++;
                }
            }
        });

        SimulationTimer timer = (SimulationTimer) getPropertyWithoutGetter(SimulationFacade.class, simulationFacade, "timer");

        //simulate many timer ticks
        for (int i = 0; i < 25; i++) {
            timer.actionPerformed(null);
        }


        assertFalse(timer.isEndOfSimulation());    //timer has not yet finished
        assertEquals(0, packetsDelivered);//no packet have been delivered
    }

    private class ListenerClass implements SimulationLogListener {

        @Override
        public void simulationLogOccurred(SimulationLogEvent evt) {
            if (evt.getSimulationLog().getCause().equals("Starting simulation timer")) return;
            if (evt.getSimulationLog().getCause().equals("Nothing to simulate - simulation stopped")) return;

            if (evt.getSimulationLog().getCause().startsWith("Ping packet delivered in:")) {
                packetsDelivered++;
            } else {
                fail("Bad simulation log message: " + evt.getSimulationLog().getCause());
            }
        }
    }
}
