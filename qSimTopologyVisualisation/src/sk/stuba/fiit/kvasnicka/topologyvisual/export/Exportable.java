/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.export;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import sk.stuba.fiit.kvasnicka.topologyvisual.exceptions.ExportException;
import sk.stuba.fiit.kvasnicka.topologyvisual.simulation.SimulationRulesExportBean;

/**
 *
 * @author Igor Kvasnicka
 */
public interface Exportable {

    public void serialize(List<SimulationRulesExportBean> simRules, InputStream chartImage, OutputStream output) throws ExportException;
}
