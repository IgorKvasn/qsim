/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.filetype;

import edu.uci.ics.jung.io.GraphIOException;
import edu.uci.ics.jung.io.GraphMLWriter;
import java.io.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import lombok.Getter;
import org.apache.commons.collections15.Transformer;
import org.apache.log4j.Logger;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import sk.stuba.fiit.kvasnicka.topologyvisual.Topology;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.edges.TopologyEdge;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.palette.gui.TopolElementTopComponent;
import sk.stuba.fiit.kvasnicka.topologyvisual.palette.gui.TopologyInformation;
import sk.stuba.fiit.kvasnicka.topologyvisual.serialisation.SerialisationHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.serialisation.XmlSerializationProxy;

public class TopologyFileTypeDataObject extends MultiDataObject {

    private static Logger logg = Logger.getLogger(TopologyFileTypeDataObject.class);
    @Getter
    private transient SerialisationHelper.DeserialisationResult loadSettings;
    private transient GraphMLWriter<TopologyVertex, TopologyEdge> graphWriter = new GraphMLWriter<TopologyVertex, TopologyEdge>();

    public TopologyFileTypeDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException, GraphIOException, JAXBException {
        super(pf, loader);
        registerEditor("text/qsim", true);
 
        deserialize();
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    @MultiViewElement.Registration(displayName = "#LBL_TopologyInfoMultiview",
    iconBase = "sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/qsimFileType.png",
    mimeType = "text/qsim",
    persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
    preferredID = "TopologyInfoMultiview",
    position = 1000)
    @Messages("LBL_TopologyInfoMultiview=Info")
    public static TopologyInformation createEditor(Lookup lkp) {
        return new TopologyInformation(lkp);
    }

    private void serialize(XmlSerializationProxy proxy, Topology topology) throws JAXBException, FileNotFoundException, IOException {
        logg.debug("serialisation....");

        BufferedWriter fileOutput = new BufferedWriter(new FileWriter(FileUtil.toFile(getPrimaryFile())));

        StringWriter sw = new StringWriter();
        marshall(proxy, sw);
        String jaxbString = sw.toString();

        String jungString = saveJung(topology);

        fileOutput.write(jaxbString);
        fileOutput.newLine();
        fileOutput.write(jungString);
        fileOutput.close();
        logg.debug("serialised");
    }

    private String saveJung(Topology topology) throws IOException {
        StringWriter out = new StringWriter();
        initSerialisationUtils(topology);
        graphWriter.save(topology.getGraph(), out);
        return out.toString();
    }

    private void marshall(XmlSerializationProxy proxy, StringWriter output) throws JAXBException, FileNotFoundException {
        JAXBContext context = JAXBContext.newInstance(XmlSerializationProxy.class);

        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        m.marshal(proxy, output);
    }

    private void deserialize() throws GraphIOException, IOException, JAXBException {
        logg.debug("deserialisation....");
        this.loadSettings = SerialisationHelper.loadSettings(FileUtil.toFile(getPrimaryFile()));
        logg.debug("deserialised");
    }

    public void save(Topology topology) throws JAXBException, FileNotFoundException, IOException {
        //create serialisation proxy
        XmlSerializationProxy proxy = new XmlSerializationProxy(topology);
        //call serialize() method
        serialize(proxy, topology);
    }

    private void initSerialisationUtils(final Topology topology) {
        graphWriter.addVertexData("x", null, "0",
                new Transformer<TopologyVertex, String>() {

                    @Override
                    public String transform(TopologyVertex v) {
                        return Double.toString(topology.getLayout().getX(v));
                    }
                });

        graphWriter.addVertexData("y", null, "0",
                new Transformer<TopologyVertex, String>() {

                    @Override
                    public String transform(TopologyVertex v) {
                        return Double.toString(topology.getLayout().getY(v));
                    }
                });

        graphWriter.addVertexData("imageType", null, "null",
                new Transformer<TopologyVertex, String>() {

                    @Override
                    public String transform(TopologyVertex v) {
                        return v.getImageType().name();
                    }
                });

        graphWriter.addVertexData("vertex_name", null, "null",
                new Transformer<TopologyVertex, String>() {

                    @Override
                    public String transform(TopologyVertex v) {
                        return v.getName();
                    }
                });

        graphWriter.addEdgeData("vertex1_name", null, "null",
                new Transformer<TopologyEdge, String>() {

                    @Override
                    public String transform(TopologyEdge e) {
                        return e.getVertex1().getName();
                    }
                });

        graphWriter.addEdgeData("vertex2_name", null, "null",
                new Transformer<TopologyEdge, String>() {

                    @Override
                    public String transform(TopologyEdge e) {
                        return e.getVertex2().getName();
                    }
                });
    }
}
