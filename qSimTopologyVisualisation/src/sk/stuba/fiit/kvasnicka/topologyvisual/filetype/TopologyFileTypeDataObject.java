/*
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
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.filetype;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.graph.AbstractGraph;
import edu.uci.ics.jung.io.GraphIOException;
import edu.uci.ics.jung.io.GraphMLWriter;
import java.awt.Component;
import java.awt.Graphics;
import java.io.*;
import java.util.List;
import javax.swing.Icon;
import lombok.Getter;
import org.apache.log4j.Logger;
import org.netbeans.spi.actions.AbstractSavable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.edges.TopologyEdge;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.utils.TopologyVertexFactory;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.serialisation.DeserialisationResult;
import sk.stuba.fiit.kvasnicka.topologyvisual.serialisation.SerialisationHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.serialisation.SerializationProxy;
import sk.stuba.fiit.kvasnicka.topologyvisual.utils.SimulationData;

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
    private InstanceContent content = new InstanceContent();

    public TopologyFileTypeDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
        super(pf, loader);
//        registerEditor("text/qsim", true);
        CookieSet cookies = getCookieSet();
        cookies.add((Node.Cookie) new MyOpenSupport(getPrimaryEntry()));
        //deseralise file
        try {
            this.loadSettings = deserialize();
        } catch (IOException e) {
            logg.error(e);
            this.loadSettings = null;
        }
        logg.debug("deserialisation completed - file: " + getPrimaryFile().getNameExt());
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

    private void serialize(SerializationProxy proxy) throws IOException {
        logg.debug("serialisation....- file: " + getPrimaryFile().getNameExt());


        String proxyString = SerializationProxy.serializetoString(proxy);

        FileWriter fileStream = new FileWriter(FileUtil.toFile(getPrimaryFile()));
        fileStream.write(proxyString);
        fileStream.close();

        logg.debug("serialised - file: " + getPrimaryFile().getNameExt());
    }

    private DeserialisationResult deserialize() throws IOException {
        logg.debug("deserialisation.... - file: " + getPrimaryFile().getNameExt());
        return serialisationHelper.loadSettings(FileUtil.toFile(getPrimaryFile()));
    }

    public void save() throws IOException {
        logg.debug("saving topology");
        //create serialisation proxy
        SerializationProxy proxy = new SerializationProxy();
        proxy.prepareProxy(getLoadSettings().getVertexFactory(), getLoadSettings().getG(), getLoadSettings().getLayout(), getLoadSettings().getName(), getLoadSettings().getDescription(), getLoadSettings().isDistanceVectorRouting(), getLoadSettings().getSimulRulesData());
        //call serialize() method
        serialize(proxy);
    }

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
    public void modifiedTopology(TopComponent window, AbstractGraph<TopologyVertex, TopologyEdge> g, AbstractLayout<TopologyVertex, TopologyEdge> layout, TopologyVertexFactory vFactory, List<SimulationData.Data> simulRulesData) {
        topComponent = window;
        loadSettings.setG(g);
        loadSettings.setLayout(layout);
        loadSettings.setVertexFactory(vFactory);
        loadSettings.setSimulRulesData(simulRulesData);
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
