/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.export.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.openide.util.Exceptions;
import sk.stuba.fiit.kvasnicka.topologyvisual.exceptions.ExportException;
import sk.stuba.fiit.kvasnicka.topologyvisual.export.Exportable;
import sk.stuba.fiit.kvasnicka.topologyvisual.simulation.SimulationRulesExportBean;

/**
 *
 * @author Igor Kvasnicka
 */
public class PdfExport implements Exportable {

    private static final String JASPER_REPORT_LOCATION = "/sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/allRules.jrxml";

    @Override
    public void serialize(List<SimulationRulesExportBean> simRules, InputStream chartImage, OutputStream output) throws ExportException {
        try {
            JasperReport jasperReport = JasperCompileManager.compileReport(getClass().getResourceAsStream(JASPER_REPORT_LOCATION));

            Map<String, Object> parameters = new HashMap<String, Object>();

            parameters.put("datasource", new JRBeanCollectionDataSource(simRules));
            parameters.put("chartImage", chartImage);

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JRBeanCollectionDataSource(simRules));

            JasperExportManager.exportReportToPdfStream(jasperPrint, output);
        } catch (JRException ex) {
            throw new ExportException(ex);
        }
    }
}
