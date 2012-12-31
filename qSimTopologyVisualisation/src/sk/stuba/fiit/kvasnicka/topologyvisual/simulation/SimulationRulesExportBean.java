/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.simulation;

import java.awt.geom.Point2D;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;
import sk.stuba.fiit.kvasnicka.topologyvisual.simulation.rules.SimulRuleStatisticalData;
import sk.stuba.fiit.kvasnicka.topologyvisual.utils.VerticesUtil;

/**
 * This Bean is carries information about one simulation rule that will be used
 * when exporting
 *
 * @author Igor Kvasnicka
 */
public class SimulationRulesExportBean {

    private transient SimulRuleStatisticalData sData;
    private transient SimulationRuleBean ruleBean;

    public SimulationRulesExportBean() {
    }

    public SimulationRulesExportBean(SimulRuleStatisticalData sData) {
        this.sData = sData;
        this.ruleBean = sData.getRule();
    }

    @XmlElement
    public List<Point2D.Double> getChartData() {
        return sData.getDelayList();
    }

    @XmlElement
    public String getSource() {
        return ruleBean.getSource().getName();
    }

    @XmlElement
    public String getDestination() {
        return ruleBean.getDestination().getName();
    }

    @XmlElement
    public int getNumberOfPackets() {
        return ruleBean.getNumberOfPackets();
    }

    @XmlElement
    public int getPacketSize() {
        return ruleBean.getPacketSize();
    }

    @XmlElement
    public Layer4TypeEnum getProtocol() {
        return ruleBean.getLayer4Type();
    }

    @XmlElement
    public List<String> getRoute() {
        return VerticesUtil.getNodeNames(ruleBean.getRoute());
    }

    @XmlElement
    public String getSimulationRuleName() {
        return ruleBean.getName();
    }

    @XmlElement
    public Integer getIpPrecedence() {
        if (ruleBean.getIpPrecedence() == null) {
            return null;
        }
        return ruleBean.getIpPrecedence().getIntRepresentation();
    }

    @XmlElement
    public String getDscpValue() {
        if (ruleBean.getDscpValue() == null) {
            return null;
        }
        return ruleBean.getDscpValue().getTextName();
    }

    @XmlElement
    public int getSrcPort() {
        return ruleBean.getSrcPort();
    }

    @XmlElement
    public int getDestPort() {
        return ruleBean.getDestPort();
    }

    @XmlElement
    public int getStatisticalDataCount() {
        return sData.getStatisticalDataCount();
    }

    @XmlElement
    public double getMinDelay() {
        return sData.getMinDelay();
    }

    @XmlElement
    public double getMaxDelay() {
        return sData.getMaxDelay();
    }
}
