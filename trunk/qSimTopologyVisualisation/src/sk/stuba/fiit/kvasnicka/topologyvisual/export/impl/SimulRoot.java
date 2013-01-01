/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.export.impl;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import sk.stuba.fiit.kvasnicka.topologyvisual.simulation.SimulationRulesExportBean;

/**
 *
 * @author Igor Kvasnicka
 */
@XmlRootElement
public class SimulRoot {

    @XmlElement
    public List<SimulationRulesExportBean> simRule;

    public SimulRoot(List<SimulationRulesExportBean> simRules) {
        this.simRule = simRules;

    }

    public SimulRoot() {
    }
}
