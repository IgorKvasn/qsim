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

package sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.utils;

import org.junit.Test;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.IpPrecedence;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Igor Kvasnicka
 */
public class ClassificationUtilTest {

    @Test
    public void testIsClassificationRuleApplied_destination_IN_list() throws ClassificationException {


        NetworkNode node1 = new Router("node1", null, null,  10, 10, 50, 10, 10, 100, 0, 0);
        NetworkNode node2 = new Router("node2", null, null,  10, 10, 50, 10, 10, 100, 0, 0);

        SimulationRuleBean rule = new SimulationRuleBean("", node1, node2, null,1, 50, 0, Layer4TypeEnum.UDP, IpPrecedence.IP_PRECEDENCE_0, null, 0, 0);
        rule.setRoute(Arrays.asList(node1, node2));

        Packet packet = new Packet(10, null, rule, 10);

        boolean result = ClassificationUtil.isClassificationRuleApplied("destinationIn(['a','node2','c','d'])", packet);
        assertTrue(result);

        boolean result2 = ClassificationUtil.isClassificationRuleApplied("destinationIn(['a','b','c','d'])", packet);
        assertFalse(result2);
    }

    @Test
    public void testIsClassificationRuleApplied_destination_single() throws ClassificationException {

        NetworkNode node1 = new Router("node1", null, null,  10, 10, 50, 10, 10, 100, 0, 0);
        NetworkNode node2 = new Router("node2", null, null,  10, 10, 50, 10, 10, 100, 0, 0);

        SimulationRuleBean rule = new SimulationRuleBean("", node1, node2, null,1, 50, 0, Layer4TypeEnum.UDP, IpPrecedence.IP_PRECEDENCE_0, null, 0, 0);
        rule.setRoute(Arrays.asList(node1, node2));

        Packet packet = new Packet(10, null, rule, 10);

        boolean result = ClassificationUtil.isClassificationRuleApplied("destination('node2')", packet);
        assertTrue(result);

        boolean result2 = ClassificationUtil.isClassificationRuleApplied("destination('a')", packet);
        assertFalse(result2);
    }

    @Test
    public void testIsClassificationRuleApplied_destination_negation() throws ClassificationException {

        NetworkNode node1 = new Router("node1", null, null,  10, 10, 50, 10, 10, 100, 0, 0);
        NetworkNode node2 = new Router("node2", null, null,  10, 10, 50, 10, 10, 100, 0, 0);

        SimulationRuleBean rule = new SimulationRuleBean("", node1, node2, null,1, 50, 0, Layer4TypeEnum.UDP, IpPrecedence.IP_PRECEDENCE_0, null, 0, 0);
        rule.setRoute(Arrays.asList(node1, node2));

        Packet packet = new Packet(10, null, rule, 10);

        boolean result = ClassificationUtil.isClassificationRuleApplied("notDestinationIn(['a','node2','c','d'])", packet);
        assertFalse(result);

        boolean result2 = ClassificationUtil.isClassificationRuleApplied("notDestinationIn(['a','b','c','d'])", packet);
        assertTrue(result2);

        boolean result3 = ClassificationUtil.isClassificationRuleApplied("notDestination('a')", packet);
        assertTrue(result3);
    }

    @Test
    public void testIsClassificationRuleApplied_source_IN_list() throws ClassificationException {

        NetworkNode node1 = new Router("node1", null, null,  10, 10, 50, 10, 10, 100, 0, 0);
        NetworkNode node2 = new Router("node2", null, null,  10, 10, 50, 10, 10, 100, 0, 0);

        SimulationRuleBean rule = new SimulationRuleBean("", node1, node2, null,1, 50, 0, Layer4TypeEnum.UDP, IpPrecedence.IP_PRECEDENCE_0, null, 0, 0);
        rule.setRoute(Arrays.asList(node1, node2));

        Packet packet = new Packet(10, null, rule, 10);

        boolean result = ClassificationUtil.isClassificationRuleApplied("sourceIn(['a','node1','c','d'])", packet);
        assertTrue(result);

        boolean result2 = ClassificationUtil.isClassificationRuleApplied("sourceIn(['a','b','c','d'])", packet);
        assertFalse(result2);
    }

