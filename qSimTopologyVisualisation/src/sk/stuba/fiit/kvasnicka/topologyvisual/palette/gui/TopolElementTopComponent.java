/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.palette.gui;

import edu.uci.ics.jung.visualization.VisualizationViewer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.xml.bind.JAXBException;
import lombok.Getter;
import org.apache.log4j.Logger;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.spi.actions.AbstractSavable;
import org.openide.awt.StatusDisplayer;
import org.openide.awt.UndoRedo;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import sk.stuba.fiit.kvasnicka.topologyvisual.topology.TopologyCreation;
import sk.stuba.fiit.kvasnicka.topologyvisual.dialogs.utils.DialogHandler;
import sk.stuba.fiit.kvasnicka.topologyvisual.filetype.TopologyFileTypeDataObject;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.VertexSelectionManager;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.edges.TopologyEdge;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.events.VertexCreatedEvent;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.events.VertexCreatedListener;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.utils.TopologyElementCreatorHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.NetbeansWindowHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.palette.TopologyPaletteTopComponent;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.palette.events.PaletteSelectionEvent;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.palette.events.PaletteSelectionListener;
import sk.stuba.fiit.kvasnicka.topologyvisual.lookuputils.RouteChanged;
import sk.stuba.fiit.kvasnicka.topologyvisual.palette.PaletteActionEnum;
import sk.stuba.fiit.kvasnicka.topologyvisual.serialisation.SerialisationHelper.DeserialisationResult;

/**
 * Top component which displays something.
 */
@MultiViewElement.Registration(displayName = "#LBL_TopologyCreatorMultiview",
iconBase = "sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/qsimFileType.png",
mimeType = "text/qsim",
persistenceType = TopComponent.PERSISTENCE_NEVER,
preferredID = "TopologyCreatorMultiview",
position = 2000)
@NbBundle.Messages("LBL_TopologyCreatorMultiview=Creator")
public final class TopolElementTopComponent extends JPanel implements Serializable, VertexCreatedListener, DocumentListener, MultiViewElement, PaletteSelectionListener {

    private static Logger logg = Logger.getLogger(TopolElementTopComponent.class);
    private TopologyCreation topology;
    private TopologyElementCreatorHelper topologyElementCreator;
    private PaletteActionEnum selectedPaletteAction = null;
    private DialogHandler dialogHandler;
    private InstanceContent content;
    private TopologyFileTypeDataObject dataObject;
    private JToolBar toolBar = new JToolBar();
    private MultiViewElementCallback callback;
    @Getter
    private VertexSelectionManager vertexSelectionManager = new VertexSelectionManager();

