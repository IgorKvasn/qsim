/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.export;

import java.util.Arrays;
import java.util.LinkedList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.IpPrecedence;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanismDefinition;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;
import sk.stuba.fiit.kvasnicka.topologyvisual.exceptions.ExportException;
import sk.stuba.fiit.kvasnicka.topologyvisual.export.impl.JsonExport;
import sk.stuba.fiit.kvasnicka.topologyvisual.export.impl.XmlExport;
import sk.stuba.fiit.kvasnicka.topologyvisual.simulation.SimulationRulesExportBean;
import sk.stuba.fiit.kvasnicka.topologyvisual.simulation.rules.SimulRuleStatisticalData;

/**
 *
 * @author Igor Kvasnicka
 */
public class XmlExportTest {

    private Exportable export;
    private SimulationRuleBean simulationRuleBean;

    @Before
    public void before() {
        export = new XmlExport();
        QosMechanismDefinition qosMechanism = new QosMechanismDefinition(null, null, null, null);
        NetworkNode node1 = new Router("srcNode", null, qosMechanism, 200, 10, 50, 10, 10, 100, 0, 0);
        NetworkNode node2 = new Router("destNode", null, qosMechanism, 200, 10, 50, 10, 10, 100, 0, 0);

        simulationRuleBean = new SimulationRuleBean("this is a sample rule", node1, node2, 1, 1, 100, Layer4TypeEnum.UDP, IpPrecedence.IP_PRECEDENCE_0, null, 0, 0);
        simulationRuleBean.setRoute(Arrays.asList(node1, node2));
    }

    @Test(expected = ExportException.class)
    public void testExport_null() throws ExportException {
        Assert.assertEquals("", export.serialize(null, null));
    }

    @Test
    public void testExport_emptyList() throws ExportException {
        String s = export.serialize(new LinkedList<SimulationRulesExportBean>(), null);
        Assert.assertNotNull(s);
        Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><simulRoot/>", s);
    }

    @Test
    public void testExport() throws ExportException {
        SimulRuleStatisticalData sData = new SimulRuleStatisticalData(simulationRuleBean);
        String s = export.serialize(Arrays.asList(new SimulationRulesExportBean(sData)), null);
        Assert.assertNotNull(s);
        Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><simulRoot><simRule><destPort>0</destPort><destination>destNode</destination><ipPrecedence>0</ipPrecedence><maxDelay>NaN</maxDelay><minDelay>NaN</minDelay><numberOfPackets>1</numberOfPackets><packetSize>1</packetSize><protocol>UDP</protocol><route>srcNode</route><route>destNode</route><simulationRuleName>this is a sample rule</simulationRuleName><source>srcNode</source><srcPort>0</srcPort><statisticalDataCount>0</statisticalDataCount></simRule></simulRoot>", s);
    }
}
