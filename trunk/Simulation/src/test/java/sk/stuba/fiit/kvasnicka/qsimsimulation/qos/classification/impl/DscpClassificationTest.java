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

package sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.impl;

import org.junit.Before;
import org.junit.Test;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.IpPrecedence;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.utils.dscp.DscpDefinition;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.utils.dscp.DscpManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.utils.dscp.DscpValuesEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * @author Igor Kvasnicka
 */
public class DscpClassificationTest {
    DscpClassification classification;
    NetworkNode node1;
    Packet packet;

    @Before
    public void before() {

        final DscpDefinition[] dscpDefinitions = new DscpDefinition[]{
                new DscpDefinition("destination('node2')", DscpValuesEnum.BEST_EFFORT),
                new DscpDefinition("size = 4", DscpValuesEnum.EF)
        };

        final DscpManager dscpManager = new DscpManager(dscpDefinitions, DscpValuesEnum.AF31, 5);
        classification = new DscpClassification(new HashMap<String, Object>() {{
            put(DscpClassification.DSCP_DEFINITIONS, dscpManager);
        }});


        node1 = new Router("node1", null, null, 100, 10, null, 10, 10, 100, 0, 0);
        packet = new Packet(14, null, null, 10);
        initRoute(packet);
    }


    @Test
    public void testClassifyAndMarkPacket_more_queues_then_definitions() throws Exception {
        assertEquals(2, classification.classifyAndMarkPacket(node1, packet));
    }


    @Test
    public void testClassifyAndMarkPacket_less_queues_then_definitions() throws Exception {
        final DscpDefinition[] dscpDefinitions = new DscpDefinition[]{
                new DscpDefinition("destination('node2')", DscpValuesEnum.EF),
                new DscpDefinition("size = 4", DscpValuesEnum.BEST_EFFORT)
        };

        final DscpManager dscpManager = new DscpManager(dscpDefinitions, DscpValuesEnum.AF31, 2);
        classification = new DscpClassification(new HashMap<String, Object>() {{
            put(DscpClassification.DSCP_DEFINITIONS, dscpManager);
        }});

        assertEquals(0, classification.classifyAndMarkPacket(node1, packet));
    }

    @Test
    public void testClassifyAndMarkPacket_no_definition_satisfied() throws Exception {

        NetworkNode node2 = new Router("this is not node2", null, null, 100, 10, null, 10, 10, 100, 0, 0);

        SimulationRuleBean simulationRuleBean = new SimulationRuleBean("", node1, node2, null, 1, 1, 100, Layer4TypeEnum.UDP, IpPrecedence.IP_PRECEDENCE_0, null, 0, 0);
        Packet packet2 = new Packet(1, null, simulationRuleBean, 10);

        assertEquals(1, classification.classifyAndMarkPacket(node1, packet2));
    }

    @Test
    public void testDscpValueName() {
        assertTrue("AF11".equals(DscpValuesEnum.AF11.getTextName()));
        assertTrue("Best effort".equals(DscpValuesEnum.BEST_EFFORT.getTextName()));
    }

    private void initRoute(Packet... packets) {

        NetworkNode node2 = new Router("node2", null, null, 100, 10, null, 10, 10, 100, 0, 0);


        SimulationRuleBean simulationRuleBean = new SimulationRuleBean("", node1, node2, null, 1, 1, 100, Layer4TypeEnum.UDP, IpPrecedence.IP_PRECEDENCE_0, null, 0, 0);
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