    @Test
    public void testIsClassificationRuleApplied_source_single() throws ClassificationException {

        NetworkNode node1 = new Router("node1", null, null,  10, 10, 50, 10, 10, 100, 0, 0);
        NetworkNode node2 = new Router("node2", null, null,  10, 10, 50, 10, 10, 100, 0, 0);

        SimulationRuleBean rule = new SimulationRuleBean("", node1, node2, null,1, 50, 0, Layer4TypeEnum.UDP, IpPrecedence.IP_PRECEDENCE_0, null, 0, 0);
        rule.setRoute(Arrays.asList(node1, node2));

        Packet packet = new Packet(10, null, rule, 10);

        boolean result = ClassificationUtil.isClassificationRuleApplied("source('node1')", packet);
        assertTrue(result);

        boolean result2 = ClassificationUtil.isClassificationRuleApplied("source('a')", packet);
        assertFalse(result2);
    }


    @Test
    public void testIsClassificationRuleApplied_source_single_regex() throws ClassificationException {

        NetworkNode node1 = new Router("node1", null, null,  10, 10, 50, 10, 10, 100, 0, 0);
        NetworkNode node2 = new Router("node2", null, null,  10, 10, 50, 10, 10, 100, 0, 0);

        SimulationRuleBean rule = new SimulationRuleBean("", node1, node2, null, 1, 50, 0, Layer4TypeEnum.UDP, IpPrecedence.IP_PRECEDENCE_0, null, 0, 0);
        rule.setRoute(Arrays.asList(node1, node2));

        Packet packet = new Packet(10, null, rule, 10);

        boolean result = ClassificationUtil.isClassificationRuleApplied("source('node.')", packet);
        assertTrue(result);

        boolean result2 = ClassificationUtil.isClassificationRuleApplied("source('n.*')", packet);
        assertTrue(result2);

        boolean result3 = ClassificationUtil.isClassificationRuleApplied("source('x.*')", packet);
        assertFalse(result3);
    }


    @Test
    public void testIsClassificationRuleApplied_source_IN_list_regex() throws ClassificationException {

        NetworkNode node1 = new Router("node1", null, null,  10, 10, 50, 10, 10, 100, 0, 0);
        NetworkNode node2 = new Router("node2", null, null,  10, 10, 50, 10, 10, 100, 0, 0);

        SimulationRuleBean rule = new SimulationRuleBean("", node1, node2, null,1, 50, 0, Layer4TypeEnum.UDP, IpPrecedence.IP_PRECEDENCE_0, null, 0, 0);
        rule.setRoute(Arrays.asList(node1, node2));

        Packet packet = new Packet(10, null, rule, 10);

        boolean result = ClassificationUtil.isClassificationRuleApplied("sourceIn(['a','node.','c','d'])", packet);
        assertTrue(result);

        boolean result3 = ClassificationUtil.isClassificationRuleApplied("sourceIn(['a','no.*','c','d'])", packet);
        assertTrue(result3);

        boolean result2 = ClassificationUtil.isClassificationRuleApplied("sourceIn(['a','b','c','d'])", packet);
        assertFalse(result2);
    }


    @Test
    public void testIsClassificationRuleApplied_source_negation() throws ClassificationException {
        NetworkNode node1 = new Router("node1", null, null,  10, 10, 50, 10, 10, 100, 0, 0);
        NetworkNode node2 = new Router("node2", null, null,  10, 10, 50, 10, 10, 100, 0, 0);

        SimulationRuleBean rule = new SimulationRuleBean("", node1, node2, null, 1, 50, 0, Layer4TypeEnum.UDP, IpPrecedence.IP_PRECEDENCE_0, null, 0, 0);
        rule.setRoute(Arrays.asList(node1, node2));

        Packet packet = new Packet(10, null, rule, 10);

        boolean result = ClassificationUtil.isClassificationRuleApplied("notSourceIn(['a','node1','c','d'])", packet);
        assertFalse(result);

        boolean result2 = ClassificationUtil.isClassificationRuleApplied("notSourceIn ( ['a','b','c','d' ] ) ", packet);
        assertTrue(result2);

        boolean result3 = ClassificationUtil.isClassificationRuleApplied("notSource('a')", packet);
        assertTrue(result3);
    }

