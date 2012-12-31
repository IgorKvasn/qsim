/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.export.impl;

import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.log4j.Logger;
import org.openide.util.Exceptions;
import sk.stuba.fiit.kvasnicka.topologyvisual.exceptions.ExportException;
import sk.stuba.fiit.kvasnicka.topologyvisual.export.Exportable;
import sk.stuba.fiit.kvasnicka.topologyvisual.simulation.SimulationRulesExportBean;

/**
 *
 * @author Igor Kvasnicka
 */
public class XmlExport implements Exportable {

    private static final Logger logg = Logger.getLogger(XmlExport.class);
    private static JAXBContext contextObj;

    {
        try {
            contextObj = JAXBContext.newInstance(SimulRoot.class);
        } catch (JAXBException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public String serialize(List<SimulationRulesExportBean> simRules, InputStream chartImage) throws ExportException {
        if (simRules == null) {
            logg.error("simulation rules are NULL - nothing to export");
            throw new ExportException("simulation rules are NULL - nothing to export");
        }

        try {
            Marshaller marshallerObj = contextObj.createMarshaller();
//            marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            SimulRoot root = new SimulRoot(simRules);

            StringWriter w = new StringWriter();
            marshallerObj.marshal(root, w);
            return w.toString();
        } catch (JAXBException ex) {
            throw new ExportException(ex);
        }
    }

    @XmlRootElement
    private static class SimulRoot {

        @XmlElement
        public List<SimulationRulesExportBean> simRule;

        public SimulRoot(List<SimulationRulesExportBean> simRules) {
            this.simRule = simRules;

        }

        public SimulRoot() {
        }
    }
}