    public TopolElementTopComponent(Lookup lkp) {
        this();
        dataObject = lkp.lookup(TopologyFileTypeDataObject.class);
        assert dataObject != null;

        topology = new TopologyCreation(this);
        DeserialisationResult loadSettings = dataObject.getLoadSettings();

        if (loadSettings == null) {//there was some problem when deserialising
            dialogHandler = new DialogHandler();
            topology.createDefaultSettings();
        } else {
            loadSettings.getVFactory().setTopolElementTopComponent(this);
            topology.loadFromSettings(loadSettings);
            dialogHandler = new DialogHandler(topology.getVertexFactory().getVertexRouterList().size(), topology.getVertexFactory().getVertexSwitchList().size(), topology.getVertexFactory().getVertexComputerList().size());
        }

        topology.initTopology();
        topologyElementCreator = new TopologyElementCreatorHelper(topology, this);
        //listen for vertices to change their position
        topology.getVv().addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                dataObject.modified(callback.getTopComponent());
            }
        });
        TopologyPaletteTopComponent component = (TopologyPaletteTopComponent) WindowManager.getDefault().findTopComponent("TopologyPaletteTopComponent");
        if (component == null) {
            logg.error("Could not find component TopologyPaletteTopComponent");
            return;
        }
        component.initListener(this);
    }

    private TopolElementTopComponent() {
        if (dialogHandler == null) {
            dialogHandler = new DialogHandler();
        }

        initComponents();

        // setName(NbBundle.getMessage(TopolElementTopComponent.class, "CTL_TopolElementTopComponent"));
        // setToolTipText(NbBundle.getMessage(TopolElementTopComponent.class, "HINT_TopolElementTopComponent"));



//        paletteController.addPropertyChangeListener(new PropertyChangeListener() {
//
//            @Override
//            public void propertyChange(PropertyChangeEvent evt) {
//                MyNode selectedItem = paletteController.getSelectedItem().lookup(MyNode.class);
//                if (selectedItem == null) {
//                    selectedPaletteAction = null;
//                    logg.debug("disselected action");
//                    topologyElementCreator.cancelAction();
//                    return;
//                }
//                PaletteActionEnum selAction = selectedItem.getPaletteTopologyElement().getPaletteAction();
//
//                setStatusBarText(NbBundle.getMessage(TopolElementTopComponent.class, "creating.new") + " " + selectedItem.getPaletteTopologyElement().getName());
//
//                if (!PaletteActionEnum.isEdgeAction(selAction)) {//when creating new Vertex, editing mode is required
//                    topology.setEditingMode();
//                }
//                if (selectedPaletteAction == null) {
//                    selectedPaletteAction = selAction;
//                } else {
//                    selectedPaletteAction = selAction;
//                }
//                topologyElementCreator.setAction(selAction);
//            }
//        });


        content = new InstanceContent();

//        associateLookup(new ProxyLookup(new Lookup[]{
//                    new AbstractLookup(content),//creating new nodes will invoke certain action in RoutingTopComponent
////                    Lookups.fixed(paletteController), //palette is opening together with this window
//                //   Lookups.singleton(this)//used to retieve curently activated TopolElementTopComponent window
//                }));

    }

    public Object getDefault() {
        return null;
    }

    @Override
    public void paletteSelectedOccurred(PaletteSelectionEvent evt) {
        selectedPaletteAction = evt.getSelectedAction();
        setStatusBarText(NbBundle.getMessage(TopolElementTopComponent.class, "creating.new") + " " + selectedPaletteAction.getDisplayableName());
        if (!PaletteActionEnum.isEdgeAction(selectedPaletteAction)) {//when creating new Vertex, editing mode is required
            topology.setEditingMode();
        }
        topologyElementCreator.setAction(selectedPaletteAction);
    }

    @Override
    public void paletteDeselectedOccurred(PaletteSelectionEvent evt) {
        topologyElementCreator.cancelAction();
    }

    public void paletteClearSelection() {
        TopologyPaletteTopComponent component = (TopologyPaletteTopComponent) WindowManager.getDefault().findTopComponent("TopologyPaletteTopComponent");
        if (component == null) {
            logg.error("Could not find component TopologyPaletteTopComponent");
            return;
        }
        component.clearSelection();
    }

    private void openPaletteWindow() {
        TopologyPaletteTopComponent component = (TopologyPaletteTopComponent) WindowManager.getDefault().findTopComponent("TopologyPaletteTopComponent");
        if (component == null) {
            logg.error("Could not find component TopologyPaletteTopComponent");
            return;
        }
        component.open();
    }

    private void hidePaletteWindow() {
        TopologyPaletteTopComponent component = (TopologyPaletteTopComponent) WindowManager.getDefault().findTopComponent("TopologyPaletteTopComponent");
        if (component == null) {
            logg.error("Could not find component TopologyPaletteTopComponent");
            return;
        }
        component.close();
    }

    /**
     * adds Jung component into TopComponent - this happens when topology has
     * been loaded from file (successfully or not)
     *
     * @param vv
     */
    public void addJungIntoFrame(VisualizationViewer<TopologyVertex, TopologyEdge> vv) {
        logg.debug("adding Jung component to TopComponent");
        jPanel1.add(topology.getVv(), BorderLayout.CENTER);
        validate();
        topology.getVv().repaint();
    }

    /**
     * sets text that will be shown in status bar - e.g. information what is
     * user about to do,...
     *
     * @param text text to show
     */
    public void setStatusBarText(String text) {
        StatusDisplayer.getDefault().setStatusText(text);
    }

    public TopologyElementCreatorHelper getTopologyElementCreator() {
        return topologyElementCreator;
    }

    public DialogHandler getDialogHandler() {
        return dialogHandler;
    }

    public TopologyCreation getTopology() {
        return topology;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();

        jPanel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel1MouseClicked(evt);
            }
        });
        jPanel1.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jPanel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel1MouseClicked
    }//GEN-LAST:event_jPanel1MouseClicked
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        //  store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        //  read your settings according to their version
    }

    public PaletteActionEnum getSelectedPaletteAction() {
        return selectedPaletteAction;
    }

    @Override
    public void vertexCreatedOccurred(VertexCreatedEvent evt) {
        content.add(evt.getNewVertex());
        dataObject.modified(callback.getTopComponent());
    }

    @Deprecated
    public void routesChanged() {
        logg.debug("routes changed");
        content.add(new RouteChanged());
        dataObject.modified(callback.getTopComponent());
    }
    private static final Icon ICON = ImageUtilities.loadImageIcon("sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/qsimFileType.png", true);

    //-------savable-------
    @Override
    public void insertUpdate(DocumentEvent e) {
        dataObject.modified(callback.getTopComponent());
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        dataObject.modified(callback.getTopComponent());
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        dataObject.modified(callback.getTopComponent());
    }

    //-------------
    @Override
    public JComponent getVisualRepresentation() {
        return this;
    }

    @Override
    public JComponent getToolbarRepresentation() {
        return toolBar;
    }

    @Override
    public Action[] getActions() {
        return new Action[0];
    }

    @Override
    public Lookup getLookup() {
        return dataObject.getLookup();
    }

    @Override
    public void componentOpened() {
    }

    @Override
    public void componentClosed() {
    }

    @Override
    public void componentShowing() {
    }

    @Override
    public void componentHidden() {
        hidePaletteWindow();
        setStatusBarText("");
        topologyElementCreator.cancelAction();
        paletteClearSelection();
    }

    @Override
    public void componentActivated() {
        NetbeansWindowHelper.getInstance().setActiveTopologyTopComponent(this);
        openPaletteWindow();
    }

    @Override
    public void componentDeactivated() {
    }

    @Override
    public UndoRedo getUndoRedo() {
        return UndoRedo.NONE;
    }

    @Override
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        this.callback = callback;
        if (dataObject.isModified()) {
            callback.updateTitle(dataObject.getPrimaryFile().getNameExt() + "*");
        } else {
            callback.updateTitle(dataObject.getPrimaryFile().getNameExt());
        }
    }

    @Override
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }

    private class TopologySavable extends AbstractSavable implements Icon {

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
            try {
                tc().content.remove(this);
                unregister();
                dataObject.save(TopolElementTopComponent.this.topology);
                callback.updateTitle(dataObject.getPrimaryFile().getNameExt());
            } catch (JAXBException ex) {
                Exceptions.printStackTrace(ex);
            } catch (FileNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        TopolElementTopComponent tc() {
            return TopolElementTopComponent.this;
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