    @Test
    public void testIsClassificationRuleApplied_packet_size() throws ClassificationException {

        NetworkNode node1 = new Router("node1", null, null,  10, 10, 50, 10, 10, 100, 0, 0);
        NetworkNode node2 = new Router("node2", null, null,  10, 10, 50, 10, 10, 100, 0, 0);

        SimulationRuleBean rule = new SimulationRuleBean("", node1, node2, null,1, 50, 0, Layer4TypeEnum.UDP, IpPrecedence.IP_PRECEDENCE_0, null, 0, 0);
        rule.setRoute(Arrays.asList(node1, node2));

        Packet packet = new Packet(10, null, rule, 10);

        boolean result = ClassificationUtil.isClassificationRuleApplied("size = 50", packet);
        assertTrue(result);

        boolean result2 = ClassificationUtil.isClassificationRuleApplied("size != 50", packet);
        assertFalse(result2);
    }

    @Test
    public void testIsClassificationRuleApplied_packet_size_compare() throws ClassificationException {

        NetworkNode node1 = new Router("node1", null, null,  10, 10, 50, 10, 10, 100, 0, 0);
        NetworkNode node2 = new Router("node2", null, null,  10, 10, 50, 10, 10, 100, 0, 0);

        SimulationRuleBean rule = new SimulationRuleBean("", node1, node2, null,1, 50, 0, Layer4TypeEnum.UDP, IpPrecedence.IP_PRECEDENCE_0, null, 0, 0);
        rule.setRoute(Arrays.asList(node1, node2));

        Packet packet = new Packet(10, null, rule, 10);

        boolean result = ClassificationUtil.isClassificationRuleApplied("size >= 50", packet);
        assertTrue(result);

        boolean result2 = ClassificationUtil.isClassificationRuleApplied("size < 50", packet);
        assertFalse(result2);
    }

    @Test
    public void testIsClassificationRuleApplied_multiple_conditions() throws ClassificationException {

        NetworkNode node1 = new Router("node1", null, null,  10, 10, 50, 10, 10, 100, 0, 0);
        NetworkNode node2 = new Router("node2", null, null,  10, 10, 50, 10, 10, 100, 0, 0);

        SimulationRuleBean rule = new SimulationRuleBean("", node1, node2,null, 1, 50, 0, Layer4TypeEnum.UDP, IpPrecedence.IP_PRECEDENCE_0, null, 0, 0);
        rule.setRoute(Arrays.asList(node1, node2));

        Packet packet = new Packet(50, null, rule, 10);

        boolean result = ClassificationUtil.isClassificationRuleApplied("size = 50 AND source('node1')", packet);
        assertTrue(result);

        boolean result2 = ClassificationUtil.isClassificationRuleApplied("(size != 50) AND (source('node1'))", packet);
        assertFalse(result2);

        boolean result4 = ClassificationUtil.isClassificationRuleApplied("size = 50 AND source('node1') AND  protocol !='TCP'", packet);
        assertTrue(result4);
    }

    @Test
    public void testIsClassificationRuleApplied_ip_preference() throws ClassificationException {
        NetworkNode node1 = new Router("node1", null, null,  10, 10, 50, 10, 10, 100, 0, 0);
        NetworkNode node2 = new Router("node2", null, null,  10, 10, 50, 10, 10, 100, 0, 0);

        SimulationRuleBean rule = new SimulationRuleBean("", node1, node2, null,1, 50, 0, Layer4TypeEnum.UDP, IpPrecedence.IP_PRECEDENCE_1, null, 0, 0);
        rule.setRoute(Arrays.asList(node1, node2));

        Packet packet = new Packet(10, null, rule, 10);

        boolean result = ClassificationUtil.isClassificationRuleApplied("ipTos = 1", packet);
        assertTrue(result);
    }

    @Test
    public void testValidateClassificationRule_validation_OK() throws ClassificationException {
        ClassificationUtil.validateClassificationRule("ipTos = 1");
    }

    @Test(expected = ClassificationException.class)
    public void testValidateClassificationRule_validation_Wrong_1() throws ClassificationException {
        ClassificationUtil.validateClassificationRule("ipToS = 1");
    }

    @Test(expected = ClassificationException.class)
    public void testValidateClassificationRule_validation_Wrong_2() throws ClassificationException {
        ClassificationUtil.validateClassificationRule("ipTos = anyText");
    }
}
