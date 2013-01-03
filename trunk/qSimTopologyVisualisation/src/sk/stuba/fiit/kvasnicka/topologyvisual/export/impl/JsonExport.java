/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.export.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import org.apache.log4j.Logger;
import org.openide.util.Exceptions;
import sk.stuba.fiit.kvasnicka.topologyvisual.exceptions.ExportException;
import sk.stuba.fiit.kvasnicka.topologyvisual.export.Exportable;
import sk.stuba.fiit.kvasnicka.topologyvisual.simulation.SimulationRulesExportBean;

/**
 *
 * @author Igor Kvasnicka
 */
public class JsonExport implements Exportable {

    private static final Logger logg = Logger.getLogger(JsonExport.class);

    @Override
    public void serialize(List<SimulationRulesExportBean> simRules, InputStream chartImage, OutputStream output) throws ExportException {
        if (simRules == null) {
            logg.error("simulation rules are NULL - nothing to export");
            throw new ExportException("simulation rules are NULL - nothing to export");
        }
        ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
        try {
            String content = mapper.writeValueAsString(simRules);

            output.write(content.getBytes());
        } catch (IOException ex) {
            throw new ExportException(ex);
        } 
    }
}
