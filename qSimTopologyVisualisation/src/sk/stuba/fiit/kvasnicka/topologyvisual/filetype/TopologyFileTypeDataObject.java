/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.filetype;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.graph.AbstractGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.io.GraphIOException;
import edu.uci.ics.jung.io.GraphMLWriter;
import java.awt.Component;
import java.awt.Graphics;
import java.io.*;
import javax.swing.Icon;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import lombok.Getter;
import org.apache.commons.collections15.Transformer;
import org.apache.log4j.Logger;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.spi.actions.AbstractSavable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.edges.TopologyEdge;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.utils.TopologyVertexFactory;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.palette.gui.TopologyInformation;
import sk.stuba.fiit.kvasnicka.topologyvisual.serialisation.DeserialisationResult;
import sk.stuba.fiit.kvasnicka.topologyvisual.serialisation.SerialisationHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.serialisation.XmlSerializationProxy;

public class TopologyFileTypeDataObject extends MultiDataObject {

    private static Logger logg = Logger.getLogger(TopologyFileTypeDataObject.class);
    private transient DeserialisationResult loadSettings;
    private transient GraphMLWriter<TopologyVertex, TopologyEdge> graphWriter = new GraphMLWriter<TopologyVertex, TopologyEdge>();
    private SerialisationHelper serialisationHelper = new SerialisationHelper();
    private TopComponent topComponent;
    @Getter
    /**
     * flag that indicates whether this file was modified or not. I know, it can
     * be done in other more elegant way (SaveCookie), but it did not work...
     */
    private boolean dirty = false;

    public TopologyFileTypeDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException, GraphIOException, JAXBException {
        super(pf, loader);
        registerEditor("text/qsim", true);
        //deseralise file
        this.loadSettings = deserialize();
        logg.debug("deserialisation completed");
    }

    /**
     * cannot return null
     *
     * @return
     */
    public DeserialisationResult getLoadSettings() {
        if (loadSettings == null) {
            loadSettings = new DeserialisationResult();
        }
        return loadSettings;
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

    private void serialize(XmlSerializationProxy proxy, Graph topologyGraph, AbstractLayout<TopologyVertex, TopologyEdge> layout) throws JAXBException, FileNotFoundException, IOException {
        logg.debug("serialisation....");

        BufferedWriter fileOutput = new BufferedWriter(new FileWriter(FileUtil.toFile(getPrimaryFile())));
        String jaxbString = "";
        if (proxy != null) {
            StringWriter sw = new StringWriter();
            marshall(proxy, sw);
            jaxbString = sw.toString();
        }

        String jungString = "";
        if (topologyGraph != null && layout != null) {
            jungString = saveJung(topologyGraph, layout);
        }
        fileOutput.write(jaxbString);
        fileOutput.newLine();
        fileOutput.write(jungString);
        fileOutput.close();
        logg.debug("serialised");
    }

    private String saveJung(Graph topologyGraph, AbstractLayout<TopologyVertex, TopologyEdge> layout) throws IOException {
        StringWriter out = new StringWriter();
        initSerialisationUtils(layout);
        graphWriter.save(topologyGraph, out);
        return out.toString();
    }

    private void marshall(XmlSerializationProxy proxy, StringWriter output) throws JAXBException, FileNotFoundException {
        JAXBContext context = JAXBContext.newInstance(XmlSerializationProxy.class);

        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        m.marshal(proxy, output);
    }

    private DeserialisationResult deserialize() throws GraphIOException, IOException, JAXBException {
        logg.debug("deserialisation....");
        return serialisationHelper.loadSettings(FileUtil.toFile(getPrimaryFile()));
    }

    public void save() throws JAXBException, FileNotFoundException, IOException {
        logg.debug("saving topology");
        //create serialisation proxy
        XmlSerializationProxy proxy = new XmlSerializationProxy();
        proxy.prepareProxy(getLoadSettings().getVFactory(), getLoadSettings().getG(), getLoadSettings().getName(), getLoadSettings().getDescription());
        //call serialize() method
        serialize(proxy, getLoadSettings().getG(), getLoadSettings().getLayout());
    }

    /**
     * init some serialisation settings for JUNG serialisation
     *
     * @param layout
     */
    private void initSerialisationUtils(final AbstractLayout<TopologyVertex, TopologyEdge> layout) {
        graphWriter.addVertexData("x", null, "0",
                new Transformer<TopologyVertex, String>() {

                    @Override
                    public String transform(TopologyVertex v) {
                        return Double.toString(layout.getX(v));
                    }
                });

        graphWriter.addVertexData("y", null, "0",
                new Transformer<TopologyVertex, String>() {

                    @Override
                    public String transform(TopologyVertex v) {
                        return Double.toString(layout.getY(v));
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
    private InstanceContent content = new InstanceContent();

    /**
     * mark this file as modified because topology information has changed
     *
     * @param window
     * @param name
     * @param description
     */
    public void modifiedInformation(TopComponent window, String name, String description) {
        topComponent = window;
        loadSettings.setDescription(description);
        loadSettings.setName(name);
        markModified(window);
    }

    /**
     * mark file as modified because topology was changed
     *
     * @param window
     */
    public void modifiedTopology(TopComponent window, AbstractGraph<TopologyVertex, TopologyEdge> g, AbstractLayout<TopologyVertex, TopologyEdge> layout, TopologyVertexFactory vFactory) {
        topComponent = window;
        loadSettings.setG(g);
        loadSettings.setLayout(layout);
        loadSettings.setVFactory(vFactory);
        markModified(window);
    }

    /**
     * marks this file as modified
     *
     * @param window
     */
    private void markModified(TopComponent window) {
        dirty = true;
        window.setDisplayName(getPrimaryFile().getNameExt() + "*");
        if (getLookup().lookup(TopologySavable.class) == null) {
            content.add(new TopologySavable(getPrimaryFile().getNameExt()));
        }
    }
    private static final Icon ICON = ImageUtilities.loadImageIcon("sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/qsimFileType.png", true);

    public class TopologySavable extends AbstractSavable implements Icon {

        private final String fileName;

        TopologySavable(String fileName) {
            register();
            this.fileName = fileName;
        }

        @Override
        protected String findDisplayName() {
            return this.fileName;
        }

        @Override
        protected void handleSave() throws IOException {
            if (topComponent == null) {
                throw new IllegalStateException("underlying TopComponent was not set");
            }
            try {
                dirty = false;
                content.remove(this);
                unregister();
                logg.debug("---------saving");
                TopologyFileTypeDataObject.this.save();
                topComponent.setDisplayName(TopologyFileTypeDataObject.this.getPrimaryFile().getNameExt());
            } catch (JAXBException ex) {
                Exceptions.printStackTrace(ex);
            } catch (FileNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        TopologyFileTypeDataObject tc() {
            return TopologyFileTypeDataObject.this;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TopologySavable) {
                TopologySavable m = (TopologySavable) obj;
                return tc() == m.tc();
            }
            return false;
        }

        @Override
        public int hashCode() {
            return tc().hashCode();
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            ICON.paintIcon(c, g, x, y);
        }

        @Override
        public int getIconWidth() {
            return ICON.getIconWidth();
        }

        @Override
        public int getIconHeight() {
            return ICON.getIconHeight();
        }
    }
}
